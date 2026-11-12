package com.yakuzasmart.app.data.bluetooth

import com.yakuzasmart.app.core.constants.BleConstants
import com.yakuzasmart.app.core.logging.DiagnosticLogger
import com.yakuzasmart.app.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class BleSimulator(
    private val scope: CoroutineScope
) {
    private var telemetryJob: Job? = null

    private val _inboundPackets = MutableSharedFlow<ByteArray>(replay = 0)
    val inboundPackets: SharedFlow<ByteArray> = _inboundPackets.asSharedFlow()

    // Virtual Controller Volatile SRAM State
    private var simulatedSpeedKmh: Float = 0.0f
    private var simulatedBatteryPercent: Int = 85
    private var simulatedVoltageVolts: Float = 54.2f
    private var simulatedCurrentAmps: Float = 0.0f
    private var simulatedTempC: Int = 32

    // Volatile Settings (Reset to defaults upon power cycle)
    private var ramStartupMode: StartupMode = StartupMode.SOFT_START
    private var ramSpeedGear: SpeedRegulationOption = SpeedRegulationOption.SPEED_0
    private var ramVibration: VibrationSensitivity = VibrationSensitivity.MEDIUM
    private var ramCyclingMode: CyclingMode = CyclingMode.COMFORT
    private var ramSlopeParking: SlopeParkingOption = SlopeParkingOption.CLOSE

    fun startTelemetryLoop() {
        telemetryJob?.cancel()
        telemetryJob = scope.launch(Dispatchers.Default) {
            DiagnosticLogger.i("BleSimulator", "Virtual Yakuza Controller Telemetry Loop Started (1 Hz)")
            while (isActive) {
                val frame = buildVirtualTelemetryFrame()
                _inboundPackets.emit(frame)
                delay(BleConstants.TELEMETRY_POLL_INTERVAL_MS)
            }
        }
    }

    fun stopTelemetryLoop() {
        telemetryJob?.cancel()
        telemetryJob = null
        DiagnosticLogger.i("BleSimulator", "Virtual Controller Stopped")
    }

    /**
     * Handles an outbound command written by the app.
     * Emulates controller piezo buzzer beep and returns an ACK frame.
     */
    suspend fun handleOutboundWrite(packet: ByteArray) {
        if (!BleProtocolHandler.validateFrame(packet)) {
            DiagnosticLogger.w("BleSimulator", "Invalid packet received by simulator")
            return
        }

        val opcode = packet[3]
        DiagnosticLogger.i("BleSimulator", "Virtual Controller received Opcode 0x%02X".format(opcode))

        // Emulate controller processing delay (50-100ms)
        delay(80)

        when (opcode) {
            BleConstants.OPCODE_STARTUP_MODE -> {
                val valByte = packet[4]
                ramStartupMode = if (valByte == 0x01.toByte()) StartupMode.HARD_BOOT else StartupMode.SOFT_START
                DiagnosticLogger.i("BleSimulator", "SRAM updated: StartupMode = $ramStartupMode. Piezo Beep Emitted!")
                val ack = BleProtocolHandler.buildPacket(opcode, byteArrayOf(valByte))
                _inboundPackets.emit(ack)
            }
            BleConstants.OPCODE_SPEED_REGULATION -> {
                val valByte = packet[4]
                ramSpeedGear = SpeedRegulationOption.entries.find { it.payloadByte == valByte } ?: SpeedRegulationOption.SPEED_0
                DiagnosticLogger.i("BleSimulator", "SRAM updated: SpeedGear = $ramSpeedGear. Piezo Beep Emitted!")
                val ack = BleProtocolHandler.buildPacket(opcode, byteArrayOf(valByte))
                _inboundPackets.emit(ack)
            }
            BleConstants.OPCODE_VIBRATION_SENSITIVITY -> {
                val valByte = packet[4]
                ramVibration = VibrationSensitivity.entries.find { it.payloadByte == valByte } ?: VibrationSensitivity.MEDIUM
                DiagnosticLogger.i("BleSimulator", "SRAM updated: Vibration = $ramVibration. Piezo Beep Emitted!")
                val ack = BleProtocolHandler.buildPacket(opcode, byteArrayOf(valByte))
                _inboundPackets.emit(ack)
            }
            BleConstants.OPCODE_CYCLING_MODE -> {
                val valByte = packet[4]
                ramCyclingMode = CyclingMode.entries.find { it.payloadByte == valByte } ?: CyclingMode.COMFORT
                DiagnosticLogger.i("BleSimulator", "SRAM updated: CyclingMode = $ramCyclingMode. Piezo Beep Emitted!")
                val ack = BleProtocolHandler.buildPacket(opcode, byteArrayOf(valByte))
                _inboundPackets.emit(ack)
            }
            BleConstants.OPCODE_SLOPE_PARKING -> {
                val valByte = packet[4]
                ramSlopeParking = SlopeParkingOption.entries.find { it.payloadByte == valByte } ?: SlopeParkingOption.CLOSE
                DiagnosticLogger.i("BleSimulator", "SRAM updated: SlopeParking = $ramSlopeParking. Piezo Beep Emitted!")
                val ack = BleProtocolHandler.buildPacket(opcode, byteArrayOf(valByte))
                _inboundPackets.emit(ack)
            }
            BleConstants.OPCODE_QUERY_STATUS -> {
                val frame = buildVirtualTelemetryFrame()
                _inboundPackets.emit(frame)
            }
            else -> {
                DiagnosticLogger.i("BleSimulator", "Generic opcode ACK emitted: 0x%02X".format(opcode))
                val ack = BleProtocolHandler.buildPacket(opcode, byteArrayOf(0x01))
                _inboundPackets.emit(ack)
            }
        }
    }

    /**
     * Simulates the scooter physical power-cycle (Key OFF, wait, Key ON).
     * Demonstrates the volatile RAM reset defect by resetting registers to defaults!
     */
    fun simulatePowerCycle() {
        DiagnosticLogger.w("BleSimulator", "SIMULATED POWER CYCLE TRIGGERED: Cutting power to controller MCU...")
        // Volatile SRAM lost!
        ramStartupMode = StartupMode.SOFT_START
        ramSpeedGear = SpeedRegulationOption.SPEED_0
        ramVibration = VibrationSensitivity.MEDIUM
        ramCyclingMode = CyclingMode.COMFORT
        ramSlopeParking = SlopeParkingOption.CLOSE
        DiagnosticLogger.i("BleSimulator", "Controller MCU rebooted. Volatile registers reverted to factory defaults.")
    }

    private fun buildVirtualTelemetryFrame(): ByteArray {
        val payload = ByteArray(12)
        // Speed in 0.1 km/h
        val spdInt = (simulatedSpeedKmh * 10).toInt()
        payload[0] = ((spdInt shr 8) and 0xFF).toByte()
        payload[1] = (spdInt and 0xFF).toByte()

        // Battery percent
        payload[2] = (simulatedBatteryPercent and 0xFF).toByte()

        // Voltage in 0.1 V
        val voltInt = (simulatedVoltageVolts * 10).toInt()
        payload[3] = ((voltInt shr 8) and 0xFF).toByte()
        payload[4] = (voltInt and 0xFF).toByte()

        // Current in 0.1 A
        val currInt = (simulatedCurrentAmps * 10).toInt()
        payload[5] = (currInt and 0xFF).toByte()

        // Temp with +40 offset
        payload[6] = ((simulatedTempC + 40) and 0xFF).toByte()

        // Fault bits (0 = Normal)
        payload[7] = 0x00.toByte()

        // Current settings
        payload[8] = ramStartupMode.payloadByte
        payload[9] = ramSpeedGear.payloadByte
        payload[10] = ramCyclingMode.payloadByte
        payload[11] = 0x00.toByte() // Reserved

        return BleProtocolHandler.buildPacket(BleConstants.OPCODE_QUERY_STATUS, payload)
    }
}
