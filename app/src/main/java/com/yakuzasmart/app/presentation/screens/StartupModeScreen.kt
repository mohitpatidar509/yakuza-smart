package com.yakuzasmart.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.yakuzasmart.app.domain.model.*
import com.yakuzasmart.app.presentation.components.StatusBadge
import com.yakuzasmart.app.presentation.components.YakuzaButton
import com.yakuzasmart.app.presentation.components.YakuzaTopBar

@Composable
fun StartupModeScreen(
    currentRecord: SettingRecord?,
    lastResult: TransactionResult?,
    isExecuting: Boolean,
    language: Language,
    onLanguageToggle: () -> Unit,
    onApplySetting: (StartupMode) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedMode by remember(currentRecord) {
        mutableStateOf((currentRecord?.requestedOption as? StartupMode) ?: StartupMode.SOFT_START)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YakuzaBlack)
    ) {
        YakuzaTopBar(
            title = AppStrings.startupMode(language),
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
                    text = AppStrings.startupModeDesc(language),
                    color = YakuzaTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                // Current verification status card
                currentRecord?.let { record ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(YakuzaDarkCharcoal, RoundedCornerShape(14.dp))
                            .border(1.dp, YakuzaCardBorder, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (language == Language.HINDI) "सत्यापन स्थिति" else "Verification State",
                                color = YakuzaTextSecondary,
                                fontSize = 12.sp
                            )
                            StatusBadge(stage = record.stage, language = language)
                        }
                    }
                }

                // Option 1: Soft Start
                StartupOptionCard(
                    title = AppStrings.softStart(language),
                    description = if (language == Language.HINDI)
                        "पहिए को धीरे-धीरे पावर मिलती है। अचानक झटके से बचाव और आरामदायक राइड।"
                    else
                        "Smooth, progressive torque delivery. Prevents wheel spin and sudden jerks.",
                    isSelected = selectedMode == StartupMode.SOFT_START,
                    onClick = { selectedMode = StartupMode.SOFT_START }
                )

                // Option 2: Hard Boot
                StartupOptionCard(
                    title = AppStrings.hardBoot(language),
                    description = if (language == Language.HINDI)
                        "एक्सेलेरेटर घुमाते ही तुरंत अधिकतम पिकअप और टॉर्क मिलता है।"
                    else
                        "Instant throttle engagement with full initial torque for rapid takeoff.",
                    isSelected = selectedMode == StartupMode.HARD_BOOT,
                    onClick = { selectedMode = StartupMode.HARD_BOOT }
                )

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
                            color = if (res.success) YakuzaStatusConnected else YakuzaStatusDisconnected,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            YakuzaButton(
                text = if (language == Language.HINDI) "कंट्रोलर में लागू करें" else "Apply & Verify (Opcode 0x3E)",
                onClick = { onApplySetting(selectedMode) },
                isLoading = isExecuting
            )
        }
    }
}

@Composable
private fun StartupOptionCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) YakuzaOrangeContainer else YakuzaDarkCharcoal,
                RoundedCornerShape(16.dp)
            )
            .border(
                1.5.dp,
                if (isSelected) YakuzaOrangePrimary else YakuzaCardBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (isSelected) YakuzaOrangePrimary else YakuzaTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = YakuzaTextSecondary,
                lineHeight = 17.sp
            )
        }
    }
}
