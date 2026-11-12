package com.yakuzasmart.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yakuzasmart.app.core.localization.AppStrings
import com.yakuzasmart.app.core.localization.Language
import com.yakuzasmart.app.core.theme.*
import com.yakuzasmart.app.domain.model.ScooterState
import com.yakuzasmart.app.presentation.components.SafetyWarningBanner
import com.yakuzasmart.app.presentation.components.TelemetryCard
import com.yakuzasmart.app.presentation.components.YakuzaTopBar

@Composable
fun DashboardScreen(
    state: ScooterState,
    language: Language,
    onLanguageToggle: () -> Unit,
    onNavigateToScan: () -> Unit,
    onDisconnect: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNfc: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToAppSettings: () -> Unit,
    onNavigateToVehicleInfo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YakuzaBlack)
    ) {
        YakuzaTopBar(
            title = AppStrings.appTitle(language),
            language = language,
            onLanguageToggle = onLanguageToggle,
            connectionStatus = state.connectionStatus,
            isSimulated = state.isSimulated
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Connection Banner Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(YakuzaDarkCharcoal, RoundedCornerShape(16.dp))
                    .border(1.dp, YakuzaCardBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (state.isConnected) {
                                state.connectedDevice?.displayName ?: (if (language == Language.HINDI) "याकुज़ा स्कूटर" else "Yakuza Scooter")
                            } else {
                                AppStrings.notConnected(language)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = YakuzaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (state.isConnected) {
                                if (state.isSimulated)
                                    AppStrings.simulated(language)
                                else
                                    "MAC: ${state.connectedDevice?.address ?: "N/A"}"
                            } else {
                                if (language == Language.HINDI) "स्कैन करके स्कूटर से जुड़ें" else "Tap scan to connect over BLE"
                            },
                            fontSize = 11.sp,
                            color = if (state.isSimulated) YakuzaStatusSimulated else YakuzaTextMuted
                        )
                    }

                    Button(
                        onClick = {
                            if (state.isConnected) onDisconnect() else onNavigateToScan()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.isConnected) YakuzaSurfaceElevated else YakuzaOrangePrimary,
                            contentColor = if (state.isConnected) YakuzaOrangePrimary else androidx.compose.ui.graphics.Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (state.isConnected) AppStrings.disconnect(language) else AppStrings.scan(language),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Real Telemetry Display
            TelemetryCard(state = state, language = language)

            // Safety Warning Banner
            SafetyWarningBanner(language = language)

            // Quick Hub Actions
            Text(
                text = if (language == Language.HINDI) "क्विक मेनू" else "QUICK ACTIONS",
                color = YakuzaOrangePrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HubCard(
                    title = AppStrings.vehicleSettings(language),
                    icon = Icons.Default.Tune,
                    onClick = onNavigateToSettings,
                    modifier = Modifier.weight(1f)
                )
                HubCard(
                    title = AppStrings.diagnostics(language),
                    icon = Icons.Default.Assessment,
                    onClick = onNavigateToDiagnostics,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HubCard(
                    title = AppStrings.nfcManagement(language),
                    icon = Icons.Default.CreditCard,
                    onClick = onNavigateToNfc,
                    modifier = Modifier.weight(1f)
                )
                HubCard(
                    title = AppStrings.vehicleInfo(language),
                    icon = Icons.Default.Info,
                    onClick = onNavigateToVehicleInfo,
                    modifier = Modifier.weight(1f)
                )
            }

            HubCard(
                title = AppStrings.appSettings(language),
                icon = Icons.Default.Settings,
                onClick = onNavigateToAppSettings,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun HubCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(YakuzaDarkCharcoal, RoundedCornerShape(16.dp))
            .border(1.dp, YakuzaCardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = YakuzaOrangePrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = YakuzaTextPrimary
            )
        }
    }
}
