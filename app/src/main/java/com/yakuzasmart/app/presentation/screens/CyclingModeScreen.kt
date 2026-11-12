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
fun CyclingModeScreen(
    currentRecord: SettingRecord?,
    lastResult: TransactionResult?,
    isExecuting: Boolean,
    language: Language,
    onLanguageToggle: () -> Unit,
    onApplySetting: (CyclingMode) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedMode by remember(currentRecord) {
        mutableStateOf((currentRecord?.requestedOption as? CyclingMode) ?: CyclingMode.COMFORT)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YakuzaBlack)
    ) {
        YakuzaTopBar(
            title = AppStrings.cyclingMode(language),
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
                    text = AppStrings.cyclingModeDesc(language),
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
                            Text(text = "Active Controller Mode", color = YakuzaTextSecondary, fontSize = 12.sp)
                            StatusBadge(stage = record.stage, language = language)
                        }
                    }
                }

                CyclingMode.entries.forEach { mode ->
                    val isSelected = selectedMode == mode
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
                            .clickable { selectedMode = mode }
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = if (language == Language.HINDI) mode.labelHi else mode.labelEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isSelected) YakuzaOrangePrimary else YakuzaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when (mode) {
                                    CyclingMode.COMFORT ->
                                        if (language == Language.HINDI) "आरामदायक राइड और अधिकतम रेंज" else "Smooth linear throttle, optimal battery economy"
                                    CyclingMode.CROSS_COUNTRY ->
                                        if (language == Language.HINDI) "कच्ची सड़कों और ढलान हेतु अधिक टॉर्क" else "High low-end torque for slopes and rough terrain"
                                    CyclingMode.CROSS_COUNTRY_PLUS ->
                                        if (language == Language.HINDI) "अधिकतम टॉर्क व तीव्र रीजेनरेटिव ब्रेकिंग" else "Maximum peak torque with aggressive regen braking"
                                    CyclingMode.RACING ->
                                        if (language == Language.HINDI) "हाई स्पीड और त्वरित रेस मोड" else "Unrestricted motor timing for top velocity"
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
                text = if (language == Language.HINDI) "लागू करें" else "Apply & Verify (Opcode 0x45)",
                onClick = { onApplySetting(selectedMode) },
                isLoading = isExecuting
            )
        }
    }
}
