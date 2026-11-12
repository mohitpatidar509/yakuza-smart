package com.yakuzasmart.app

import com.yakuzasmart.app.core.constants.BleConstants
import com.yakuzasmart.app.data.bluetooth.BleProtocolHandler
import com.yakuzasmart.app.data.bluetooth.ParsedInboundResult
import com.yakuzasmart.app.domain.model.ScooterState
import com.yakuzasmart.app.domain.model.StartupMode
import org.junit.Assert.*
import org.junit.Test

class BleProtocolHandlerTest {

    @Test
    fun testBuildStartupModePacket() {
        val packet = BleProtocolHandler.buildPacket(
            BleConstants.OPCODE_STARTUP_MODE,
            byteArrayOf(StartupMode.HARD_BOOT.payloadByte)
        )

        // Expected: [0x88, 0x01, LEN(4), 0x3E, 0x01, CHECKSUM]
        assertEquals(BleConstants.SYNC_BYTE_0, packet[0])
        assertEquals(BleConstants.SYNC_BYTE_1, packet[1])
        assertEquals(4.toByte(), packet[2]) // len: payload + 3
        assertEquals(BleConstants.OPCODE_STARTUP_MODE, packet[3])
        assertEquals(0x01.toByte(), packet[4])

        // Verify checksum
        val expectedSum = ((0x88 and 0xFF) + (0x01 and 0xFF) + 4 + (0x3E and 0xFF) + 1) and 0xFF
        assertEquals(expectedSum.toByte(), packet[5])
    }

    @Test
    fun testValidateFrame_ValidAndCorrupt() {
        val validPacket = BleProtocolHandler.buildPacket(
            BleConstants.OPCODE_STARTUP_MODE,
            byteArrayOf(0x00.toByte())
        )
        assertTrue(BleProtocolHandler.validateFrame(validPacket))

        // Corrupt checksum
        val corruptPacket = validPacket.clone()
        corruptPacket[corruptPacket.size - 1] = (corruptPacket[corruptPacket.size - 1] + 1).toByte()
        assertFalse(BleProtocolHandler.validateFrame(corruptPacket))

        // Truncated packet
        val truncated = validPacket.copyOfRange(0, 3)
        assertFalse(BleProtocolHandler.validateFrame(truncated))

        // Wrong preamble
        val wrongPreamble = validPacket.clone()
        wrongPreamble[0] = 0x00.toByte()
        assertFalse(BleProtocolHandler.validateFrame(wrongPreamble))
    }

    @Test
    fun testParseInboundTelemetryFrame() {
        // Build 16-byte telemetry frame
        // [0x88, 0x01, LEN(0x10), OPCODE(0x01), SpdHi(0x01), SpdLo(0x2C) -> 300 = 30.0km/h,
        //  BatSoc(82%), VoltHi(0x02), VoltLo(0x58) -> 600 = 60.0V, Curr(50 = 5.0A),
        //  Temp(72 -> 32 C), Faults(0x01 -> Kickstand), StartMode(0x01 -> Hard), Gear(0x02 -> 25), Cycle(0x03 -> Racing), CHK]
        val payload = byteArrayOf(
            0x01.toByte(), 0x2C.toByte(), // 30.0 km/h
            82.toByte(),                  // 82%
            0x02.toByte(), 0x58.toByte(), // 60.0 V
            50.toByte(),                  // 5.0 A
            72.toByte(),                  // 32 C (72 - 40)
            0x01.toByte(),                // Kickstand down
            0x01.toByte(),                // Hard Boot
            0x02.toByte(),                // 25 km/h gear
            0x03.toByte()                 // Racing mode
        )

        val packet = BleProtocolHandler.buildPacket(BleConstants.OPCODE_QUERY_STATUS, payload)
        val initial = ScooterState()
        val result = BleProtocolHandler.parseInboundPacket(packet, initial)

        assertTrue(result is ParsedInboundResult.TelemetryUpdate)
        val state = (result as ParsedInboundResult.TelemetryUpdate).state
        assertEquals(30.0f, state.speedKmh ?: 0.0f, 0.01f)
        assertEquals(82, state.batteryPercent)
        assertEquals(60.0f, state.batteryVoltageVolts ?: 0.0f, 0.01f)
        assertEquals(5.0f, state.motorCurrentAmps ?: 0.0f, 0.01f)
        assertEquals(32, state.controllerTempCelsius)
        assertTrue(state.faults.kickstandDown)
        assertFalse(state.faults.brakeEngaged)
        assertEquals(StartupMode.HARD_BOOT, state.activeStartupMode)
    }

    @Test
    fun testParseCommandAck() {
        val ackPacket = BleProtocolHandler.buildPacket(
            BleConstants.OPCODE_STARTUP_MODE,
            byteArrayOf(0x01.toByte())
        )
        val result = BleProtocolHandler.parseInboundPacket(ackPacket, ScooterState())
        assertTrue(result is ParsedInboundResult.CommandAck)
        val cmdAck = result as ParsedInboundResult.CommandAck
        assertEquals(StartupMode.HARD_BOOT, cmdAck.confirmedOption)
    }
}
