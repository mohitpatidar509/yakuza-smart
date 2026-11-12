package com.yakuzasmart.app.domain.model

import com.yakuzasmart.app.core.constants.BleConstants

sealed interface SettingOption {
    val labelEn: String
    val labelHi: String
    val payloadByte: Byte
}

enum class StartupMode(
    override val labelEn: String,
    override val labelHi: String,
    override val payloadByte: Byte
) : SettingOption {
    SOFT_START("Soft Start", "सॉफ्ट स्टार्ट", 0x00.toByte()),
    HARD_BOOT("Hard Boot", "हार्ड बूट", 0x01.toByte())
}

enum class SpeedRegulationOption(
    override val labelEn: String,
    override val labelHi: String,
    override val payloadByte: Byte
) : SettingOption {
    SPEED_0("0 (Default/Neutral)", "0 (डिफ़ॉल्ट)", 0x00.toByte()),
    SPEED_15("15 (15 km/h)", "15 (15 किमी/घंटा)", 0x01.toByte()),
    SPEED_25("25 (25 km/h)", "25 (25 किमी/घंटा)", 0x02.toByte()),
    SPEED_100("100 (100% Unlock)", "100 (100% अनलॉक)", 0x03.toByte())
}

enum class VibrationSensitivity(
    override val labelEn: String,
    override val labelHi: String,
    override val payloadByte: Byte
) : SettingOption {
    LOW("Low", "कम", 0x01.toByte()),
    MEDIUM("Medium", "मध्यम", 0x02.toByte()),
    HIGH("High", "ज्यादा", 0x03.toByte())
}

enum class CyclingMode(
    override val labelEn: String,
    override val labelHi: String,
    override val payloadByte: Byte
) : SettingOption {
    COMFORT("Comfort", "कम्फर्ट", 0x00.toByte()),
    CROSS_COUNTRY("Cross-country", "क्रॉस-कंट्री", 0x01.toByte()),
    CROSS_COUNTRY_PLUS("Cross-country Plus", "क्रॉस-कंट्री प्लस", 0x02.toByte()),
    RACING("Racing", "रेसिंग", 0x03.toByte())
}

enum class SlopeParkingOption(
    override val labelEn: String,
    override val labelHi: String,
    override val payloadByte: Byte
) : SettingOption {
    CLOSE("Close (Disabled)", "बंद (निष्क्रिय)", 0x00.toByte()),
    OPEN("Open (Enabled)", "चालू (सक्रिय)", 0x01.toByte())
}

enum class ToggleOption(
    override val labelEn: String,
    override val labelHi: String,
    override val payloadByte: Byte
) : SettingOption {
    OFF("Off", "बंद", 0x00.toByte()),
    ON("On", "चालू", 0x01.toByte())
}

enum class ProximityDistance(
    override val labelEn: String,
    override val labelHi: String,
    override val payloadByte: Byte
) : SettingOption {
    NEAR("Near", "पास", 0x01.toByte()),
    MIDDLE("Middle", "मध्यम", 0x02.toByte()),
    FAR("Far", "दूर", 0x03.toByte())
}

enum class SettingCategory(
    val titleEn: String,
    val titleHi: String,
    val opcode: Byte,
    val isSafetyCritical: Boolean = false
) {
    STARTUP_MODE("Startup Mode", "स्टार्टअप मोड", BleConstants.OPCODE_STARTUP_MODE),
    SPEED_REGULATION("Speed Regulation", "स्पीड रेगुलेशन", BleConstants.OPCODE_SPEED_REGULATION, true),
    VIBRATION_SENSITIVITY("Vibration Sensitivity", "वाइब्रेशन सेंसिटिविटी", BleConstants.OPCODE_VIBRATION_SENSITIVITY),
    CYCLING_MODE("Cycling Mode", "साइक्लिंग मोड", BleConstants.OPCODE_CYCLING_MODE),
    SLOPE_PARKING("Slope Parking", "स्लोप पार्किंग", BleConstants.OPCODE_SLOPE_PARKING, true),
    SMART_HELMET("Smart Helmet", "स्मार्ट हेलमेट", BleConstants.OPCODE_SMART_HELMET),
    CHILD_MODE("Child Mode", "चाइल्ड मोड", BleConstants.OPCODE_CHILD_MODE, true),
    WELCOME_MODE("Welcome Mode", "वेलकम मोड", BleConstants.OPCODE_WELCOME_MODE),
    QUIET_MODE("Quiet Mode", "क्वाइट मोड", BleConstants.OPCODE_QUIET_MODE),
    TURN_SIGNAL_TONE("Turn Signal Tone", "इंडिकेटर टोन", BleConstants.OPCODE_TURN_SIGNAL_TONE),
    VOICE_SETTINGS("Voice Settings", "वॉइस सेटिंग्स", BleConstants.OPCODE_VOICE_SETTINGS),
    NFC_MANAGEMENT("NFC Card Management", "NFC कार्ड प्रबंधन", BleConstants.OPCODE_NFC_COMMAND, true),
    SAFETY_SYSTEMS("PCS / TCS / ABS", "PCS / TCS / ABS", 0x90.toByte(), true),
    INDUCTION_VOLTAGE("Induction / Voltage", "इंडक्शन / वोल्टेज", 0x85.toByte())
}
