package com.yakuzasmart.app.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.yakuzasmart.app.core.constants.BleConstants
import com.yakuzasmart.app.core.logging.DiagnosticLogger
import com.yakuzasmart.app.core.security.BluetoothPermissionHelper
import com.yakuzasmart.app.domain.model.BleDeviceItem
import com.yakuzasmart.app.domain.model.ConnectionStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentLinkedQueue

@SuppressLint("MissingPermission")
class BleManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter?
        get() = bluetoothManager?.adapter

    private var activeGatt: BluetoothGatt? = null
    var writeCharacteristic: BluetoothGattCharacteristic? = null
        private set
    var notifyCharacteristic: BluetoothGattCharacteristic? = null
        private set

    private val _isBluetoothEnabled = MutableStateFlow(bluetoothAdapter?.isEnabled == true)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<BleDeviceItem>>(emptyList())
    val scannedDevices: StateFlow<List<BleDeviceItem>> = _scannedDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanError = MutableStateFlow<String?>(null)
    val scanError: StateFlow<String?> = _scanError.asStateFlow()

    private val _inboundPackets = MutableSharedFlow<ByteArray>(replay = 0)
    val inboundPackets: SharedFlow<ByteArray> = _inboundPackets.asSharedFlow()

    private val writeQueue = ConcurrentLinkedQueue<ByteArray>()
    private var isWriting = false

    private var autoReconnectEnabled = false
    private var lastTargetAddress: String? = null
    private var lastTargetName: String? = null
    private var reconnectJob: Job? = null

    fun enableAutoReconnect(address: String, displayName: String?) {
        autoReconnectEnabled = true
        lastTargetAddress = address
        lastTargetName = displayName
    }

    fun disableAutoReconnect() {
        autoReconnectEnabled = false
        reconnectJob?.cancel()
        reconnectJob = null
    }

    private fun checkAutoReconnect() {
        if (!autoReconnectEnabled || lastTargetAddress == null) return
        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) return

        DiagnosticLogger.i("BleManager", "Auto-reconnect active: scheduling reconnect to $lastTargetAddress in 1.8s...")
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(1800)
            if (_connectionStatus.value == ConnectionStatus.DISCONNECTED && autoReconnectEnabled) {
                val address = lastTargetAddress ?: return@launch
                val name = lastTargetName
                DiagnosticLogger.i("BleManager", "Attempting auto-reconnect to $address...")
                connectByAddress(address, name)
            }
        }
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                val enabled = (state == BluetoothAdapter.STATE_ON)
                _isBluetoothEnabled.value = enabled
                DiagnosticLogger.i("BleManager", "Bluetooth hardware state changed: enabled=$enabled")
                if (!enabled) {
                    stopScan()
                    disconnect()
                }
            }
        }
    }

    init {
        try {
            val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            context.registerReceiver(bluetoothReceiver, filter)
        } catch (e: Exception) {
            DiagnosticLogger.w("BleManager", "Failed to register Bluetooth state receiver: ${e.message}")
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                try {
                    val name = device.name ?: result.scanRecord?.deviceName
                    val address = device.address ?: return
                    val rssi = result.rssi
                    val isYakuza = (name?.contains("YAKUZA", ignoreCase = true) == true) ||
                            (name?.contains("YKZ", ignoreCase = true) == true) ||
                            (name?.contains("SCOOTER", ignoreCase = true) == true)

                    val item = BleDeviceItem(
                        name = name,
                        address = address,
                        rssi = rssi,
                        isBonded = device.bondState == BluetoothDevice.BOND_BONDED,
                        isYakuzaDevice = isYakuza
                    )

                    val current = _scannedDevices.value.toMutableList()
                    val existingIndex = current.indexOfFirst { it.address == address }
                    if (existingIndex >= 0) {
                        current[existingIndex] = item
                    } else {
                        current.add(item)
                    }
                    _scannedDevices.value = current.sortedWith(
                        compareByDescending<BleDeviceItem> { it.isYakuzaDevice }
                            .thenByDescending { it.rssi }
                    )
                } catch (e: Exception) {
                    DiagnosticLogger.w("BleManager", "Error processing scan result: ${e.message}")
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            val errorMsg = when (errorCode) {
                SCAN_FAILED_ALREADY_STARTED -> "Scan already started"
                SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "App registration failed"
                SCAN_FAILED_FEATURE_UNSUPPORTED -> "BLE Scan unsupported on device"
                SCAN_FAILED_INTERNAL_ERROR -> "Bluetooth driver internal error"
                else -> "Scan error code $errorCode"
            }
            DiagnosticLogger.e("BleManager", "BLE Scan failed: $errorMsg")
            _scanError.value = errorMsg
            _isScanning.value = false
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            DiagnosticLogger.i("BleManager", "GATT state change: status=$status, newState=$newState")
            if (status != BluetoothGatt.GATT_SUCCESS) {
                DiagnosticLogger.w("BleManager", "GATT Error: status $status. Disconnecting...")
                releaseGatt()
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
                checkAutoReconnect()
                return
            }

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _connectionStatus.value = ConnectionStatus.DISCOVERING_SERVICES
                    DiagnosticLogger.i("BleManager", "Connected to GATT server. Optimizing connection priority & discovering services...")
                    try {
                        gatt?.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                        scope.launch {
                            delay(150)
                            gatt?.discoverServices()
                        }
                    } catch (e: Exception) {
                        DiagnosticLogger.e("BleManager", "Error discovering services: ${e.message}")
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    DiagnosticLogger.i("BleManager", "Disconnected from GATT server.")
                    releaseGatt()
                    _connectionStatus.value = ConnectionStatus.DISCONNECTED
                    checkAutoReconnect()
                }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            DiagnosticLogger.i("BleManager", "MTU changed to $mtu (status=$status)")
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS || gatt == null) {
                DiagnosticLogger.e("BleManager", "Service discovery failed with status $status")
                return
            }

            try {
                DiagnosticLogger.i("BleManager", "GATT Services Discovered: ${gatt.services.size} total")

                var foundWrite: BluetoothGattCharacteristic? = null
                var foundNotify: BluetoothGattCharacteristic? = null

                // 1. Inspect all services and characteristics
                for (service in gatt.services) {
                    val sUuid = service.uuid
                    DiagnosticLogger.i("BleManager", "Service: $sUuid")
                    for (char in service.characteristics) {
                        val props = char.properties
                        val hasWrite = (props and (BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0
                        val hasNotify = (props and (BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_INDICATE)) != 0
                        DiagnosticLogger.i("BleManager", "  -> Char: ${char.uuid} [Write=$hasWrite, Notify=$hasNotify]")

                        // Check known standard e-scooter UUIDs
                        if (char.uuid == BleConstants.WRITE_CHAR_FFF2 ||
                            char.uuid == BleConstants.CHAR_UUID_FFE1 ||
                            char.uuid == BleConstants.NORDIC_UART_RX) {
                            foundWrite = char
                        }
                        if (char.uuid == BleConstants.NOTIFY_CHAR_FFF1 ||
                            char.uuid == BleConstants.CHAR_UUID_FFE1 ||
                            char.uuid == BleConstants.NORDIC_UART_TX) {
                            foundNotify = char
                        }
                    }
                }

                // 2. Dynamic Fallback: If not found in known UUID table, pick first writable & notifiable characteristic!
                if (foundWrite == null || foundNotify == null) {
                    for (service in gatt.services) {
                        // Skip standard generic access/attribute profiles (0x1800, 0x1801)
                        val sStr = service.uuid.toString()
                        if (sStr.startsWith("00001800") || sStr.startsWith("00001801")) continue

                        for (char in service.characteristics) {
                            val props = char.properties
                            if (foundWrite == null && (props and (BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0) {
                                foundWrite = char
                            }
                            if (foundNotify == null && (props and (BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_INDICATE)) != 0) {
                                foundNotify = char
                            }
                        }
                    }
                }

                writeCharacteristic = foundWrite
                notifyCharacteristic = foundNotify

                if (writeCharacteristic != null) {
                    val props = writeCharacteristic!!.properties
                    val writeType = if ((props and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                        BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                    } else {
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    }
                    writeCharacteristic!!.writeType = writeType
                    DiagnosticLogger.i("BleManager", "SUCCESS: Selected Write Pipe: ${writeCharacteristic!!.uuid} (Type=$writeType)")
                } else {
                    DiagnosticLogger.e("BleManager", "WARNING: No writable characteristic found on this device!")
                }

                if (notifyCharacteristic != null) {
                    enableNotifications(gatt, notifyCharacteristic!!)
                    DiagnosticLogger.i("BleManager", "SUCCESS: Selected Notify Pipe: ${notifyCharacteristic!!.uuid}")
                } else {
                    DiagnosticLogger.w("BleManager", "No notify characteristic found.")
                }

                _connectionStatus.value = ConnectionStatus.CONNECTED_IDLE
            } catch (e: Exception) {
                DiagnosticLogger.e("BleManager", "Error in onServicesDiscovered: ${e.message}")
                _connectionStatus.value = ConnectionStatus.CONNECTED_IDLE
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            DiagnosticLogger.rx("BleManager", "Inbound packet on ${characteristic.uuid}", value)
            scope.launch {
                _inboundPackets.emit(value)
            }
        }

        @Deprecated("Deprecated in Java for API 33")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                val value = characteristic?.value ?: return
                DiagnosticLogger.rx("BleManager", "Inbound packet on ${characteristic.uuid} (Legacy)", value)
                scope.launch {
                    _inboundPackets.emit(value)
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            isWriting = false
            if (status == BluetoothGatt.GATT_SUCCESS) {
                DiagnosticLogger.i("BleManager", "Packet write success to ${characteristic?.uuid}")
            } else {
                DiagnosticLogger.e("BleManager", "Packet write failed with status $status")
            }
            processNextWrite()
        }
    }

    fun startScan(): Boolean {
        _scanError.value = null

        val adapter = bluetoothAdapter
        if (adapter == null) {
            _scanError.value = "Bluetooth is not supported on this device"
            DiagnosticLogger.e("BleManager", _scanError.value!!)
            return false
        }

        if (!adapter.isEnabled) {
            _isBluetoothEnabled.value = false
            _scanError.value = "Bluetooth is turned OFF. Please turn on Bluetooth in settings."
            DiagnosticLogger.w("BleManager", _scanError.value!!)
            return false
        }
        _isBluetoothEnabled.value = true

        if (!BluetoothPermissionHelper.hasRequiredPermissions(context)) {
            _scanError.value = "Bluetooth permissions are required to scan for scooters."
            DiagnosticLogger.w("BleManager", _scanError.value!!)
            return false
        }

        if (!BluetoothPermissionHelper.isLocationEnabledForBle(context)) {
            _scanError.value = "Please turn on GPS/Location services for Bluetooth scanning on this Android version."
            DiagnosticLogger.w("BleManager", _scanError.value!!)
            return false
        }

        try {
            val scanner = adapter.bluetoothLeScanner
            if (scanner == null) {
                _scanError.value = "Bluetooth LE scanner is currently unavailable."
                DiagnosticLogger.e("BleManager", _scanError.value!!)
                return false
            }

            _scannedDevices.value = emptyList()
            _isScanning.value = true

            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            DiagnosticLogger.i("BleManager", "Starting safe BLE scan (15s timeout)...")
            scanner.startScan(null, settings, scanCallback)

            scope.launch {
                delay(BleConstants.SCAN_TIMEOUT_MS)
                if (_isScanning.value) {
                    stopScan()
                }
            }
            return true
        } catch (e: SecurityException) {
            _scanError.value = "Bluetooth permission error: ${e.message}"
            _isScanning.value = false
            DiagnosticLogger.e("BleManager", "SecurityException during scan: ${e.message}")
            return false
        } catch (e: Exception) {
            _scanError.value = "Unable to start scan: ${e.message}"
            _isScanning.value = false
            DiagnosticLogger.e("BleManager", "Exception during scan: ${e.message}")
            return false
        }
    }

    fun stopScan() {
        if (!_isScanning.value) return
        try {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: Exception) {
            DiagnosticLogger.w("BleManager", "Error stopping scan: ${e.message}")
        } finally {
            _isScanning.value = false
            DiagnosticLogger.i("BleManager", "BLE scan stopped.")
        }
    }

    fun connect(deviceItem: BleDeviceItem) {
        connectByAddress(deviceItem.address, deviceItem.displayName)
    }

    fun connectByAddress(address: String, displayName: String?) {
        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            _isBluetoothEnabled.value = false
            DiagnosticLogger.e("BleManager", "Cannot connect: Bluetooth is OFF.")
            return
        }

        if (!BluetoothPermissionHelper.hasRequiredPermissions(context)) {
            DiagnosticLogger.e("BleManager", "Cannot connect: Missing permissions.")
            return
        }

        try {
            val device = adapter.getRemoteDevice(address)
            if (device == null) {
                DiagnosticLogger.e("BleManager", "Invalid Bluetooth address: $address")
                return
            }

            stopScan()
            releaseGatt()

            enableAutoReconnect(address, displayName)

            _connectionStatus.value = ConnectionStatus.CONNECTING
            DiagnosticLogger.i("BleManager", "Connecting to ${displayName ?: "Scooter"} [$address]...")

            activeGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            } else {
                device.connectGatt(context, false, gattCallback)
            }
        } catch (e: SecurityException) {
            DiagnosticLogger.e("BleManager", "SecurityException during connect: ${e.message}")
            _connectionStatus.value = ConnectionStatus.DISCONNECTED
        } catch (e: Exception) {
            DiagnosticLogger.e("BleManager", "Exception during connect: ${e.message}")
            _connectionStatus.value = ConnectionStatus.DISCONNECTED
        }
    }

    fun disconnect() {
        try {
            DiagnosticLogger.i("BleManager", "User requested disconnect.")
            disableAutoReconnect()
            activeGatt?.disconnect()
        } catch (e: Exception) {
            DiagnosticLogger.w("BleManager", "Error during disconnect: ${e.message}")
        } finally {
            releaseGatt()
            _connectionStatus.value = ConnectionStatus.DISCONNECTED
        }
    }

    private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        try {
            gatt.setCharacteristicNotification(characteristic, true)
            val descriptor = characteristic.getDescriptor(BleConstants.CCCD_UUID)
            if (descriptor != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    @Suppress("DEPRECATION")
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    gatt.writeDescriptor(descriptor)
                }
                DiagnosticLogger.i("BleManager", "Subscribed to CCCD notifications on ${characteristic.uuid}")
            }
        } catch (e: Exception) {
            DiagnosticLogger.w("BleManager", "Failed to subscribe notifications: ${e.message}")
        }
    }

    fun writePacket(packet: ByteArray) {
        if (packet.size > 20) {
            var offset = 0
            while (offset < packet.size) {
                val chunkSize = minOf(20, packet.size - offset)
                val chunk = packet.copyOfRange(offset, offset + chunkSize)
                writeQueue.add(chunk)
                offset += chunkSize
            }
        } else {
            writeQueue.add(packet)
        }
        if (!isWriting) {
            processNextWrite()
        }
    }

    @Synchronized
    private fun processNextWrite() {
        val gatt = activeGatt ?: return
        val char = writeCharacteristic
        if (char == null) {
            DiagnosticLogger.w("BleManager", "Cannot write packet: writeCharacteristic is not ready.")
            writeQueue.clear()
            isWriting = false
            return
        }

        if (isWriting || writeQueue.isEmpty()) return

        val packet = writeQueue.poll() ?: return
        isWriting = true

        DiagnosticLogger.tx("BleManager", "Dispatching to ${char.uuid} (Type=${char.writeType})", packet)

        try {
            val isNoResponse = (char.writeType == BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeCharacteristic(char, packet, char.writeType)
            } else {
                @Suppress("DEPRECATION")
                char.value = packet
                @Suppress("DEPRECATION")
                gatt.writeCharacteristic(char)
            }

            // For WRITE_TYPE_NO_RESPONSE, peripheral does not trigger onCharacteristicWrite callback,
            // so release lock after a 40ms interval between chunks!
            if (isNoResponse) {
                scope.launch {
                    delay(40)
                    isWriting = false
                    processNextWrite()
                }
            }
        } catch (e: Exception) {
            DiagnosticLogger.e("BleManager", "Exception during writeCharacteristic: ${e.message}")
            isWriting = false
        }
    }

    private fun releaseGatt() {
        try {
            activeGatt?.close()
        } catch (e: Exception) {
            // Ignore close exceptions
        } finally {
            activeGatt = null
            writeCharacteristic = null
            notifyCharacteristic = null
            writeQueue.clear()
            isWriting = false
        }
    }
}
