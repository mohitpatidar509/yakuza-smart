package com.yakuzasmart.app.data.repository

import com.yakuzasmart.app.core.constants.BleConstants
import com.yakuzasmart.app.core.logging.DiagnosticLogger
import com.yakuzasmart.app.data.bluetooth.BleManager
import com.yakuzasmart.app.data.bluetooth.BleProtocolHandler
import com.yakuzasmart.app.data.bluetooth.BleSimulator
import com.yakuzasmart.app.data.bluetooth.ParsedInboundResult
import com.yakuzasmart.app.data.local.AppPreferences
import com.yakuzasmart.app.domain.model.*
import com.yakuzasmart.app.domain.repository.ScooterRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class ScooterRepositoryImpl(
    private val bleManager: BleManager,
    private val bleSimulator: BleSimulator,
    private val appPreferences: AppPreferences,
    private val scope: CoroutineScope
) : ScooterRepository {

    private val _scooterState = MutableStateFlow(ScooterState())
    override val scooterState: StateFlow<ScooterState> = _scooterState.asStateFlow()

    override val scannedDevices: StateFlow<List<BleDeviceItem>> = bleManager.scannedDevices
    override val isScanning: StateFlow<Boolean> = bleManager.isScanning
    override val isBluetoothEnabled: StateFlow<Boolean> = bleManager.isBluetoothEnabled
    override val scanError: StateFlow<String?> = bleManager.scanError

    private val _savedScooter = MutableStateFlow<BleDeviceItem?>(null)
    override val savedScooter: StateFlow<BleDeviceItem?> = _savedScooter.asStateFlow()

    private val _settingAckEvent = MutableSharedFlow<Pair<Byte, Boolean>>(extraBufferCapacity = 10)
    val settingAckEvent = _settingAckEvent.asSharedFlow()

    private var isSimulatedMode = false
    private var authFallbackJob: Job? = null
    private var keepAliveJob: Job? = null

    init {
        // Collect inbound packets from BleManager
        scope.launch {
            bleManager.inboundPackets.collect { bytes ->
                handleInboundBytes(bytes)
            }
        }

        // Collect inbound packets from BleSimulator
        scope.launch {
            bleSimulator.inboundPackets.collect { bytes ->
                handleInboundBytes(bytes)
            }
        }

        // Monitor BleManager connection status
        scope.launch {
            bleManager.connectionStatus.collect { status ->
                if (!isSimulatedMode) {
                    _scooterState.value = _scooterState.value.copy(connectionStatus = status)
                    if (status == ConnectionStatus.CONNECTED_IDLE) {
                        DiagnosticLogger.i("ScooterRepo", "GATT connected & notify ready. Initiating secure session sequence...")
                        
                        // Send 0x27 handshake
                        delay(150)
                        sendHandshake()

                        // Proactive Auth Fallback: If HandshakeAck doesn't fire within 400ms,
                        // send 0x16 Auth packet to prevent hardware watchdog disconnect!
                        authFallbackJob?.cancel()
                        authFallbackJob = scope.launch {
                            delay(400)
                            if (_scooterState.value.isConnected) {
                                DiagnosticLogger.i("ScooterRepo", "Handshake timeout fallback: Dispatching Auth 0x16 frame to prevent hardware watchdog drop...")
                                val authPacket = BleProtocolHandler.buildAuthPacket()
                                sendRawPacket(authPacket)
                            }
                        }

                        // Periodic Keep-Alive / Watchdog Refresh (Every 2.5s)
                        keepAliveJob?.cancel()
                        keepAliveJob = scope.launch {
                            while (isActive && _scooterState.value.isConnected) {
                                delay(2500)
                                if (_scooterState.value.isConnected) {
                                    DiagnosticLogger.d("ScooterRepo", "Watchdog keep-alive ping (0x27)")
                                    sendHandshake()
                                }
                            }
                        }
                    } else if (status == ConnectionStatus.DISCONNECTED) {
                        authFallbackJob?.cancel()
                        keepAliveJob?.cancel()
                    }
                }
            }
        }

        // Observe saved scooter from DataStore preferences
        scope.launch {
            appPreferences.lastDeviceFlow.collect { (address, name) ->
                if (!address.isNullOrBlank()) {
                    _savedScooter.value = BleDeviceItem(
                        name = name ?: "Yakuza Scooter",
                        address = address,
                        rssi = -60,
                        isBonded = true,
                        isYakuzaDevice = true
                    )
                } else {
                    _savedScooter.value = null
                }
            }
        }
    }

    private fun handleInboundBytes(bytes: ByteArray) {
        when (val result = BleProtocolHandler.parseInboundPacket(bytes, _scooterState.value)) {
            is ParsedInboundResult.TelemetryUpdate -> {
                _scooterState.value = result.state
            }
            is ParsedInboundResult.CommandAck -> {
                DiagnosticLogger.i("ScooterRepo", "Received ACK for ${result.category} -> ${result.confirmedOption.labelEn}")
            }
            is ParsedInboundResult.VehicleControlAck -> {
                DiagnosticLogger.i("ScooterRepo", "Vehicle Control ACK for opcode 0x%02X".format(result.opcode))
            }
            is ParsedInboundResult.HandshakeAck -> {
                DiagnosticLogger.i("ScooterRepo", "Handshake established with controller: MAC=${result.macAddress}. Dispatching Auth 0x16 packet...")
                authFallbackJob?.cancel()
                scope.launch {
                    delay(50)
                    val authPacket = BleProtocolHandler.buildAuthPacket(result.boundMobileBytes)
                    sendRawPacket(authPacket)
                }
            }
            is ParsedInboundResult.AuthAck -> {
                if (result.success) {
                    DiagnosticLogger.i("ScooterRepo", "CONTROLLER AUTHENTICATED SUCCESSFULLY! Connection persistent.")
                } else {
                    DiagnosticLogger.w("ScooterRepo", "Controller auth response non-zero, continuing session.")
                }
            }
            is ParsedInboundResult.SettingAck -> {
                DiagnosticLogger.i("ScooterRepo", "Setting ACK: opcode 0x%02X, success=${result.success}".format(result.opcode))
                _settingAckEvent.tryEmit(Pair(result.opcode, result.success))
            }
            is ParsedInboundResult.GenericAck -> {
                DiagnosticLogger.i("ScooterRepo", "Received generic ACK opcode 0x%02X".format(result.opcode))
            }
            is ParsedInboundResult.CorruptFrame -> {
                DiagnosticLogger.w("ScooterRepo", "Corrupted frame: ${result.reason}")
            }
        }
    }

    fun updateActiveSetting(category: SettingCategory, option: SettingOption) {
        val current = _scooterState.value
        _scooterState.value = when (category) {
            SettingCategory.STARTUP_MODE -> current.copy(activeStartupMode = option as? StartupMode ?: current.activeStartupMode)
            SettingCategory.SPEED_REGULATION -> current.copy(activeSpeedGear = option as? SpeedRegulationOption ?: current.activeSpeedGear)
            SettingCategory.CYCLING_MODE -> current.copy(activeCyclingMode = option as? CyclingMode ?: current.activeCyclingMode)
            SettingCategory.VIBRATION_SENSITIVITY -> current.copy(activeVibrationSensitivity = option as? VibrationSensitivity ?: current.activeVibrationSensitivity)
            SettingCategory.SLOPE_PARKING -> current.copy(activeSlopeParking = (option == SlopeParkingOption.OPEN))
            else -> current
        }
    }

    override suspend fun startScan(): Boolean {
        if (isSimulatedMode) {
            return true
        }
        return bleManager.startScan()
    }

    override fun stopScan() {
        if (!isSimulatedMode) {
            bleManager.stopScan()
        }
    }

    override suspend fun connect(device: BleDeviceItem) {
        if (isSimulatedMode) {
            _scooterState.value = _scooterState.value.copy(
                connectionStatus = ConnectionStatus.CONNECTED_IDLE,
                connectedDevice = device,
                isSimulated = true
            )
            bleSimulator.startTelemetryLoop()
            DiagnosticLogger.i("ScooterRepo", "Connected to Virtual Simulator Controller.")
            return
        }

        bleManager.connect(device)
        appPreferences.saveLastDevice(device.address, device.displayName)
    }

    override suspend fun connectSavedScooter() {
        val saved = _savedScooter.value ?: return
        DiagnosticLogger.i("ScooterRepo", "Connecting to saved scooter: ${saved.displayName} [${saved.address}]")
        connect(saved)
    }

    override suspend fun forgetSavedScooter() {
        disconnect()
        appPreferences.forgetLastDevice()
        _savedScooter.value = null
        DiagnosticLogger.i("ScooterRepo", "Saved scooter forgotten.")
    }

    override suspend fun disconnect() {
        authFallbackJob?.cancel()
        authFallbackJob = null
        keepAliveJob?.cancel()
        keepAliveJob = null

        if (isSimulatedMode) {
            bleSimulator.stopTelemetryLoop()
            _scooterState.value = ScooterState(
                connectionStatus = ConnectionStatus.DISCONNECTED,
                isSimulated = true
            )
            DiagnosticLogger.i("ScooterRepo", "Disconnected from Virtual Simulator.")
            return
        }
        try {
            // Send disconnect opcode 0x12 to controller
            val disconnectPkt = BleProtocolHandler.buildPacket(BleConstants.OPCODE_DISCONNECT, byteArrayOf(0x00))
            sendRawPacket(disconnectPkt)
            delay(100)
        } catch (e: Exception) {
            // Ignore
        }
        bleManager.disconnect()
        _scooterState.value = ScooterState(connectionStatus = ConnectionStatus.DISCONNECTED)
    }

    override fun setSimulatorEnabled(enabled: Boolean) {
        isSimulatedMode = enabled
        _scooterState.value = _scooterState.value.copy(isSimulated = enabled)
        scope.launch { appPreferences.setSimulatorEnabled(enabled) }
    }

    override fun isSimulatorEnabled(): Boolean = isSimulatedMode

    override suspend fun sendQueryAllStatus() {
        sendHandshake()
    }

    private suspend fun sendHandshake() {
        val handshakePacket = BleProtocolHandler.buildHandshakePacket()
        sendRawPacket(handshakePacket)
    }

    override suspend fun setIgnition(powerOn: Boolean): Boolean {
        // Safety interlock: cannot turn off vehicle if moving faster than 0.5 km/h
        val currentSpeed = _scooterState.value.speedKmh ?: 0.0f
        if (!powerOn && currentSpeed > 0.5f) {
            DiagnosticLogger.w("ScooterRepo", "Blocked turning OFF ignition while vehicle is in motion ($currentSpeed km/h)!")
            return false
        }

        DiagnosticLogger.i("ScooterRepo", "Dispatching Ignition Command: powerOn=$powerOn")
        val packet = BleProtocolHandler.buildIgnitionPacket(powerOn)
        sendRawPacket(packet)

        // Optimistically update state, verified by subsequent 0x1F telemetry
        _scooterState.value = _scooterState.value.copy(isIgnitionOn = powerOn)
        return true
    }

    override suspend fun setVehicleLock(lock: Boolean): Boolean {
        val currentSpeed = _scooterState.value.speedKmh ?: 0.0f
        if (lock && currentSpeed > 0.5f) {
            DiagnosticLogger.w("ScooterRepo", "Blocked arming Anti-Theft Lock while in motion ($currentSpeed km/h)!")
            return false
        }

        DiagnosticLogger.i("ScooterRepo", "Dispatching Anti-Theft Lock Command: lock=$lock")
        val packet = BleProtocolHandler.buildLockPacket(lock)
        sendRawPacket(packet)

        _scooterState.value = _scooterState.value.copy(isLocked = lock)
        return true
    }

    override suspend fun findVehicle(): Boolean {
        DiagnosticLogger.i("ScooterRepo", "Dispatching Find Vehicle / Siren Command")
        val packet = BleProtocolHandler.buildFindVehiclePacket()
        sendRawPacket(packet)
        return true
    }

    override suspend fun openSeatCushion(): Boolean {
        DiagnosticLogger.i("ScooterRepo", "Dispatching Open Seat Cushion Command")
        val packet = BleProtocolHandler.buildSeatCushionPacket()
        sendRawPacket(packet)
        return true
    }

    override fun setVehicleModel(model: YakuzaModel) {
        _scooterState.value = _scooterState.value.copy(selectedModel = model)
    }

    suspend fun sendRawPacket(packet: ByteArray) {
        _scooterState.value = _scooterState.value.copy(
            packetsTransmittedCount = _scooterState.value.packetsTransmittedCount + 1
        )
        if (isSimulatedMode) {
            bleSimulator.handleOutboundWrite(packet)
        } else {
            bleManager.writePacket(packet)
        }
    }

    fun triggerSimulatorPowerCycle() {
        if (isSimulatedMode) {
            bleSimulator.simulatePowerCycle()
        }
    }
}
