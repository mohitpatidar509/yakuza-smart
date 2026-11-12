package com.yakuzasmart.app.data.bluetooth

import com.yakuzasmart.app.core.constants.BleConstants
import com.yakuzasmart.app.core.logging.DiagnosticLogger
import com.yakuzasmart.app.domain.model.*

sealed interface ParsedInboundResult {
    data class TelemetryUpdate(val state: ScooterState) : ParsedInboundResult
    data class CommandAck(val category: SettingCategory, val confirmedOption: SettingOption) : ParsedInboundResult
    data class VehicleControlAck(val opcode: Byte, val stateByte: Byte) : ParsedInboundResult
    data class HandshakeAck(val macAddress: String) : ParsedInboundResult
    data class GenericAck(val opcode: Byte) : ParsedInboundResult
    data class CorruptFrame(val reason: String) : ParsedInboundResult
}

object BleProtocolHandler {

    /**
     * Builds the authentic Yakuza / Tiannian framed packet:
     * [0x88, 0x01, LEN, CMD_TYPE, CMD_GROUP, OPCODE, ...PAYLOAD, CHECKSUM]
     * LEN = Payload size + 4 (counts CMD_TYPE, CMD_GROUP, OPCODE, PAYLOAD bytes, CHECKSUM)
     */
    fun buildPacket(
        opcode: Byte,
        payload: ByteArray = byteArrayOf(),
        cmdType: Byte = BleConstants.CMD_TYPE,
        cmdGroup: Byte = BleConstants.CMD_GROUP_CONTROL
    ): ByteArray {
        val totalLength = payload.size + 4 // LEN byte value
        val packetWithoutChecksum = ByteArray(3 + 3 + payload.size)
        packetWithoutChecksum[0] = BleConstants.SYNC_BYTE_0
        packetWithoutChecksum[1] = BleConstants.SYNC_BYTE_1
        packetWithoutChecksum[2] = totalLength.toByte()
        packetWithoutChecksum[3] = cmdType
        packetWithoutChecksum[4] = cmdGroup
        packetWithoutChecksum[5] = opcode
        if (payload.isNotEmpty()) {
            System.arraycopy(payload, 0, packetWithoutChecksum, 6, payload.size)
        }

        val checksum = calculateChecksum(packetWithoutChecksum)
        val completePacket = ByteArray(packetWithoutChecksum.size + 1)
        System.arraycopy(packetWithoutChecksum, 0, completePacket, 0, packetWithoutChecksum.size)
        completePacket[completePacket.size - 1] = checksum
        return completePacket
    }

    /**
     * Ignition Control: Start (ON) / Stop (OFF)
     * e = ["00","01","1B","01", start?"01":"00", "00", "00"]
     */
    fun buildIgnitionPacket(start: Boolean): ByteArray {
        val payload = byteArrayOf(0x01.toByte(), if (start) 0x01.toByte() else 0x00.toByte(), 0x00.toByte(), 0x00.toByte())
        return buildPacket(BleConstants.OPCODE_IGNITION, payload)
    }

    /**
     * Anti-Theft Lock / Alert Guard: Lock (Armed) / Unlock (Disarmed)
     * e = ["00","01","1C","03", lock?"01":"00", "00", "00"]
     */
    fun buildLockPacket(lock: Boolean): ByteArray {
        val payload = byteArrayOf(0x03.toByte(), if (lock) 0x01.toByte() else 0x00.toByte(), 0x00.toByte(), 0x00.toByte())
        return buildPacket(BleConstants.OPCODE_ALERT_LOCK, payload)
    }

    /**
     * Find Scooter / Siren / Whistle
     * e = ["00","01","25","01","01"]
     */
    fun buildFindVehiclePacket(): ByteArray {
        val payload = byteArrayOf(0x01.toByte(), 0x01.toByte())
        return buildPacket(BleConstants.OPCODE_WHISTLE_FIND, payload)
    }

    /**
     * Open Seat Cushion / Electronic Trunk Release
     * e = ["00","01","24","01","00"]
     */
    fun buildSeatCushionPacket(): ByteArray {
        val payload = byteArrayOf(0x01.toByte(), 0x00.toByte())
        return buildPacket(BleConstants.OPCODE_SEAT_CUSHION, payload)
    }

