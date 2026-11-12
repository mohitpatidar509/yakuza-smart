package com.yakuzasmart.app.presentation.screens

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yakuzasmart.app.core.localization.AppStrings
import com.yakuzasmart.app.core.localization.Language
import com.yakuzasmart.app.core.theme.*
import com.yakuzasmart.app.domain.model.BleDeviceItem
import com.yakuzasmart.app.domain.model.ScooterState
import com.yakuzasmart.app.presentation.components.SafetyWarningBanner
import com.yakuzasmart.app.presentation.components.TelemetryCard
import com.yakuzasmart.app.presentation.components.YakuzaTopBar

@Composable
fun DashboardScreen(
    state: ScooterState,
    savedScooter: BleDeviceItem?,
    isBluetoothEnabled: Boolean,
    language: Language,
    onLanguageToggle: () -> Unit,
    onConnectSavedScooter: () -> Unit,
    onForgetSavedScooter: () -> Unit,
    onNavigateToScan: () -> Unit,
    onDisconnect: () -> Unit,
    onToggleIgnition: () -> Unit,
    onToggleLock: () -> Unit,
    onFindVehicle: () -> Unit,
    onOpenSeat: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNfc: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToAppSettings: () -> Unit,
    onNavigateToVehicleInfo: () -> Unit
) {
    val context = LocalContext.current

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
            // 1. BLUETOOTH OFF ALERT CARD
            if (!isBluetoothEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(YakuzaStatusWarning.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .border(1.5.dp, YakuzaStatusWarning, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BluetoothDisabled,
                                contentDescription = "Bluetooth Disabled",
                                tint = YakuzaStatusWarning,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (language == Language.HINDI) "मोबाइल का ब्लूटूथ बंद है" else "Bluetooth is Turned OFF",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = YakuzaStatusWarning
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (language == Language.HINDI)
                                "गाड़ी को स्कैन करने या कनेक्ट करने के लिए कृपया पहले अपने मोबाइल का ब्लूटूथ चालू करें।"
                            else
                                "Please turn on your phone's Bluetooth to scan or connect to your Yakuza scooter.",
                            fontSize = 12.sp,
                            color = YakuzaTextPrimary,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                try {
                                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                                    enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(enableBtIntent)
                                } catch (e: Exception) {
                                    context.startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    })
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = YakuzaStatusWarning),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (language == Language.HINDI) "ब्लूटूथ ऑन करें" else "Turn On Bluetooth",
                                color = YakuzaBlack,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 2. CONNECTION / SAVED SCOOTER PROFILE CARD
            if (state.isConnected) {
                // ACTIVE CONNECTION CARD
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(YakuzaDarkCharcoal, RoundedCornerShape(16.dp))
                        .border(1.dp, YakuzaStatusConnected.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(YakuzaStatusConnected, RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = state.connectedDevice?.displayName ?: (if (language == Language.HINDI) "याकुज़ा इलेक्ट्रिक स्कूटर" else "Yakuza Electric Scooter"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = YakuzaTextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (state.isSimulated)
                                    AppStrings.simulated(language)
                                else
                                    "Connected • MAC: ${state.connectedDevice?.address ?: "N/A"}",
                                fontSize = 11.sp,
                                color = if (state.isSimulated) YakuzaStatusSimulated else YakuzaStatusConnected
                            )
                        }

                        Button(
                            onClick = onDisconnect,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = YakuzaSurfaceElevated,
                                contentColor = YakuzaOrangePrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = AppStrings.disconnect(language),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else if (savedScooter != null) {
                // SAVED SCOOTER CARD
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(YakuzaDarkCharcoal, RoundedCornerShape(16.dp))
                        .border(1.5.dp, YakuzaOrangePrimary.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (language == Language.HINDI) "मेरी सहेजी गई स्कूटी" else "My Saved Scooter",
                                    color = YakuzaOrangePrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = savedScooter.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = YakuzaTextPrimary
                                )
                                Text(
                                    text = "MAC: ${savedScooter.address}  •  Status: Offline",
                                    fontSize = 11.sp,
                                    color = YakuzaTextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onConnectSavedScooter,
                                enabled = isBluetoothEnabled,
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = YakuzaOrangePrimary,
                                    contentColor = Color.White,
                                    disabledContainerColor = YakuzaCardBackground,
                                    disabledContentColor = YakuzaTextMuted
                                )
                            ) {
                                Icon(Icons.Default.BluetoothConnected, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == Language.HINDI) "स्कूटी से जुड़ें" else "Connect Scooter",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Button(
                                onClick = onNavigateToScan,
                                modifier = Modifier.height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = YakuzaSurfaceElevated,
                                    contentColor = YakuzaTextSecondary
                                )
                            ) {
                                Text(
                                    text = if (language == Language.HINDI) "नई खोजें" else "Scan New",
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (language == Language.HINDI) "स्कूटी भूलें (Forget)" else "Forget this scooter",
                            color = YakuzaTextMuted,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .align(Alignment.End)
                                .clickable { onForgetSavedScooter() }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            } else {
                // NO SCOOTER SAVED YET
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
                                text = AppStrings.notConnected(language),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = YakuzaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (language == Language.HINDI) "स्कैन करके अपनी याकुज़ा स्कूटी से जुड़ें" else "Scan to pair your Yakuza scooter",
                                fontSize = 11.sp,
                                color = YakuzaTextMuted
                            )
                        }

                        Button(
                            onClick = onNavigateToScan,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = YakuzaOrangePrimary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = AppStrings.scan(language),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 3. VEHICLE CONTROL PANEL (Ignition ON/OFF, Lock/Unlock, Siren, Seat)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(YakuzaDarkCharcoal, RoundedCornerShape(20.dp))
                    .border(
                        1.5.dp,
                        if (state.isConnected && state.isIgnitionOn) YakuzaStatusConnected.copy(alpha = 0.8f) else YakuzaCardBorder,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == Language.HINDI) "वाहन रिमोट कंट्रोल" else "REMOTE VEHICLE CONTROLS",
                            color = YakuzaOrangePrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        // Power Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (!state.isConnected) YakuzaCardBackground
                                    else if (state.isIgnitionOn) YakuzaStatusConnected.copy(alpha = 0.2f)
                                    else YakuzaStatusWarning.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (!state.isConnected)
                                    (if (language == Language.HINDI) "डिस्कनेक्टेड" else "OFFLINE")
                                else if (state.isIgnitionOn)
                                    (if (language == Language.HINDI) "गाड़ी चालू है (ON)" else "IGNITION ON")
                                else
                                    (if (language == Language.HINDI) "स्टैंडबाय (OFF)" else "STANDBY (OFF)"),
                                color = if (!state.isConnected) YakuzaTextMuted
                                else if (state.isIgnitionOn) YakuzaStatusConnected
                                else YakuzaStatusWarning,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Main Ignition Switch Button
                    Button(
                        onClick = onToggleIgnition,
                        enabled = state.isConnected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.isIgnitionOn) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                            contentColor = Color.White,
                            disabledContainerColor = YakuzaCardBackground,
                            disabledContentColor = YakuzaTextMuted
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Ignition Power",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (!state.isConnected)
                                (if (language == Language.HINDI) "कंट्रोल के लिए कनेक्ट करें" else "Connect to Control Bike")
                            else if (state.isIgnitionOn)
                                (if (language == Language.HINDI) "गाड़ी बंद करें (POWER OFF)" else "TURN VEHICLE OFF")
                            else
                                (if (language == Language.HINDI) "गाड़ी चालू करें (POWER ON)" else "START VEHICLE (POWER ON)"),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary Remote Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Anti-Theft Lock
                        VehicleActionButton(
                            icon = if (state.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            label = if (state.isLocked)
                                (if (language == Language.HINDI) "अनलॉक" else "Unlock")
                            else
                                (if (language == Language.HINDI) "सुरक्षा लॉक" else "Lock Guard"),
                            isActive = state.isLocked,
                            activeColor = YakuzaStatusWarning,
                            enabled = state.isConnected,
                            onClick = onToggleLock,
                            modifier = Modifier.weight(1f)
                        )

                        // Siren / Find Scooter
                        VehicleActionButton(
                            icon = Icons.Default.VolumeUp,
                            label = if (language == Language.HINDI) "सायरन / खोजें" else "Find Horn",
                            isActive = false,
                            activeColor = YakuzaOrangePrimary,
                            enabled = state.isConnected,
                            onClick = onFindVehicle,
                            modifier = Modifier.weight(1f)
                        )

                        // Open Seat / Trunk
                        VehicleActionButton(
                            icon = Icons.Default.AirlineSeatReclineNormal,
                            label = if (language == Language.HINDI) "सीट खोलें" else "Open Seat",
                            isActive = false,
                            activeColor = YakuzaOrangePrimary,
                            enabled = state.isConnected,
                            onClick = onOpenSeat,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 4. Real Telemetry Display
            TelemetryCard(state = state, language = language)

            // 5. Safety Warning Banner
            SafetyWarningBanner(language = language)

            // 6. Quick Hub Actions
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
                    title = AppStrings.nfcManagement(language),
                    icon = Icons.Default.Nfc,
                    onClick = onNavigateToNfc,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HubCard(
                    title = AppStrings.diagnostics(language),
                    icon = Icons.Default.Build,
                    onClick = onNavigateToDiagnostics,
                    modifier = Modifier.weight(1f)
                )
                HubCard(
                    title = AppStrings.vehicleInfo(language),
                    icon = Icons.Default.ElectricScooter,
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
private fun VehicleActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) activeColor.copy(alpha = 0.2f) else YakuzaCardBackground)
            .border(
                1.dp,
                if (isActive) activeColor else YakuzaCardBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (!enabled) YakuzaTextMuted else if (isActive) activeColor else YakuzaTextPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                color = if (!enabled) YakuzaTextMuted else if (isActive) activeColor else YakuzaTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
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
            .clip(RoundedCornerShape(16.dp))
            .background(YakuzaDarkCharcoal)
            .border(1.dp, YakuzaCardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(YakuzaOrangePrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = YakuzaOrangePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    color = YakuzaTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = YakuzaTextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
