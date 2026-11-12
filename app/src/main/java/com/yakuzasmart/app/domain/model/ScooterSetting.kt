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
    SOFT_START("Soft Start (Smooth Throttle)", "सॉफ्ट स्टार्ट (आरामदायक गति)", 0x00.toByte()),
    HARD_BOOT("Hard Boot (Instant Torque)", "हार्ड बूट (तुरंत पिकअप)", 0x01.toByte())
}

enum class SpeedRegulationOption(
    override val labelEn: String,
    override val labelHi: String,
    override val payloadByte: Byte
) : SettingOption {
    SPEED_0("0 (Default / Unrestricted)", "0 (डिफ़ॉल्ट / अनियंत्रित)", 0x00.toByte()),
    SPEED_15("15 (15 km/h Eco)", "15 (15 किमी/घंटा इको)", 0x01.toByte()),
    SPEED_25("25 (25 km/h Standard)", "25 (25 किमी/घंटा मानक)", 0x02.toByte()),
    SPEED_100("100 (100% Unlock)", "100 (100% अधिकतम अनलॉक)", 0x03.toByte())
}

enum class VibrationSensitivity(
    override val labelEn: String,
    override val labelHi: String,
    override val payloadByte: Byte
) : SettingOption {
    LOW("Low", "कम (धीमा)", 0x00.toByte()),
    MEDIUM("Medium", "मध्यम (संतुलित)", 0x01.toByte()),
    HIGH("High", "ज्यादा (अत्यधिक संवेदनशील)", 0x02.toByte())
}

enum class CyclingMode(
    override val labelEn: String,
    override val labelHi: String,
    override val payloadByte: Byte
) : SettingOption {
    COMFORT("Energy Saving / Comfort", "कम्फर्ट / ऊर्जा बचत", 0x00.toByte()),
    CROSS_COUNTRY("Routine / Cross-country", "रूटीन / क्रॉस-कंट्री", 0x01.toByte()),
    CROSS_COUNTRY_PLUS("Sport / Cross-country Plus", "स्पोर्ट / अधिकतम शक्ति", 0x02.toByte()),
    RACING("Racing", "रेसिंग", 0x03.toByte())
}

enum class SlopeParkingOption(
    override val labelEn: String,
    override val labelHi: String,
    override val payloadByte: Byte
) : SettingOption {
    CLOSE("Close (Disabled)", "बंद (निष्क्रिय)", 0x00.toByte()),
    OPEN("Open (Hill Hold Enabled)", "चालू (सक्रिय)", 0x01.toByte())
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
    VOICE_SETTINGS("Voice Settings", "वॉइस सेटिंग्स", BleConstants.OPCODE_START_VOICE),
    NFC_MANAGEMENT("NFC Card Management", "NFC कार्ड प्रबंधन", BleConstants.OPCODE_NFC_MODE, true),
    SAFETY_SYSTEMS("PCS / TCS / ABS", "PCS / TCS / ABS", BleConstants.OPCODE_PCS, true),
    INDUCTION_VOLTAGE("Induction / Voltage", "इंडक्शन / वोल्टेज", BleConstants.OPCODE_VOLTAGE_SETTING)
}

enum class YakuzaModel(
    val modelName: String,
    val modelNameHi: String,
    val voltageNominal: String,
    val motorWattage: String,
    val topSpeedKmh: Int,
    val fullChargeRangeKm: Int
) {
    YAKUZA_RUBIE("Yakuza Rubie", "याकुज़ा रूबी", "60V / 72V", "1200W BLDC Hub", 55, 65),
    YAKUZA_KARISMA("Yakuza Karisma", "याकुज़ा करिश्मा", "60V Lithium", "1000W BLDC Hub", 50, 60),
    YAKUZA_DELTA("Yakuza Delta", "याकुज़ा डेल्टा", "60V / 72V Heavy", "1500W High Torque", 50, 70),
    YAKUZA_RAZOR("Yakuza Razor", "याकुज़ा रेज़र", "72V Sport", "2000W High Speed", 65, 80),
    YAKUZA_UNIVERSAL("Yakuza Smart EV", "याकुज़ा स्मार्ट ईवी", "48V / 60V / 72V", "Standard BLDC Controller", 45, 60)
}
