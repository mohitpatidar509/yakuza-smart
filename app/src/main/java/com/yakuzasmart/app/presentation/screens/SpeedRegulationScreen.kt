package com.yakuzasmart.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yakuzasmart.app.core.localization.AppStrings
import com.yakuzasmart.app.core.localization.Language
import com.yakuzasmart.app.core.theme.*
import com.yakuzasmart.app.domain.model.*
import com.yakuzasmart.app.presentation.components.StatusBadge
import com.yakuzasmart.app.presentation.components.YakuzaButton
import com.yakuzasmart.app.presentation.components.YakuzaTopBar

@Composable
fun SpeedRegulationScreen(
    currentRecord: SettingRecord?,
    lastResult: TransactionResult?,
    isExecuting: Boolean,
    currentSpeedKmh: Float,
    language: Language,
    onLanguageToggle: () -> Unit,
    onApplySetting: (SpeedRegulationOption) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedGear by remember(currentRecord) {
        mutableStateOf((currentRecord?.requestedOption as? SpeedRegulationOption) ?: SpeedRegulationOption.SPEED_0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YakuzaBlack)
    ) {
        YakuzaTopBar(
            title = AppStrings.speedRegulation(language),
            language = language,
            onLanguageToggle = onLanguageToggle,
            canNavigateBack = true,
            onNavigateBack = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = AppStrings.speedRegulationDesc(language),
                    color = YakuzaTextSecondary,
                    fontSize = 13.sp
                )

                // Stationary Velocity Warning
                if (currentSpeedKmh > 0.5f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(YakuzaStatusDisconnected.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .border(1.dp, YakuzaStatusDisconnected.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = if (language == Language.HINDI)
                                "सुरक्षा लॉक: वाहन चल रहा है ($currentSpeedKmh km/h)। स्पीड मोड बदलने के लिए रुकें।"
                            else
                                "Safety Interlock: Vehicle in motion ($currentSpeedKmh km/h). Stop to change speed regulation.",
                            color = YakuzaStatusDisconnected,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                currentRecord?.let { record ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(YakuzaDarkCharcoal, RoundedCornerShape(14.dp))
                            .border(1.dp, YakuzaCardBorder, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Current Hardware State", color = YakuzaTextSecondary, fontSize = 12.sp)
                            StatusBadge(stage = record.stage, language = language)
                        }
                    }
                }

                SpeedRegulationOption.entries.forEach { option ->
                    val isSelected = selectedGear == option
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelected) YakuzaOrangeContainer else YakuzaDarkCharcoal,
                                RoundedCornerShape(14.dp)
                            )
                            .border(
                                1.5.dp,
                                if (isSelected) YakuzaOrangePrimary else YakuzaCardBorder,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedGear = option }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (language == Language.HINDI) option.labelHi else option.labelEn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) YakuzaOrangePrimary else YakuzaTextPrimary
                                )
                                Text(
                                    text = "Payload byte: 0x%02X".format(option.payloadByte),
                                    fontSize = 11.sp,
                                    color = YakuzaTextMuted
                                )
                            }
                        }
                    }
                }

                lastResult?.let { res ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (res.success) YakuzaStatusConnected.copy(alpha = 0.12f)
                                else YakuzaStatusDisconnected.copy(alpha = 0.12f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = if (language == Language.HINDI) res.messageHi else res.messageEn,
                            fontSize = 12.sp,
                            color = if (res.success) YakuzaStatusConnected else YakuzaStatusDisconnected
                        )
                    }
                }
            }

            YakuzaButton(
                text = if (language == Language.HINDI) "लागू करें" else "Apply & Verify (Opcode 0x3A)",
                onClick = { onApplySetting(selectedGear) },
                enabled = currentSpeedKmh <= 0.5f,
                isLoading = isExecuting
            )
        }
    }
}
