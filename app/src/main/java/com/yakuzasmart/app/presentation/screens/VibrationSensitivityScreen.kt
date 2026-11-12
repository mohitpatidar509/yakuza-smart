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
fun VibrationSensitivityScreen(
    currentRecord: SettingRecord?,
    lastResult: TransactionResult?,
    isExecuting: Boolean,
    language: Language,
    onLanguageToggle: () -> Unit,
    onApplySetting: (VibrationSensitivity) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedSensitivity by remember(currentRecord) {
        mutableStateOf((currentRecord?.requestedOption as? VibrationSensitivity) ?: VibrationSensitivity.MEDIUM)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YakuzaBlack)
    ) {
        YakuzaTopBar(
            title = AppStrings.vibrationSensitivity(language),
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
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = AppStrings.vibrationSensitivityDesc(language),
                    color = YakuzaTextSecondary,
                    fontSize = 13.sp
                )

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
                            Text(text = "Controller Status", color = YakuzaTextSecondary, fontSize = 12.sp)
                            StatusBadge(stage = record.stage, language = language)
                        }
                    }
                }

                VibrationSensitivity.entries.forEach { option ->
                    val isSelected = selectedSensitivity == option
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
                            .clickable { selectedSensitivity = option }
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = if (language == Language.HINDI) option.labelHi else option.labelEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isSelected) YakuzaOrangePrimary else YakuzaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when (option) {
                                    VibrationSensitivity.LOW ->
                                        if (language == Language.HINDI) "तेज धक्के पर ही अलार्म बजेगा" else "Triggers only on heavy impact"
                                    VibrationSensitivity.MEDIUM ->
                                        if (language == Language.HINDI) "सामान्य सड़कों व पार्किंग हेतु संतुलित" else "Balanced threshold for street parking"
                                    VibrationSensitivity.HIGH ->
                                        if (language == Language.HINDI) "हल्के से छूने पर भी सायरन सक्रिय होगा" else "Triggers on subtle movement or touch"
                                },
                                fontSize = 12.sp,
                                color = YakuzaTextSecondary
                            )
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
                text = if (language == Language.HINDI) "कंट्रोलर में लागू करें" else "Apply & Verify (Opcode 0x3B)",
                onClick = { onApplySetting(selectedSensitivity) },
                isLoading = isExecuting
            )
        }
    }
}
