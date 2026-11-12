package com.yakuzasmart.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yakuzasmart.app.core.localization.AppStrings
import com.yakuzasmart.app.core.localization.Language
import com.yakuzasmart.app.core.theme.*
import com.yakuzasmart.app.presentation.components.YakuzaTopBar

@Composable
fun AppSettingsScreen(
    isSimulatorEnabled: Boolean,
    language: Language,
    onLanguageToggle: () -> Unit,
    onToggleSimulator: (Boolean) -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onClearCache: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YakuzaBlack)
    ) {
        YakuzaTopBar(
            title = AppStrings.appSettings(language),
            language = language,
            onLanguageToggle = onLanguageToggle,
            canNavigateBack = true,
            onNavigateBack = onNavigateBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Simulator Mode Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(YakuzaDarkCharcoal, RoundedCornerShape(16.dp))
                        .border(1.dp, YakuzaCardBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == Language.HINDI) "आभासी सिम्युलेटर मोड" else "BLE Simulator Mode",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = YakuzaStatusSimulated
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (language == Language.HINDI)
                                    "बिना असली स्कूटर के समस्त ऐप फीचर्स, सेटिंग्स व पर्सिस्टेंस बग का परीक्षण करें।"
                                else
                                    "Test all UI features, protocol framing, and power-cycle reset behavior without a physical scooter.",
                                fontSize = 12.sp,
                                color = YakuzaTextSecondary,
                                lineHeight = 16.sp
                            )
                        }

                        Switch(
                            checked = isSimulatorEnabled,
                            onCheckedChange = onToggleSimulator,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = YakuzaStatusSimulated,
                                checkedTrackColor = YakuzaDarkCharcoal
                            )
                        )
                    }
                }
            }

            // Language Selector Link
            item {
                SettingsNavigationRow(
                    title = if (language == Language.HINDI) "भाषा चुनें (Language)" else "Select Language",
                    subtitle = if (language == Language.HINDI) "वर्तमान: हिन्दी" else "Current: English",
                    onClick = onNavigateToLanguage
                )
            }

            // Clear Cache Action
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(YakuzaDarkCharcoal, RoundedCornerShape(16.dp))
                        .border(1.dp, YakuzaCardBorder, RoundedCornerShape(16.dp))
                        .clickable { onClearCache() }
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = if (language == Language.HINDI) "लोकल डेटा एवं कैश मिटाएं" else "Clear Local Cache & History",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = YakuzaStatusDisconnected
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (language == Language.HINDI)
                                "सहेजी गई सेटिंग्स और परीक्षण रिकॉर्ड रीसेट करें।"
                            else
                                "Reset cached device settings and test history.",
                            fontSize = 11.sp,
                            color = YakuzaTextSecondary
                        )
                    }
                }
            }

            // About and Privacy Link
            item {
                SettingsNavigationRow(
                    title = if (language == Language.HINDI) "ऐप के बारे में एवं गोपनीयता" else "About & Privacy Policy",
                    subtitle = "Yakuza Smart v1.0.0 (Production Native Build)",
                    onClick = onNavigateToAbout
                )
            }
        }
    }
}

@Composable
private fun SettingsNavigationRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(YakuzaDarkCharcoal, RoundedCornerShape(16.dp))
            .border(1.dp, YakuzaCardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = YakuzaTextPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 11.sp, color = YakuzaTextMuted)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = YakuzaTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
