package com.yakuzasmart.app.core.constants

import java.util.UUID

object BleConstants {
    // Standard and Alternative 16-bit / 128-bit BLE Services used in e-scooters
    val SERVICE_UUID_FFF0: UUID = UUID.fromString("0000FFF0-0000-1000-8000-00805F9B34FB")
    val NOTIFY_CHAR_FFF1: UUID = UUID.fromString("0000FFF1-0000-1000-8000-00805F9B34FB")
    val WRITE_CHAR_FFF2: UUID = UUID.fromString("0000FFF2-0000-1000-8000-00805F9B34FB")

    val SERVICE_UUID_FFE0: UUID = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB")
    val CHAR_UUID_FFE1: UUID = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB")

    val SERVICE_UUID_FEE0: UUID = UUID.fromString("0000FEE0-0000-1000-8000-00805F9B34FB")
    val NORDIC_UART_SERVICE: UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
    val NORDIC_UART_RX: UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
    val NORDIC_UART_TX: UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")

    val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")

    // Preamble & Framing
    const val SYNC_BYTE_0: Byte = 0x88.toByte()
    const val SYNC_BYTE_1: Byte = 0x01.toByte()
    const val CMD_TYPE: Byte = 0x00.toByte()
    const val CMD_GROUP_CONTROL: Byte = 0x01.toByte()
    const val CMD_GROUP_ACK: Byte = 0x02.toByte()
    const val CMD_GROUP_NOTIFY: Byte = 0x06.toByte()

    // Scooter Remote Controls (Authentic Opcodes from Yakuza/Tiannian Controller)
    const val OPCODE_AUTH_INFO: Byte = 0x16.toByte()    // Security Authentication handshake
    const val OPCODE_QUERY_INFO: Byte = 0x17.toByte()   // Query bound info
    const val OPCODE_IGNITION: Byte = 0x1B.toByte()    // Start (0x01) / Stop (0x00)
    const val OPCODE_ALERT_LOCK: Byte = 0x1C.toByte()  // Anti-Theft Lock (0x01) / Unlock (0x00)
    const val OPCODE_WHISTLE_FIND: Byte = 0x25.toByte()// Horn / Find Scooter (0x01)
    const val OPCODE_SEAT_CUSHION: Byte = 0x24.toByte()// Seat Lock Release (0x00)
    const val OPCODE_HANDSHAKE: Byte = 0x27.toByte()   // Handshake / Query MAC
    const val OPCODE_DISCONNECT: Byte = 0x12.toByte()  // Disconnect notification

    // Setting Opcodes (Authentic Controller Registers)
    const val OPCODE_STARTUP_MODE: Byte = 0x3E.toByte()         // 0x00 = Soft Start, 0x01 = Hard Boot
    const val OPCODE_SLOPE_PARKING: Byte = 0x3D.toByte()        // 0x00 = Close, 0x01 = Open
    const val OPCODE_SPEED_REGULATION: Byte = 0x3C.toByte()     // 0x00=0, 0x01=15, 0x02=25, 0x03=100
    const val OPCODE_VIBRATION_SENSITIVITY: Byte = 0x3B.toByte()// 0x00=Low, 0x01=Med, 0x02=High
    const val OPCODE_CYCLING_MODE: Byte = 0x33.toByte()         // 0x00=Comfort, 0x01=Routine, 0x02=Sport
    const val OPCODE_WELCOME_MODE: Byte = 0x31.toByte()         // 0x01=On, 0x00=Off
    const val OPCODE_QUIET_MODE: Byte = 0x20.toByte()           // 0x01=On, 0x00=Off
    const val OPCODE_CHILD_MODE: Byte = 0x21.toByte()           // 0x01=On, 0x00=Off
    const val OPCODE_TURN_SIGNAL_TONE: Byte = 0x29.toByte()     // 0x01=On, 0x00=Off
    const val OPCODE_PASSWORD_LOCK: Byte = 0x2A.toByte()        // 0x01=On, 0x00=Off
    const val OPCODE_ELECTRONIC_CLOCK: Byte = 0x2B.toByte()     // 0x01=On, 0x00=Off
    const val OPCODE_SMART_HELMET: Byte = 0x32.toByte()         // 0x01=On, 0x00=Off
    const val OPCODE_START_VOICE: Byte = 0x36.toByte()          // 0x01=On, 0x00=Off
    const val OPCODE_STOP_VOICE: Byte = 0x2E.toByte()           // 0x01=On, 0x00=Off
    const val OPCODE_PROXIMITY_DISTANCE: Byte = 0x35.toByte()   // Distance tuning
    const val OPCODE_VOLTAGE_SETTING: Byte = 0x30.toByte()      // 48V/60V/72V configuration
    const val OPCODE_NFC_MODE: Byte = 0x51.toByte()             // 0x01=Read, 0x00=Exit
    const val OPCODE_CLEAR_CARD: Byte = 0x53.toByte()           // Clear card 1
    const val OPCODE_PCS: Byte = 0x38.toByte()                  // Pre-collision system
    const val OPCODE_ABS: Byte = 0x2D.toByte()                  // Anti-lock braking
    const val OPCODE_TCS: Byte = 0x37.toByte()                  // Traction control
    const val OPCODE_SIDE_STAND: Byte = 0x39.toByte()           // Side stand motor cutoff

    // Inbound Notification Opcodes
    const val OPCODE_NOTIFY_STATUS_1F: Byte = 0x1F.toByte()     // Live speed, battery SOC, ODO, faults
    const val OPCODE_NOTIFY_ELECTRICAL_2F: Byte = 0x2F.toByte() // Current, cell voltage, temps, charging

    // Legacy compatibility opcodes
    const val OPCODE_QUERY_STATUS: Byte = 0x01.toByte()

    // Authentic Security Constants for Handshake & Auth (Opcode 0x16)
    val AUTH_AES_KEY: ByteArray = byteArrayOf(
        0x11, 0x12, 0x13, 0x14,
        0x11, 0x12, 0x13, 0x14,
        0x11, 0x12, 0x13, 0x14,
        0x11, 0x12, 0x13, 0x14
    )
    val AUTH_SALT: ByteArray = byteArrayOf(
        0x0b.toByte(), 0xf2.toByte(), 0xda.toByte(), 0x3e.toByte(),
        0xef.toByte(), 0xf4.toByte(), 0xef.toByte(), 0x82.toByte(),
        0x34.toByte(), 0x03.toByte(), 0xb9.toByte(), 0x97.toByte(),
        0xfa.toByte(), 0x2b.toByte(), 0x5c.toByte(), 0x52.toByte()
    )

    // Timeouts and Limits
    const val SCAN_TIMEOUT_MS = 15_000L
    const val CONNECTION_TIMEOUT_MS = 10_000L
    const val TRANSACTION_ACK_TIMEOUT_MS = 2_500L
    const val TELEMETRY_POLL_INTERVAL_MS = 1_000L
    const val DEFAULT_MTU = 23
    const val TARGET_MTU = 247
}
