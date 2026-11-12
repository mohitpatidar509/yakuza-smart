package com.yakuzasmart.app.core.constants

import java.util.UUID

object BleConstants {
    // 16-bit BLE UUID standard expansion base: 0000xxxx-0000-1000-8000-00805F9B34FB
    val SERVICE_UUID: UUID = UUID.fromString("0000FFF0-0000-1000-8000-00805F9B34FB")
    val NOTIFY_CHAR_UUID: UUID = UUID.fromString("0000FFF1-0000-1000-8000-00805F9B34FB")
    val WRITE_CHAR_UUID: UUID = UUID.fromString("0000FFF2-0000-1000-8000-00805F9B34FB")
    val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")

    // Preamble & Framing
    const val SYNC_BYTE_0: Byte = 0x88.toByte()
    const val SYNC_BYTE_1: Byte = 0x01.toByte()

    // Opcodes
    const val OPCODE_QUERY_STATUS: Byte = 0x01.toByte()
    const val OPCODE_STARTUP_MODE: Byte = 0x3E.toByte()
    const val OPCODE_SPEED_REGULATION: Byte = 0x3A.toByte()
    const val OPCODE_VIBRATION_SENSITIVITY: Byte = 0x42.toByte()
    const val OPCODE_CYCLING_MODE: Byte = 0x45.toByte()
    const val OPCODE_SLOPE_PARKING: Byte = 0x51.toByte()
    const val OPCODE_CHILD_MODE: Byte = 0x60.toByte()
    const val OPCODE_WELCOME_MODE: Byte = 0x61.toByte()
    const val OPCODE_QUIET_MODE: Byte = 0x62.toByte()
    const val OPCODE_SMART_HELMET: Byte = 0x70.toByte()
    const val OPCODE_TURN_SIGNAL_TONE: Byte = 0x75.toByte()
    const val OPCODE_VOICE_SETTINGS: Byte = 0x76.toByte()
    const val OPCODE_NFC_COMMAND: Byte = 0x80.toByte()
    const val OPCODE_COMMIT_FLASH_SAVE: Byte = 0xEE.toByte()

    // Timeouts and Limits
    const val SCAN_TIMEOUT_MS = 15_000L
    const val CONNECTION_TIMEOUT_MS = 10_000L
    const val TRANSACTION_ACK_TIMEOUT_MS = 2_500L
    const val TELEMETRY_POLL_INTERVAL_MS = 1_000L
    const val DEFAULT_MTU = 23
    const val TARGET_MTU = 247
}
