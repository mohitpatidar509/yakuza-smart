package com.yakuzasmart.app.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import com.yakuzasmart.app.core.constants.BleConstants
import com.yakuzasmart.app.core.logging.DiagnosticLogger
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
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var activeGatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private var notifyCharacteristic: BluetoothGattCharacteristic? = null

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<BleDeviceItem>>(emptyList())
    val scannedDevices: StateFlow<List<BleDeviceItem>> = _scannedDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _inboundPackets = MutableSharedFlow<ByteArray>(replay = 0)
    val inboundPackets: SharedFlow<ByteArray> = _inboundPackets.asSharedFlow()

    private val writeQueue = ConcurrentLinkedQueue<ByteArray>()
    private var isWriting = false

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                val name = device.name ?: result.scanRecord?.deviceName
                val address = device.address
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
                // Sort with Yakuza devices and strongest RSSI first
                _scannedDevices.value = current.sortedWith(
                    compareByDescending<BleDeviceItem> { it.isYakuzaDevice }
                        .thenByDescending { it.rssi }
                )
            }
        }

        override fun onScanFailed(errorCode: Int) {
            DiagnosticLogger.e("BleManager", "BLE Scan failed with errorCode: $errorCode")
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
                return
            }

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _connectionStatus.value = ConnectionStatus.DISCOVERING_SERVICES
                    DiagnosticLogger.i("BleManager", "Connected to GATT server. Requesting MTU and discovering services...")
                    gatt?.requestMtu(BleConstants.TARGET_MTU)
                    gatt?.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    DiagnosticLogger.i("BleManager", "Disconnected from GATT server.")
                    releaseGatt()
                    _connectionStatus.value = ConnectionStatus.DISCONNECTED
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

            DiagnosticLogger.i("BleManager", "Services discovered. Searching for 0xFFF0 service...")
            val service = gatt.getService(BleConstants.SERVICE_UUID)
            if (service == null) {
                DiagnosticLogger.e("BleManager", "FFF0 Service NOT found on device!")
                return
            }

            writeCharacteristic = service.getCharacteristic(BleConstants.WRITE_CHAR_UUID)
            notifyCharacteristic = service.getCharacteristic(BleConstants.NOTIFY_CHAR_UUID)

            if (notifyCharacteristic != null) {
                enableNotifications(gatt, notifyCharacteristic!!)
            } else {
                DiagnosticLogger.w("BleManager", "Notify characteristic FFF1 not found.")
            }

            _connectionStatus.value = ConnectionStatus.CONNECTED_IDLE
            DiagnosticLogger.i("BleManager", "BLE Setup Complete. Channel open.")
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            DiagnosticLogger.rx("BleManager", "Inbound packet on FFF1", value)
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
                DiagnosticLogger.rx("BleManager", "Inbound packet on FFF1 (Legacy API)", value)
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
                DiagnosticLogger.i("BleManager", "Packet write success to FFF2")
            } else {
                DiagnosticLogger.e("BleManager", "Packet write failed with status $status")
            }
            processNextWrite()
        }
    }

    fun startScan() {
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null) {
            DiagnosticLogger.e("BleManager", "Bluetooth adapter or LE scanner unavailable.")
            return
        }

        _scannedDevices.value = emptyList()
        _isScanning.value = true

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        DiagnosticLogger.i("BleManager", "Starting BLE scan (15s timeout)...")
        scanner.startScan(null, settings, scanCallback)

        scope.launch {
            delay(BleConstants.SCAN_TIMEOUT_MS)
            if (_isScanning.value) {
                stopScan()
            }
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
        val device = bluetoothAdapter?.getRemoteDevice(deviceItem.address)
        if (device == null) {
            DiagnosticLogger.e("BleManager", "Invalid Bluetooth address: ${deviceItem.address}")
            return
        }

        stopScan()
        releaseGatt()

        _connectionStatus.value = ConnectionStatus.CONNECTING
        DiagnosticLogger.i("BleManager", "Connecting to ${deviceItem.displayName} [${deviceItem.address}]...")

        activeGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(context, false, gattCallback)
        }
    }

    fun disconnect() {
        DiagnosticLogger.i("BleManager", "User requested disconnect.")
        activeGatt?.disconnect()
        releaseGatt()
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
    }

    private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
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
            DiagnosticLogger.i("BleManager", "Subscribed to CCCD 0x2902 notifications on FFF1.")
        } else {
            DiagnosticLogger.w("BleManager", "CCCD descriptor 0x2902 not found on FFF1.")
        }
    }

    fun writePacket(packet: ByteArray) {
        writeQueue.add(packet)
        if (!isWriting) {
            processNextWrite()
        }
    }

    @Synchronized
    private fun processNextWrite() {
        val gatt = activeGatt ?: return
        val char = writeCharacteristic ?: return
        if (isWriting || writeQueue.isEmpty()) return

        val packet = writeQueue.poll() ?: return
        isWriting = true

        DiagnosticLogger.tx("BleManager", "Dispatching packet to FFF2", packet)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeCharacteristic(char, packet, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
        } else {
            @Suppress("DEPRECATION")
            char.value = packet
            @Suppress("DEPRECATION")
            char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            @Suppress("DEPRECATION")
            gatt.writeCharacteristic(char)
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