    /**
     * Connection Handshake / Query Controller
     * e = ["00","01","27","00"]
     */
    fun buildHandshakePacket(): ByteArray {
        val payload = byteArrayOf(0x00.toByte())
        return buildPacket(BleConstants.OPCODE_HANDSHAKE, payload)
    }

    /**
     * Parameter Setting Packet
     * e = ["00","01", opcode, "01", val]
     */
    fun buildSettingPacket(opcode: Byte, optionValue: Byte): ByteArray {
        val payload = byteArrayOf(0x01.toByte(), optionValue)
        return buildPacket(opcode, payload)
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
     * Bitwise XOR checksum variant.
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
        // Packet size must be declaredLen + 2 (Header 2 bytes + LEN bytes)
        if (data.size != declaredLen + 2 && data.size != declaredLen + 3) {
            // Some controllers omit counting checksum in declaredLen
            if (data.size < declaredLen) return false
        }

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

        // Layout:
        // data[0]=0x88, data[1]=0x01, data[2]=LEN
        // In full framing:
        // data[3]=cmdType, data[4]=cmdGroup, data[5]=cmdOpcode, data[6]=paramLen, data[7..N-2]=paramArray, data[N-1]=chk
        val isFullFramed = data.size >= 7 && data[3] == BleConstants.CMD_TYPE

        val opcode = if (isFullFramed) data[5] else data[3]
        val paramArray = if (isFullFramed && data.size > 7) {
            data.copyOfRange(7, data.size - 1)
        } else if (data.size > 4) {
            data.copyOfRange(4, data.size - 1)
        } else {
            byteArrayOf()
        }

        DiagnosticLogger.i("BleProtocolHandler", "Parsed Inbound Opcode: 0x%02X, paramSize=%d".format(opcode, paramArray.size))

        return when (opcode) {
            BleConstants.OPCODE_NOTIFY_STATUS_1F -> {
                // Primary Telemetry Stream (0x1F)
                val updatedState = parseStatus1fFrame(paramArray, currentState)
                ParsedInboundResult.TelemetryUpdate(updatedState)
            }
            BleConstants.OPCODE_NOTIFY_ELECTRICAL_2F -> {
                // Secondary Electrical Telemetry Stream (0x2F)
                val updatedState = parseElectrical2fFrame(paramArray, currentState)
                ParsedInboundResult.TelemetryUpdate(updatedState)
            }
            BleConstants.OPCODE_HANDSHAKE -> {
                // Controller response to 0x27 handshake (Contains MAC address in first 6 bytes)
                val mac = if (paramArray.size >= 6) {
                    paramArray.take(6).joinToString(":") { "%02X".format(it) }
                } else {
                    "CONNECTED"
                }
                DiagnosticLogger.i("BleProtocolHandler", "Handshake ACK received: Controller MAC=$mac")
                ParsedInboundResult.HandshakeAck(mac)
            }
            BleConstants.OPCODE_IGNITION -> {
                val stateByte = if (paramArray.isNotEmpty()) paramArray[0] else 0x00.toByte()
                val isStarted = stateByte == 0x01.toByte()
                val updated = currentState.copy(isIgnitionOn = isStarted)
                ParsedInboundResult.TelemetryUpdate(updated)
            }
            BleConstants.OPCODE_ALERT_LOCK -> {
                val stateByte = if (paramArray.isNotEmpty()) paramArray[0] else 0x00.toByte()
                val isLocked = stateByte == 0x01.toByte()
                val updated = currentState.copy(isLocked = isLocked)
                ParsedInboundResult.TelemetryUpdate(updated)
            }
            BleConstants.OPCODE_STARTUP_MODE -> {
                val valByte = if (paramArray.isNotEmpty()) paramArray[0] else 0x00.toByte()
                val mode = if (valByte == 0x01.toByte()) StartupMode.HARD_BOOT else StartupMode.SOFT_START
                val updated = currentState.copy(activeStartupMode = mode)
                ParsedInboundResult.CommandAck(SettingCategory.STARTUP_MODE, mode)
            }
            BleConstants.OPCODE_SPEED_REGULATION -> {
                val valByte = if (paramArray.isNotEmpty()) paramArray[0] else 0x00.toByte()
                val gear = SpeedRegulationOption.entries.find { it.payloadByte == valByte } ?: SpeedRegulationOption.SPEED_0
                val updated = currentState.copy(activeSpeedGear = gear)
                ParsedInboundResult.CommandAck(SettingCategory.SPEED_REGULATION, gear)
            }
            BleConstants.OPCODE_VIBRATION_SENSITIVITY -> {
                val valByte = if (paramArray.isNotEmpty()) paramArray[0] else 0x01.toByte()
                val vib = VibrationSensitivity.entries.find { it.payloadByte == valByte } ?: VibrationSensitivity.MEDIUM
                val updated = currentState.copy(activeVibrationSensitivity = vib)
                ParsedInboundResult.CommandAck(SettingCategory.VIBRATION_SENSITIVITY, vib)
            }
            BleConstants.OPCODE_CYCLING_MODE -> {
                val valByte = if (paramArray.isNotEmpty()) paramArray[0] else 0x00.toByte()
                val cycle = CyclingMode.entries.find { it.payloadByte == valByte } ?: CyclingMode.COMFORT
                val updated = currentState.copy(activeCyclingMode = cycle)
                ParsedInboundResult.CommandAck(SettingCategory.CYCLING_MODE, cycle)
            }
            BleConstants.OPCODE_SLOPE_PARKING -> {
                val valByte = if (paramArray.isNotEmpty()) paramArray[0] else 0x00.toByte()
                val opt = SlopeParkingOption.entries.find { it.payloadByte == valByte } ?: SlopeParkingOption.CLOSE
                val updated = currentState.copy(activeSlopeParking = opt == SlopeParkingOption.OPEN)
                ParsedInboundResult.CommandAck(SettingCategory.SLOPE_PARKING, opt)
            }
            BleConstants.OPCODE_QUERY_STATUS -> {
                // Fallback legacy 16-byte frame
                val state = parseLegacyTelemetryFrame(data, currentState)
                ParsedInboundResult.TelemetryUpdate(state)
            }
            else -> {
                ParsedInboundResult.GenericAck(opcode)
            }
        }
    }

