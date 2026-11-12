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

    private var isSimulatedMode = false

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
                        sendQueryAllStatus()
                    }
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
            is ParsedInboundResult.GenericAck -> {
                DiagnosticLogger.i("ScooterRepo", "Received generic ACK opcode 0x%02X".format(result.opcode))
            }
            is ParsedInboundResult.CorruptFrame -> {
                DiagnosticLogger.w("ScooterRepo", "Corrupted frame: ${result.reason}")
            }
        }
    }

    override suspend fun startScan() {
        if (isSimulatedMode) {
            // Emulate finding a simulator device
            val mockDevice = BleDeviceItem(
                name = "YAKUZA_SMART_MOCK",
                address = "00:11:22:33:44:55",
                rssi = -55,
                isBonded = false,
                isYakuzaDevice = true
            )
            // Add to simulated scan results if needed
            return
        }
        bleManager.startScan()
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
        appPreferences.saveLastDevice(device.address, device.name)
    }

    override suspend fun disconnect() {
        if (isSimulatedMode) {
            bleSimulator.stopTelemetryLoop()
            _scooterState.value = ScooterState(
                connectionStatus = ConnectionStatus.DISCONNECTED,
                isSimulated = true
            )
            DiagnosticLogger.i("ScooterRepo", "Disconnected from Virtual Simulator.")
            return
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
        val queryPacket = BleProtocolHandler.buildPacket(BleConstants.OPCODE_QUERY_STATUS)
        if (isSimulatedMode) {
            bleSimulator.handleOutboundWrite(queryPacket)
        } else {
            bleManager.writePacket(queryPacket)
        }
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
