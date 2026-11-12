package com.yakuzasmart.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun SoundSettingsScreen(
    language: Language,
    onLanguageToggle: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var turnSignalTone by remember { mutableStateOf(true) }
    var voiceSettings by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YakuzaBlack)
    ) {
        YakuzaTopBar(
            title = AppStrings.soundSettings(language),
            language = language,
            onLanguageToggle = onLanguageToggle,
            canNavigateBack = true,
            onNavigateBack = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (language == Language.HINDI) "इंडिकेटर एवं आवाज़ नियंत्रण" else "AUDIO & CHIME PREFERENCES",
                color = YakuzaOrangePrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

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
                            text = if (language == Language.HINDI) "इंडिकेटर टोन (Turn Signal Tone)" else "Turn Signal Audio Chime",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = YakuzaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (language == Language.HINDI)
                                "इंडिकेटर चालू होने पर रिले की टिक-टिक / बीप ध्वनि"
                            else
                                "Audible flasher beeper during turn indicator activation",
                            fontSize = 11.sp,
                            color = YakuzaTextSecondary
                        )
                    }
                    Switch(
                        checked = turnSignalTone,
                        onCheckedChange = { turnSignalTone = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = YakuzaOrangePrimary,
                            checkedTrackColor = YakuzaOrangeContainer
                        )
                    )
                }
            }

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
                            text = if (language == Language.HINDI) "वॉइस सेटिंग्स (Voice Settings)" else "Synthesized Voice Alerts",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = YakuzaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (language == Language.HINDI)
                                "कम बैटरी या ओवर-स्पीड पर सिंथेसाइज्ड वॉइस गाइडेंस"
                            else
                                "Spoken vehicle status warnings (e.g., 'Low Battery', 'Stand Down')",
                            fontSize = 11.sp,
                            color = YakuzaTextSecondary
                        )
                    }
                    Switch(
                        checked = voiceSettings,
                        onCheckedChange = { voiceSettings = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = YakuzaOrangePrimary,
                            checkedTrackColor = YakuzaOrangeContainer
                        )
                    )
                }
            }
        }
    }
}
