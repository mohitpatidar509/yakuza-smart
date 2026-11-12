package com.yakuzasmart.app.presentation.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Permissions : Screen("permissions")
    data object DeviceScan : Screen("device_scan")
    data object DeviceConnection : Screen("device_connection")
    data object Dashboard : Screen("dashboard")
    data object AllSettings : Screen("all_settings")
    data object StartupMode : Screen("startup_mode")
    data object SpeedRegulation : Screen("speed_regulation")
    data object VibrationSensitivity : Screen("vibration_sensitivity")
    data object CyclingMode : Screen("cycling_mode")
    data object SafetySettings : Screen("safety_settings")
    data object SmartFeatures : Screen("smart_features")
    data object SoundSettings : Screen("sound_settings")
    data object NfcManagement : Screen("nfc_management")
    data object VehicleInfo : Screen("vehicle_info")
    data object Diagnostics : Screen("diagnostics")
    data object AppSettings : Screen("app_settings")
    data object LanguageSelection : Screen("language_selection")
    data object About : Screen("about")
}
