package com.yakuzasmart.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yakuzasmart.app.domain.model.SettingCategory
import com.yakuzasmart.app.presentation.screens.*
import com.yakuzasmart.app.presentation.viewmodel.ScooterViewModel

@Composable
fun YakuzaNavGraph(
    viewModel: ScooterViewModel,
    navController: NavHostController = rememberNavController()
) {
    val language by viewModel.language.collectAsState()
    val scooterState by viewModel.scooterState.collectAsState()
    val scannedDevices by viewModel.scannedDevices.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val settingRecords by viewModel.settingRecords.collectAsState()
    val persistenceRecords by viewModel.persistenceRecords.collectAsState()
    val lastResult by viewModel.lastTransactionResult.collectAsState()
    val isExecuting by viewModel.isExecutingTransaction.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                language = language,
                onLanguageToggle = { viewModel.toggleLanguage() },
                onGetStarted = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Permissions.route) {
            BluetoothPermissionScreen(
                language = language,
                onLanguageToggle = { viewModel.toggleLanguage() },
                onGrantPermissions = {
                    navController.navigate(Screen.DeviceScan.route)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DeviceScan.route) {
            DeviceScanScreen(
                scannedDevices = scannedDevices,
                isScanning = isScanning,
                language = language,
                onLanguageToggle = { viewModel.toggleLanguage() },
                onStartScan = { viewModel.startScan() },
                onStopScan = { viewModel.stopScan() },
                onSelectDevice = { device ->
                    viewModel.connectDevice(device)
                    navController.navigate(Screen.DeviceConnection.route)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DeviceConnection.route) {
            DeviceConnectionScreen(
                state = scooterState,
                language = language,
                onLanguageToggle = { viewModel.toggleLanguage() },
                onConnectedSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.DeviceConnection.route) { inclusive = true }
                    }
                },
                onCancel = {
                    viewModel.disconnect()
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                state = scooterState,
                language = language,
                onLanguageToggle = { viewModel.toggleLanguage() },
                onNavigateToScan = { navController.navigate(Screen.DeviceScan.route) },
                onDisconnect = { viewModel.disconnect() },
                onNavigateToSettings = { navController.navigate(Screen.AllSettings.route) },
                onNavigateToNfc = { navController.navigate(Screen.NfcManagement.route) },
                onNavigateToDiagnostics = { navController.navigate(Screen.Diagnostics.route) },
                onNavigateToAppSettings = { navController.navigate(Screen.AppSettings.route) },
                onNavigateToVehicleInfo = { navController.navigate(Screen.VehicleInfo.route) }
            )
        }

        composable(Screen.AllSettings.route) {
            AllVehicleSettingsScreen(
                records = settingRecords,
                language = language,
                onLanguageToggle = { viewModel.toggleLanguage() },
                onNavigateToStartupMode = { navController.navigate(Screen.StartupMode.route) },
                onNavigateToSpeedRegulation = { navController.navigate(Screen.SpeedRegulation.route) },
                onNavigateToVibration = { navController.navigate(Screen.VibrationSensitivity.route) },
                onNavigateToCyclingMode = { navController.navigate(Screen.CyclingMode.route) },
                onNavigateToSafety = { navController.navigate(Screen.SafetySettings.route) },
                onNavigateToSmartFeatures = { navController.navigate(Screen.SmartFeatures.route) },
                onNavigateToSound = { navController.navigate(Screen.SoundSettings.route) },
                onNavigateToNfc = { navController.navigate(Screen.NfcManagement.route) },
                onNavigateToVehicleInfo = { navController.navigate(Screen.VehicleInfo.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.StartupMode.route) {
            StartupModeScreen(
                currentRecord = settingRecords[SettingCategory.STARTUP_MODE],
                lastResult = lastResult,
                isExecuting = isExecuting,
                language = language,
                onLanguageToggle = { viewModel.toggleLanguage() },
                onApplySetting = { mode -> viewModel.updateSetting(SettingCategory.STARTUP_MODE, mode) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SpeedRegulation.route) {
            SpeedRegulationScreen(
                currentRecord = settingRecords[SettingCategory.SPEED_REGULATION],
                lastResult = lastResult,
                isExecuting = isExecuting,
                currentSpeedKmh = scooterState.speedKmh ?: 0.0f,
                language = language,
                onLanguageToggle = { viewModel.toggleLanguage() },
                onApplySetting = { gear -> viewModel.updateSetting(SettingCategory.SPEED_REGULATION, gear) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.VibrationSensitivity.route) {
            VibrationSensitivityScreen(
                currentRecord = settingRecords[SettingCategory.VIBRATION_SENSITIVITY],
                lastResult = lastResult,
                isExecuting = isExecuting,
                language = language,
                onLanguageToggle = { viewModel.toggleLanguage() },
                onApplySetting = { sens -> viewModel.updateSetting(SettingCategory.VIBRATION_SENSITIVITY, sens) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CyclingMode.route) {
            CyclingModeScreen(
                currentRecord = settingRecords[SettingCategory.CYCLING_MODE],
                lastResult = lastResult,
                isExecuting = isExecuting,
                language = language,
                onLanguageToggle = { viewModel.toggleLanguage() },
                onApplySetting = { mode -> viewModel.updateSetting(SettingCategory.CYCLING_MODE, mode) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SafetySettings.route) {
            SafetySettingsScreen(
                currentRecord = settingRecords[SettingCategory.SLOPE_PARKING],
                lastResult = lastResult,
                isExecuting = isExecuting,
                currentSpeedKmh = scooterState.speedKmh ?: 0.0f,
                language = language,
                onLanguageToggle = { viewModel.toggleLanguage() },
                onApplySlopeParking = { opt -> viewModel.updateSetting(SettingCategory.SLOPE_PARKING, opt) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SmartFeatures.route) {
            SmartFeaturesScreen(
                language = language,
                onLanguageToggle = { viewModel.toggleLanguage() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SoundSettings.route) {
            SoundSettingsScreen(
                language = language,
                onLanguageToggle = { viewModel.toggleLanguage() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.NfcManagement.route) {
            NfcManagementScreen(
                language = language,
                onLanguageToggle = { viewModel.toggleLanguage() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.VehicleInfo.route) {
            VehicleInfoScreen(
                state = scooterState,
                language = language,
                onLanguageToggle = { viewModel.toggleLanguage() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Diagnostics.route) {
            DiagnosticsScreen(
                state = scooterState,
                persistenceRecords = persistenceRecords,
                language = language,
                onLanguageToggle = { viewModel.toggleLanguage() },
                onTriggerSimulatedPowerCycle = { viewModel.triggerSimulatedPowerCycle() },
                onUpdateSetting = { cat, opt -> viewModel.updateSetting(cat, opt) },
                onRecordBaseline = { cat, opt -> viewModel.recordPersistenceBaseline(cat, opt) },
                onRecordWritten = { cat, opt, ack, beep -> viewModel.recordPersistenceWritten(cat, opt, ack, beep) },
                onVerifyPostPowerCycle = { cat, opt -> viewModel.verifyPersistenceAfterReconnection(cat, opt) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AppSettings.route) {
            AppSettingsScreen(
                isSimulatorEnabled = scooterState.isSimulated,
                language = language,
                onLanguageToggle = { viewModel.toggleLanguage() },
                onToggleSimulator = { enabled -> viewModel.setSimulatorMode(enabled) },
                onNavigateToLanguage = { navController.navigate(Screen.LanguageSelection.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) },
                onClearCache = { viewModel.clearCache() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.LanguageSelection.route) {
            LanguageSelectionScreen(
                currentLanguage = language,
                onSelectLanguage = { lang -> viewModel.setLanguage(lang) },
                onLanguageToggle = { viewModel.toggleLanguage() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                language = language,
                onLanguageToggle = { viewModel.toggleLanguage() },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
