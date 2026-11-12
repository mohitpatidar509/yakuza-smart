package com.yakuzasmart.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.yakuzasmart.app.presentation.components.ConfirmationDialog
import com.yakuzasmart.app.presentation.components.SafetyWarningBanner
import com.yakuzasmart.app.presentation.components.StatusBadge
import com.yakuzasmart.app.presentation.components.YakuzaTopBar

@Composable
fun SafetySettingsScreen(
    currentRecord: SettingRecord?,
    lastResult: TransactionResult?,
    isExecuting: Boolean,
    currentSpeedKmh: Float,
    language: Language,
    onLanguageToggle: () -> Unit,
    onApplySlopeParking: (SlopeParkingOption) -> Unit,
    onNavigateBack: () -> Unit
) {
    var slopeParkingState by remember { mutableStateOf(false) }
    var pcsState by remember { mutableStateOf(false) }
    var tcsState by remember { mutableStateOf(true) }
    var absState by remember { mutableStateOf(true) }

    var showSlopeConfirmDialog by remember { mutableStateOf(false) }

    if (showSlopeConfirmDialog) {
        ConfirmationDialog(
            title = if (language == Language.HINDI) "स्लोप पार्किंग बदलें?" else "Toggle Slope Parking?",
            message = if (language == Language.HINDI)
                "यह ढलान पर मोटर ब्रेक को सक्रिय या निष्क्रिय करता है। सुनिश्चित करें कि स्कूटर स्थिर है।"
            else
                "This engages or disengages electronic hill-hold motor locking. Ensure the vehicle is stationary.",
            language = language,
            onConfirm = {
                showSlopeConfirmDialog = false
                val next = if (slopeParkingState) SlopeParkingOption.CLOSE else SlopeParkingOption.OPEN
                slopeParkingState = !slopeParkingState
                onApplySlopeParking(next)
            },
            onDismiss = { showSlopeConfirmDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YakuzaBlack)
    ) {
        YakuzaTopBar(
            title = AppStrings.safetyFeatures(language),
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
            item {
                SafetyWarningBanner(language = language)
            }

            // Slope Parking Card
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = AppStrings.slopeParking(language),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = YakuzaTextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(
                                    stage = currentRecord?.stage ?: TransactionStage.IDLE,
                                    language = language
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (language == Language.HINDI)
                                    "चढ़ाई पर रुकने पर स्कूटर को पीछे फिसलने से रोकता है (Opcode 0x3D)"
                                else
                                    "Electronic hill hold prevents roll-back on inclines (Opcode 0x3D)",
                                fontSize = 12.sp,
                                color = YakuzaTextSecondary
                            )
                        }

                        Switch(
                            checked = slopeParkingState,
                            onCheckedChange = { showSlopeConfirmDialog = true },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = YakuzaOrangePrimary,
                                checkedTrackColor = YakuzaOrangeContainer
                            ),
                            enabled = currentSpeedKmh <= 0.5f && !isExecuting
                        )
                    }
                }
            }

            // PCS
            item {
                SafetyFeatureRow(
                    title = "PCS (Pre-Collision System)",
                    description = if (language == Language.HINDI)
                        "सेंसर आधारित संभावित टकराव चेतावनी व थ्रॉटल कट-ऑफ (वाहन समर्थित)"
                    else
                        "Proximity radar warning and emergency throttle cut-off",
                    checked = pcsState,
                    onCheckedChange = { pcsState = it },
                    language = language
                )
            }

            // TCS
            item {
                SafetyFeatureRow(
                    title = "TCS (Traction Control System)",
                    description = if (language == Language.HINDI)
                        "गीली व फिसलन भरी सड़कों पर पहिया फिसलने से बचाव"
                    else
                        "Modulates torque to suppress wheel slippage on wet asphalt",
                    checked = tcsState,
                    onCheckedChange = { tcsState = it },
                    language = language
                )
            }

            // ABS
            item {
                SafetyFeatureRow(
                    title = "ABS (Electronic Anti-lock Braking)",
                    description = if (language == Language.HINDI)
                        "अचानक ब्रेक लगाने पर पहिए को लॉक होने से बचाता है"
                    else
                        "Electronic counter-EMF brake pulsing prevents wheel lockup",
                    checked = absState,
                    onCheckedChange = { absState = it },
                    language = language
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SafetyFeatureRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    language: Language
) {
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
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = YakuzaTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = YakuzaTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = AppStrings.unverifiedHardwareBadge(language),
                    fontSize = 10.sp,
                    color = YakuzaStatusWarning
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = YakuzaOrangePrimary,
                    checkedTrackColor = YakuzaOrangeContainer
                )
            )
        }
    }
}
