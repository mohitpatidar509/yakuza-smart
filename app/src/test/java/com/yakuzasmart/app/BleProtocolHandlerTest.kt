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

        // Expected: [0x88, 0x01, LEN(5), CMD_TYPE(0), CMD_GRP(1), OPCODE(0x3E), VAL(0x01), CHECKSUM]
        assertEquals(BleConstants.SYNC_BYTE_0, packet[0])
        assertEquals(BleConstants.SYNC_BYTE_1, packet[1])
        assertEquals(5.toByte(), packet[2]) // len: payload(1) + 4
        assertEquals(BleConstants.CMD_TYPE, packet[3])
        assertEquals(BleConstants.CMD_GROUP_CONTROL, packet[4])
        assertEquals(BleConstants.OPCODE_STARTUP_MODE, packet[5])
        assertEquals(0x01.toByte(), packet[6])
        assertTrue(BleProtocolHandler.validateFrame(packet))
    }

    @Test
    fun testBuildAuthPacket() {
        val authPacket = BleProtocolHandler.buildAuthPacket(byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06))

        // Total 47 bytes: 88 01 2C 00 01 16 27 [16B salt] [16B token] [7B timestamp] CHK
        assertEquals(47, authPacket.size)
        assertEquals(BleConstants.SYNC_BYTE_0, authPacket[0])
        assertEquals(BleConstants.SYNC_BYTE_1, authPacket[1])
        assertEquals(0x2C.toByte(), authPacket[2]) // 44 bytes length
        assertEquals(BleConstants.CMD_TYPE, authPacket[3])
        assertEquals(BleConstants.CMD_GROUP_CONTROL, authPacket[4])
        assertEquals(BleConstants.OPCODE_AUTH_INFO, authPacket[5])
        assertEquals(0x27.toByte(), authPacket[6]) // param length = 39
        assertTrue(BleProtocolHandler.validateFrame(authPacket))
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
    fun testParseInboundHandshakeAck() {
        // Handshake packet from controller: [88, 01, LEN, 00, 06, 27, 0C, MAC(6B), BIND(6B), CHK]
        val macBytes = byteArrayOf(0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte(), 0xDD.toByte(), 0xEE.toByte(), 0xFF.toByte())
        val bindBytes = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06)
        val payload = byteArrayOf(0x0C.toByte()) + macBytes + bindBytes
        val packet = BleProtocolHandler.buildPacket(BleConstants.OPCODE_HANDSHAKE, payload, cmdGroup = BleConstants.CMD_GROUP_NOTIFY)

        val result = BleProtocolHandler.parseInboundPacket(packet, ScooterState())
        assertTrue(result is ParsedInboundResult.HandshakeAck)
        val hs = result as ParsedInboundResult.HandshakeAck
        assertEquals("AA:BB:CC:DD:EE:FF", hs.macAddress)
        assertArrayEquals(bindBytes, hs.boundMobileBytes)
    }

    @Test
    fun testParseSettingAck() {
        // Setting ACK from controller: [88, 01, LEN, 00, 02, 3E, 01, 00, CHK]
        val payload = byteArrayOf(0x01.toByte(), 0x00.toByte()) // 0x00 = success
        val packet = BleProtocolHandler.buildPacket(BleConstants.OPCODE_STARTUP_MODE, payload, cmdGroup = BleConstants.CMD_GROUP_ACK)

        val result = BleProtocolHandler.parseInboundPacket(packet, ScooterState())
        assertTrue(result is ParsedInboundResult.SettingAck)
        val ack = result as ParsedInboundResult.SettingAck
        assertEquals(BleConstants.OPCODE_STARTUP_MODE, ack.opcode)
        assertTrue(ack.success)
    }
}