    /**
     * Parses the authentic 0x1F Status Telemetry packet:
     * paramArray layout:
     * [0] = started (0x01 = started / power ON)
     * [5] = alertOn (0x01 = locked / alarm armed)
     * [13] = speed in km/h
     * [16] = battery SOC percentage (0-100)
     * [17..20] = total mileage ODO in km
     * [21..24] = each trip mileage in km
     * [26] = fault bitmask
     * [28] = cushionLockOn
     * [32] = muteOn
     * [33] = childOn
     */
    private fun parseStatus1fFrame(p: ByteArray, current: ScooterState): ScooterState {
        if (p.size < 17) return current

        val started = (p[0] == 0x01.toByte())
        val alertOn = if (p.size > 5) (p[5] == 0x01.toByte()) else current.isLocked
        val speedKmh = if (p.size > 13) (p[13].toInt() and 0xFF).toFloat() else (current.speedKmh ?: 0.0f)
        val batSoc = (p[16].toInt() and 0xFF).coerceIn(0, 100)

        var odoKm: Float? = current.totalMileageKm
        if (p.size >= 21) {
            val rawOdo = ((p[17].toLong() and 0xFF) shl 24) or
                    ((p[18].toLong() and 0xFF) shl 16) or
                    ((p[19].toLong() and 0xFF) shl 8) or
                    (p[20].toLong() and 0xFF)
            odoKm = rawOdo.toFloat()
        }

        var tripKm: Float? = current.tripMileageKm
        if (p.size >= 25) {
            val rawTrip = ((p[21].toLong() and 0xFF) shl 24) or
                    ((p[22].toLong() and 0xFF) shl 16) or
                    ((p[23].toLong() and 0xFF) shl 8) or
                    (p[24].toLong() and 0xFF)
            tripKm = rawTrip.toFloat()
        }

        val faults = if (p.size >= 27) {
            val fb = p[26].toInt() and 0xFF
            VehicleFaults(
                kickstandDown = (fb and 0x01) != 0,
                brakeEngaged = (fb and 0x02) != 0,
                controllerError = (fb and 0x04) != 0,
                motorHallError = (fb and 0x08) != 0,
                undervoltage = (fb and 0x10) != 0,
                throttleError = (fb and 0x20) != 0,
                alarmTriggered = alertOn
            )
        } else {
            current.faults
        }

        val cushionLock = if (p.size > 28) (p[28] == 0x01.toByte()) else current.isCushionOpen
        val mute = if (p.size > 32) (p[32] == 0x01.toByte()) else current.isMuteOn
        val child = if (p.size > 33) (p[33] == 0x01.toByte()) else current.isChildModeOn

        // Dynamic estimated range based on real battery SOC and model capacity
        val modelMaxRange = current.selectedModel.fullChargeRangeKm
        val estimatedRange = (batSoc * (modelMaxRange / 100.0f)).toInt()

        return current.copy(
            isIgnitionOn = started,
            isLocked = alertOn,
            speedKmh = speedKmh,
            batteryPercent = batSoc,
            totalMileageKm = odoKm,
            tripMileageKm = tripKm,
            isCushionOpen = cushionLock,
            isMuteOn = mute,
            isChildModeOn = child,
            estimatedRangeKm = estimatedRange,
            faults = faults,
            packetsReceivedCount = current.packetsReceivedCount + 1,
            lastSeenTimestamp = System.currentTimeMillis()
        )
    }

