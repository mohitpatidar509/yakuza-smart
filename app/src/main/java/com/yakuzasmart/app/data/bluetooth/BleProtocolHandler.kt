package com.yakuzasmart.app.data.bluetooth

import com.yakuzasmart.app.core.constants.BleConstants
import com.yakuzasmart.app.domain.model.*

object BleProtocolHandler {

    /**
     * Builds a standard Yakuza framed command packet:
     * [0x88, 0x01, LEN, OPCODE, ...PAYLOAD, CHECKSUM]
     * LEN = Payload size + 3 (counts LEN, OPCODE, PAYLOAD bytes, CHECKSUM)
     */
    fun buildPacket(opcode: Byte, payload: ByteArray = byteArrayOf()): ByteArray {
        val len = (payload.size + 3).toByte()
        val packetWithoutChecksum = ByteArray(4 + payload.size)
        packetWithoutChecksum[0] = BleConstants.SYNC_BYTE_0
        packetWithoutChecksum[1] = BleConstants.SYNC_BYTE_1
        packetWithoutChecksum[2] = len
        packetWithoutChecksum[3] = opcode
        System.arraycopy(payload, 0, packetWithoutChecksum, 4, payload.size)

        val checksum = calculateChecksum(packetWithoutChecksum)
        val completePacket = ByteArray(packetWithoutChecksum.size + 1)
        System.arraycopy(packetWithoutChecksum, 0, completePacket, 0, packetWithoutChecksum.size)
        completePacket[completePacket.size - 1] = checksum
        return completePacket
    }

    /**
     * Modulo-256 arithmetic sum of all bytes in the buffer.
     */
    fun calculateChecksum(bytes: ByteArray): Byte {
        var sum = 0
        for (b in bytes) {
            sum = (sum + (b.toInt() and 0xFF)) and 0xFF
        }
        return sum.toByte()
    }

    /**
     * Bitwise XOR checksum alternative variant.
     */
    fun calculateXorChecksum(bytes: ByteArray): Byte {
        var xor = 0
        for (b in bytes) {
            xor = xor xor (b.toInt() and 0xFF)
        }
        return xor.toByte()
    }

    /**
     * Validates frame integrity: preamble, declared length, and checksum.
     */
    fun validateFrame(data: ByteArray): Boolean {
        if (data.size < 5) return false
        if (data[0] != BleConstants.SYNC_BYTE_0 || data[1] != BleConstants.SYNC_BYTE_1) return false

        val declaredLen = data[2].toInt() and 0xFF
        if (data.size != declaredLen + 2) return false

        val expectedChecksum = data[data.size - 1]
        val payloadRange = data.copyOfRange(0, data.size - 1)
        val calculatedSum = calculateChecksum(payloadRange)
        val calculatedXor = calculateXorChecksum(payloadRange)

        return expectedChecksum == calculatedSum || expectedChecksum == calculatedXor
    }

    /**
     * Parses an inbound packet into either telemetry or command ACK response.
     */
    fun parseInboundPacket(data: ByteArray, currentState: ScooterState): ParsedInboundResult {
        if (!validateFrame(data)) {
            return ParsedInboundResult.CorruptFrame("Checksum or framing validation failed")
        }

        val opcode = data[3]
        return when (opcode) {
            BleConstants.OPCODE_QUERY_STATUS -> {
                // 16-byte Telemetry Packet
                val state = parseTelemetryFrame(data, currentState)
                ParsedInboundResult.TelemetryUpdate(state)
            }
            BleConstants.OPCODE_STARTUP_MODE -> {
                val valueByte = if (data.size > 5) data[4] else 0x00.toByte()
                val mode = if (valueByte == 0x01.toByte()) StartupMode.HARD_BOOT else StartupMode.SOFT_START
                ParsedInboundResult.CommandAck(SettingCategory.STARTUP_MODE, mode)
            }
            BleConstants.OPCODE_SPEED_REGULATION -> {
                val gearByte = if (data.size > 5) data[4] else 0x00.toByte()
                val gear = SpeedRegulationOption.entries.find { it.payloadByte == gearByte } ?: SpeedRegulationOption.SPEED_0
                ParsedInboundResult.CommandAck(SettingCategory.SPEED_REGULATION, gear)
            }
            BleConstants.OPCODE_VIBRATION_SENSITIVITY -> {
                val vibByte = if (data.size > 5) data[4] else 0x02.toByte()
                val vib = VibrationSensitivity.entries.find { it.payloadByte == vibByte } ?: VibrationSensitivity.MEDIUM
                ParsedInboundResult.CommandAck(SettingCategory.VIBRATION_SENSITIVITY, vib)
            }
            BleConstants.OPCODE_CYCLING_MODE -> {
                val cycleByte = if (data.size > 5) data[4] else 0x00.toByte()
                val cycle = CyclingMode.entries.find { it.payloadByte == cycleByte } ?: CyclingMode.COMFORT
                ParsedInboundResult.CommandAck(SettingCategory.CYCLING_MODE, cycle)
            }
            BleConstants.OPCODE_SLOPE_PARKING -> {
                val optByte = if (data.size > 5) data[4] else 0x00.toByte()
                val opt = SlopeParkingOption.entries.find { it.payloadByte == optByte } ?: SlopeParkingOption.CLOSE
                ParsedInboundResult.CommandAck(SettingCategory.SLOPE_PARKING, opt)
            }
            else -> {
                ParsedInboundResult.GenericAck(opcode)
            }
        }
    }

