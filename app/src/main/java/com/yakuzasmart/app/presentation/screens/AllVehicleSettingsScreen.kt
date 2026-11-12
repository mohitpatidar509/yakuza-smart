package com.yakuzasmart.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yakuzasmart.app.core.localization.AppStrings
import com.yakuzasmart.app.core.localization.Language
import com.yakuzasmart.app.core.theme.*
import com.yakuzasmart.app.domain.model.SettingCategory
import com.yakuzasmart.app.domain.model.SettingRecord
import com.yakuzasmart.app.presentation.components.SafetyWarningBanner
import com.yakuzasmart.app.presentation.components.SettingCard
import com.yakuzasmart.app.presentation.components.YakuzaTopBar

@Composable
fun AllVehicleSettingsScreen(
    records: Map<SettingCategory, SettingRecord>,
    language: Language,
    onLanguageToggle: () -> Unit,
    onNavigateToStartupMode: () -> Unit,
    onNavigateToSpeedRegulation: () -> Unit,
    onNavigateToVibration: () -> Unit,
    onNavigateToCyclingMode: () -> Unit,
    onNavigateToSafety: () -> Unit,
    onNavigateToSmartFeatures: () -> Unit,
    onNavigateToSound: () -> Unit,
    onNavigateToNfc: () -> Unit,
    onNavigateToVehicleInfo: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YakuzaBlack)
    ) {
        YakuzaTopBar(
            title = AppStrings.vehicleSettings(language),
            language = language,
            onLanguageToggle = onLanguageToggle,
            canNavigateBack = true,
            onNavigateBack = onNavigateBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SafetyWarningBanner(language = language)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (language == Language.HINDI) "समस्त वाहन सेटिंग्स" else "LEGACY SETTINGS CATALOGUE",
                    color = YakuzaOrangePrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (language == Language.HINDI)
                        "सभी विकल्प मूल ऐप से संरक्षित हैं • कंट्रोलर सत्यापन आवश्यक"
                    else
                        "All options preserved from original APK • Requires controller verification",
                    color = YakuzaTextMuted,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            item {
                SettingCard(
                    category = SettingCategory.STARTUP_MODE,
                    currentRecord = records[SettingCategory.STARTUP_MODE],
                    language = language,
                    onClick = onNavigateToStartupMode
                )
            }

            item {
                SettingCard(
                    category = SettingCategory.SPEED_REGULATION,
                    currentRecord = records[SettingCategory.SPEED_REGULATION],
                    language = language,
                    onClick = onNavigateToSpeedRegulation
                )
            }

            item {
                SettingCard(
                    category = SettingCategory.VIBRATION_SENSITIVITY,
                    currentRecord = records[SettingCategory.VIBRATION_SENSITIVITY],
                    language = language,
                    onClick = onNavigateToVibration
                )
            }

            item {
                SettingCard(
                    category = SettingCategory.CYCLING_MODE,
                    currentRecord = records[SettingCategory.CYCLING_MODE],
                    language = language,
                    onClick = onNavigateToCyclingMode
                )
            }

            item {
                SettingCard(
                    category = SettingCategory.SLOPE_PARKING,
                    currentRecord = records[SettingCategory.SLOPE_PARKING],
                    language = language,
                    onClick = onNavigateToSafety
                )
            }

            item {
                SettingCard(
                    category = SettingCategory.SMART_HELMET,
                    currentRecord = records[SettingCategory.SMART_HELMET],
                    language = language,
                    onClick = onNavigateToSmartFeatures
                )
            }

            item {
                SettingCard(
                    category = SettingCategory.SAFETY_SYSTEMS,
                    currentRecord = records[SettingCategory.SAFETY_SYSTEMS],
                    language = language,
                    onClick = onNavigateToSafety
                )
            }

            item {
                SettingCard(
                    category = SettingCategory.TURN_SIGNAL_TONE,
                    currentRecord = records[SettingCategory.TURN_SIGNAL_TONE],
                    language = language,
                    onClick = onNavigateToSound
                )
            }

            item {
                SettingCard(
                    category = SettingCategory.NFC_MANAGEMENT,
                    currentRecord = records[SettingCategory.NFC_MANAGEMENT],
                    language = language,
                    onClick = onNavigateToNfc
                )
            }

            item {
                SettingCard(
                    category = SettingCategory.INDUCTION_VOLTAGE,
                    currentRecord = records[SettingCategory.INDUCTION_VOLTAGE],
                    language = language,
                    onClick = onNavigateToVehicleInfo
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