    /**
     * Parses the authentic 0x2F Electrical Telemetry packet:
     * [9] = cyclingMode
     * [10] = current * 0.1 A
     * [11] = maxTemperature °C
     * [12] = minTemperature °C
     * [13] = mosTemperature °C
     * [15] = charging (0x01 = charging)
     * [16..17] = minMonomer / battery pack voltage * 0.001 V
     */
    private fun parseElectrical2fFrame(p: ByteArray, current: ScooterState): ScooterState {
        if (p.size < 18) return current

        val currentAmps = (p[10].toInt() and 0xFF) * 0.1f
        val maxTemp = p[11].toInt() and 0xFF
        val mosTemp = p[13].toInt() and 0xFF
        val charging = (p[15] == 0x01.toByte())
        val rawVolt = ((p[16].toInt() and 0xFF) * 256) + (p[17].toInt() and 0xFF)
        val packVoltage = if (rawVolt > 20000) rawVolt * 0.001f else if (rawVolt > 400) rawVolt * 0.1f else current.batteryVoltageVolts

        val cycleModeByte = p[9]
        val cycleMode = CyclingMode.entries.find { it.payloadByte == cycleModeByte } ?: current.activeCyclingMode

        return current.copy(
            motorCurrentAmps = currentAmps,
            controllerTempCelsius = mosTemp.coerceAtLeast(maxTemp),
            isCharging = charging,
            batteryVoltageVolts = packVoltage,
            activeCyclingMode = cycleMode,
            packetsReceivedCount = current.packetsReceivedCount + 1,
            lastSeenTimestamp = System.currentTimeMillis()
        )
    }

    private fun parseLegacyTelemetryFrame(data: ByteArray, currentState: ScooterState): ScooterState {
        if (data.size < 15) return currentState

        val speedRaw = ((data[4].toInt() and 0xFF) shl 8) or (data[5].toInt() and 0xFF)
        val speedKmh = speedRaw / 10.0f
        val batPercent = (data[6].toInt() and 0xFF).coerceIn(0, 100)
        val voltageRaw = ((data[7].toInt() and 0xFF) shl 8) or (data[8].toInt() and 0xFF)
        val voltageVolts = voltageRaw / 10.0f
        val currentRaw = (data[9].toInt() and 0xFF)
        val currentAmps = currentRaw / 10.0f
        val tempRaw = (data[10].toInt() and 0xFF) - 40

        val estimatedRange = (batPercent * (currentState.selectedModel.fullChargeRangeKm / 100.0f)).toInt()

        return currentState.copy(
            speedKmh = speedKmh,
            batteryPercent = batPercent,
            batteryVoltageVolts = voltageVolts,
            motorCurrentAmps = currentAmps,
            controllerTempCelsius = tempRaw,
            estimatedRangeKm = estimatedRange,
            packetsReceivedCount = currentState.packetsReceivedCount + 1,
            lastSeenTimestamp = System.currentTimeMillis()
        )
    }
}
