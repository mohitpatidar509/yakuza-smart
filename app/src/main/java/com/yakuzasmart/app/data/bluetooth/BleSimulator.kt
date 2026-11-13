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

    // Virtual Controller State
    private var ramIgnition: Boolean = false
    private var ramLocked: Boolean = false
    private var ramSpeedKmh: Float = 0.0f
    private var ramBatteryPercent: Int = 85
    private var ramVoltageVolts: Float = 63.8f
    private var ramCurrentAmps: Float = 0.0f
    private var ramTempC: Int = 34

    // Volatile Settings
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
                // Alternately emit 0x1F Status frame and 0x2F Electrical frame
                val frame1f = buildVirtualStatus1fFrame()
                _inboundPackets.emit(frame1f)

                delay(500)

                val frame2f = buildVirtualElectrical2fFrame()
                _inboundPackets.emit(frame2f)

                delay(500)
            }
        }
    }

    fun stopTelemetryLoop() {
        telemetryJob?.cancel()
        telemetryJob = null
        DiagnosticLogger.i("BleSimulator", "Virtual Controller Stopped")
    }

    suspend fun handleOutboundWrite(packet: ByteArray) {
        if (!BleProtocolHandler.validateFrame(packet)) {
            DiagnosticLogger.w("BleSimulator", "Invalid packet received by simulator")
            return
        }

        // Layout: [0x88, 0x01, len, cmdType, cmdGroup, opcode, ...payload, chk]
        val isFullFramed = packet.size >= 7 && packet[3] == BleConstants.CMD_TYPE
        val opcode = if (isFullFramed) packet[5] else packet[3]
        val paramArray = if (isFullFramed && packet.size > 7) {
            packet.copyOfRange(7, packet.size - 1)
        } else if (packet.size > 4) {
            packet.copyOfRange(4, packet.size - 1)
        } else {
            byteArrayOf()
        }

        DiagnosticLogger.i("BleSimulator", "Virtual Controller received Opcode 0x%02X".format(opcode))
        delay(60) // Emulate controller processing latency

        when (opcode) {
            BleConstants.OPCODE_HANDSHAKE -> {
                // Echo handshake ACK with virtual MAC: "YK:Z8:60:V1:22:99"
                val macPayload = byteArrayOf(0x06.toByte(), 0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte(), 0xDD.toByte(), 0xEE.toByte(), 0xFF.toByte())
                val ack = BleProtocolHandler.buildPacket(BleConstants.OPCODE_HANDSHAKE, macPayload, cmdGroup = BleConstants.CMD_GROUP_NOTIFY)
                _inboundPackets.emit(ack)
            }
            BleConstants.OPCODE_AUTH_INFO -> {
                DiagnosticLogger.i("BleSimulator", "Virtual Controller Authenticated with 0x16 Auth packet! Security timer reset.")
                val ack = BleProtocolHandler.buildPacket(BleConstants.OPCODE_AUTH_INFO, byteArrayOf(0x01.toByte(), 0x00.toByte()), cmdGroup = BleConstants.CMD_GROUP_ACK)
                _inboundPackets.emit(ack)
            }
            BleConstants.OPCODE_IGNITION -> {
                val isStart = if (paramArray.isNotEmpty()) paramArray[0] == 0x01.toByte() else false
                ramIgnition = isStart
                ramSpeedKmh = if (isStart) 28.0f else 0.0f
                ramCurrentAmps = if (isStart) 4.5f else 0.0f
                DiagnosticLogger.i("BleSimulator", "Virtual Ignition changed: $ramIgnition. Piezo beep emitted!")
                val ack = BleProtocolHandler.buildPacket(opcode, byteArrayOf(if (isStart) 0x01 else 0x00), cmdGroup = BleConstants.CMD_GROUP_NOTIFY)
                _inboundPackets.emit(ack)
            }
            BleConstants.OPCODE_ALERT_LOCK -> {
                val isLock = if (paramArray.isNotEmpty()) paramArray[0] == 0x01.toByte() else false
                ramLocked = isLock
                DiagnosticLogger.i("BleSimulator", "Virtual Anti-Theft Lock: $ramLocked. Horn chime sounded!")
                val ack = BleProtocolHandler.buildPacket(opcode, byteArrayOf(if (isLock) 0x01 else 0x00), cmdGroup = BleConstants.CMD_GROUP_NOTIFY)
                _inboundPackets.emit(ack)
            }
            BleConstants.OPCODE_WHISTLE_FIND -> {
                DiagnosticLogger.i("BleSimulator", "Virtual Siren / Whistle Sounded! *BEEP BEEP BEEP*")
            }
            BleConstants.OPCODE_SEAT_CUSHION -> {
                DiagnosticLogger.i("BleSimulator", "Virtual Seat Solenoid Clicked! Trunk Unlocked.")
            }
            BleConstants.OPCODE_STARTUP_MODE -> {
                val valByte = if (paramArray.isNotEmpty()) paramArray[0] else 0x00.toByte()
                ramStartupMode = if (valByte == 0x01.toByte()) StartupMode.HARD_BOOT else StartupMode.SOFT_START
                DiagnosticLogger.i("BleSimulator", "SRAM updated: StartupMode = $ramStartupMode")
                val ack = BleProtocolHandler.buildPacket(opcode, byteArrayOf(0x01.toByte(), 0x00.toByte()), cmdGroup = BleConstants.CMD_GROUP_ACK)
                _inboundPackets.emit(ack)
            }
            BleConstants.OPCODE_SPEED_REGULATION -> {
                val valByte = if (paramArray.isNotEmpty()) paramArray[0] else 0x00.toByte()
                ramSpeedGear = SpeedRegulationOption.entries.find { it.payloadByte == valByte } ?: SpeedRegulationOption.SPEED_0
                DiagnosticLogger.i("BleSimulator", "SRAM updated: SpeedGear = $ramSpeedGear")
                val ack = BleProtocolHandler.buildPacket(opcode, byteArrayOf(0x01.toByte(), 0x00.toByte()), cmdGroup = BleConstants.CMD_GROUP_ACK)
                _inboundPackets.emit(ack)
            }
            BleConstants.OPCODE_VIBRATION_SENSITIVITY -> {
                val valByte = if (paramArray.isNotEmpty()) paramArray[0] else 0x01.toByte()
                ramVibration = VibrationSensitivity.entries.find { it.payloadByte == valByte } ?: VibrationSensitivity.MEDIUM
                DiagnosticLogger.i("BleSimulator", "SRAM updated: Vibration = $ramVibration")
                val ack = BleProtocolHandler.buildPacket(opcode, byteArrayOf(0x01.toByte(), 0x00.toByte()), cmdGroup = BleConstants.CMD_GROUP_ACK)
                _inboundPackets.emit(ack)
            }
            BleConstants.OPCODE_CYCLING_MODE -> {
                val valByte = if (paramArray.isNotEmpty()) paramArray[0] else 0x00.toByte()
                ramCyclingMode = CyclingMode.entries.find { it.payloadByte == valByte } ?: CyclingMode.COMFORT
                DiagnosticLogger.i("BleSimulator", "SRAM updated: CyclingMode = $ramCyclingMode")
                val ack = BleProtocolHandler.buildPacket(opcode, byteArrayOf(0x01.toByte(), 0x00.toByte()), cmdGroup = BleConstants.CMD_GROUP_ACK)
                _inboundPackets.emit(ack)
            }
            BleConstants.OPCODE_SLOPE_PARKING -> {
                val valByte = if (paramArray.isNotEmpty()) paramArray[0] else 0x00.toByte()
                ramSlopeParking = SlopeParkingOption.entries.find { it.payloadByte == valByte } ?: SlopeParkingOption.CLOSE
                DiagnosticLogger.i("BleSimulator", "SRAM updated: SlopeParking = $ramSlopeParking")
                val ack = BleProtocolHandler.buildPacket(opcode, byteArrayOf(0x01.toByte(), 0x00.toByte()), cmdGroup = BleConstants.CMD_GROUP_ACK)
                _inboundPackets.emit(ack)
            }
            else -> {
                val ack = BleProtocolHandler.buildPacket(opcode, byteArrayOf(0x01), cmdGroup = BleConstants.CMD_GROUP_NOTIFY)
                _inboundPackets.emit(ack)
            }
        }
    }

    fun simulatePowerCycle() {
        DiagnosticLogger.w("BleSimulator", "SIMULATED POWER CYCLE TRIGGERED: Cutting power to controller MCU...")
        ramIgnition = false
        ramSpeedKmh = 0.0f
        ramCurrentAmps = 0.0f
        ramStartupMode = StartupMode.SOFT_START
        ramSpeedGear = SpeedRegulationOption.SPEED_0
        ramVibration = VibrationSensitivity.MEDIUM
        ramCyclingMode = CyclingMode.COMFORT
        ramSlopeParking = SlopeParkingOption.CLOSE
        DiagnosticLogger.i("BleSimulator", "Controller MCU rebooted. Volatile registers reverted to defaults.")
    }

    private fun buildVirtualStatus1fFrame(): ByteArray {
        val p = ByteArray(36)
        p[0] = if (ramIgnition) 0x01.toByte() else 0x00.toByte()
        p[5] = if (ramLocked) 0x01.toByte() else 0x00.toByte()
        p[13] = ramSpeedKmh.toInt().toByte()
        p[16] = ramBatteryPercent.toByte()

        // ODO: 1,450 km (0x00, 0x00, 0x05, 0xAA)
        p[17] = 0x00.toByte()
        p[18] = 0x00.toByte()
        p[19] = 0x05.toByte()
        p[20] = 0xAA.toByte()

        // Trip: 24 km
        p[21] = 0x00.toByte()
        p[22] = 0x00.toByte()
        p[23] = 0x00.toByte()
        p[24] = 0x18.toByte()

        // Faults = 0x00 (Normal)
        p[26] = 0x00.toByte()

        // Cushion lock
        p[28] = 0x00.toByte()

        val payload = ByteArray(1 + p.size)
        payload[0] = p.size.toByte()
        System.arraycopy(p, 0, payload, 1, p.size)

        return BleProtocolHandler.buildPacket(
            opcode = BleConstants.OPCODE_NOTIFY_STATUS_1F,
            payload = payload,
            cmdGroup = BleConstants.CMD_GROUP_NOTIFY
        )
    }

    private fun buildVirtualElectrical2fFrame(): ByteArray {
        val p = ByteArray(22)
        p[9] = ramCyclingMode.payloadByte
        p[10] = (ramCurrentAmps * 10).toInt().toByte()
        p[11] = ramTempC.toByte()
        p[12] = 28.toByte()
        p[13] = (ramTempC + 4).toByte()
        p[15] = 0x00.toByte() // Not charging

        // Min monomer / pack voltage: 63.8 V * 1000 = 63800 = 0xF938
        val voltVal = (ramVoltageVolts * 1000).toInt()
        p[16] = ((voltVal shr 8) and 0xFF).toByte()
        p[17] = (voltVal and 0xFF).toByte()

        val payload = ByteArray(1 + p.size)
        payload[0] = p.size.toByte()
        System.arraycopy(p, 0, payload, 1, p.size)

        return BleProtocolHandler.buildPacket(
            opcode = BleConstants.OPCODE_NOTIFY_ELECTRICAL_2F,
            payload = payload,
            cmdGroup = BleConstants.CMD_GROUP_NOTIFY
        )
    }
}