    private fun parseTelemetryFrame(data: ByteArray, currentState: ScooterState): ScooterState {
        // Expected format:
        // [0x88, 0x01, LEN(0x10), OPCODE(0x01), SpdHi, SpdLo, BatSoc, VoltHi, VoltLo, Curr, Temp, Faults, StartMode, SpeedGear, CycleMode, CHK]
        if (data.size < 15) return currentState

        val speedRaw = ((data[4].toInt() and 0xFF) shl 8) or (data[5].toInt() and 0xFF)
        val speedKmh = speedRaw / 10.0f

        val batPercent = (data[6].toInt() and 0xFF).coerceIn(0, 100)
        val voltageRaw = ((data[7].toInt() and 0xFF) shl 8) or (data[8].toInt() and 0xFF)
        val voltageVolts = voltageRaw / 10.0f

        val currentRaw = (data[9].toInt() and 0xFF)
        val currentAmps = currentRaw / 10.0f

        val tempRaw = (data[10].toInt() and 0xFF) - 40 // offset of 40 deg C

        val faultByte = data[11].toInt() and 0xFF
        val faults = VehicleFaults(
            kickstandDown = (faultByte and 0x01) != 0,
            brakeEngaged = (faultByte and 0x02) != 0,
            throttleError = (faultByte and 0x04) != 0,
            motorHallError = (faultByte and 0x08) != 0,
            overvoltage = (faultByte and 0x10) != 0,
            undervoltage = (faultByte and 0x20) != 0,
            overtemperature = (faultByte and 0x40) != 0,
            alarmTriggered = (faultByte and 0x80) != 0
        )

        val startMode = if (data.size > 12 && data[12] == 0x01.toByte()) StartupMode.HARD_BOOT else StartupMode.SOFT_START
        val speedGear = if (data.size > 13) SpeedRegulationOption.entries.find { it.payloadByte == data[13] } ?: SpeedRegulationOption.SPEED_0 else null
        val cycleMode = if (data.size > 14) CyclingMode.entries.find { it.payloadByte == data[14] } ?: CyclingMode.COMFORT else null

        // Estimated range based on nominal 48V/60V battery capacity
        val estimatedRange = (batPercent * 0.45f).toInt()

        return currentState.copy(
            speedKmh = speedKmh,
            batteryPercent = batPercent,
            batteryVoltageVolts = voltageVolts,
            motorCurrentAmps = currentAmps,
            controllerTempCelsius = tempRaw,
            estimatedRangeKm = estimatedRange,
            faults = faults,
            activeStartupMode = startMode,
            activeSpeedGear = speedGear,
            activeCyclingMode = cycleMode,
            lastSeenTimestamp = System.currentTimeMillis(),
            packetsReceivedCount = currentState.packetsReceivedCount + 1
        )
    }
}

sealed class ParsedInboundResult {
    data class TelemetryUpdate(val state: ScooterState) : ParsedInboundResult()
    data class CommandAck(val category: SettingCategory, val confirmedOption: SettingOption) : ParsedInboundResult()
    data class GenericAck(val opcode: Byte) : ParsedInboundResult()
    data class CorruptFrame(val reason: String) : ParsedInboundResult()
}
