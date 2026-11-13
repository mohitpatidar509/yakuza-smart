package com.yakuzasmart.app.presentation.screens

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.yakuzasmart.app.presentation.components.*

enum class DashboardTab {
    RIDE,      // Screen 3: "Your ride is in progress ⚡"
    EXPLORE,   // Screen 2: "Where to?" Map & Hub
    SETTINGS   // Vehicle & App Settings
}

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
    var selectedTab by remember { mutableStateOf(DashboardTab.RIDE) }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OlaBackground)
    ) {
        // 1. Top Bar matching the clean Ola theme
        YakuzaTopBar(
            title = AppStrings.appTitle(language),
            language = language,
            onLanguageToggle = onLanguageToggle,
            connectionStatus = state.connectionStatus,
            isSimulated = state.isSimulated,
            showHelpAction = true,
            onHelpClick = onNavigateToVehicleInfo
        )

        // 2. Segmented Pill Tab Bar (Ride | Explore | Controls)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .clip(CircleShape)
                .background(OlaSurfaceSubtle)
                .border(1.dp, OlaCardBorder, CircleShape)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TabPill(
                title = if (language == Language.HINDI) "राइड डैशबोर्ड" else "Live Ride",
                icon = Icons.Default.LocationOn,
                isSelected = selectedTab == DashboardTab.RIDE,
                onClick = { selectedTab = DashboardTab.RIDE },
                modifier = Modifier.weight(1f)
            )
            TabPill(
                title = if (language == Language.HINDI) "मार्ग व खोज" else "Where To",
                icon = Icons.Default.Place,
                isSelected = selectedTab == DashboardTab.EXPLORE,
                onClick = { selectedTab = DashboardTab.EXPLORE },
                modifier = Modifier.weight(1f)
            )
            TabPill(
                title = if (language == Language.HINDI) "सेटिंग्स" else "Settings",
                icon = Icons.Default.Settings,
                isSelected = selectedTab == DashboardTab.SETTINGS,
                onClick = { selectedTab = DashboardTab.SETTINGS },
                modifier = Modifier.weight(1f)
            )
        }

        // 3. Scrollable Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // BLUETOOTH OFF WARNING BANNER
            if (!isBluetoothEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(OlaStatusWarning.copy(alpha = 0.12f))
                        .border(1.5.dp, OlaStatusWarning, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Bluetooth Disabled",
                                tint = OlaStatusWarning,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (language == Language.HINDI) "मोबाइल का ब्लूटूथ बंद है" else "Bluetooth is Turned OFF",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (language == Language.HINDI)
                                "स्कूटी से जुड़ने के लिए कृपया पहले ब्लूटूथ ऑन करें।"
                            else
                                "Please turn on Bluetooth to communicate with your Yakuza scooter.",
                            fontSize = 12.sp,
                            color = OlaTextSecondary
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
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = OlaStatusWarning),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (language == Language.HINDI) "ब्लूटूथ ऑन करें" else "Turn On Bluetooth",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // CONNECTION STATUS BAR (IF CONNECTED OR SAVED)
            if (state.isConnected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(OlaGreenSurface)
                        .border(1.dp, OlaGreenBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(OlaStatusConnected)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = state.connectedDevice?.displayName ?: "Yakuza Scooter",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = OlaTextPrimary
                                )
                                Text(
                                    text = if (state.isSimulated) AppStrings.simulated(language) else "BLE Connected • Secured",
                                    fontSize = 11.sp,
                                    color = OlaGreenDeepDark,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Button(
                            onClick = onDisconnect,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OlaWhite,
                                contentColor = OlaStatusDisconnected
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OlaCardBorder),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = AppStrings.disconnect(language),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else if (savedScooter != null) {
                // SAVED SCOOTER 1-TAP RECONNECT CARD
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(OlaWhite)
                        .border(1.dp, OlaCardBorder, RoundedCornerShape(24.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (language == Language.HINDI) "सहेजी गई स्कूटी" else "Saved Scooter",
                                    color = OlaGreenDeepDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = savedScooter.displayName,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = OlaTextPrimary
                                )
                            }
                            Text(
                                text = if (language == Language.HINDI) "हटाएँ (Forget)" else "Forget",
                                color = OlaTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clickable { onForgetSavedScooter() }
                                    .padding(4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OlaPillButton(
                                text = if (language == Language.HINDI) "स्कूटी से जुड़ें" else "Connect Scooter",
                                onClick = onConnectSavedScooter,
                                enabled = isBluetoothEnabled,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = onNavigateToScan,
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OlaSurfaceSubtle,
                                    contentColor = OlaTextPrimary
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OlaCardBorder),
                                modifier = Modifier.height(56.dp)
                            ) {
                                Text(
                                    text = if (language == Language.HINDI) "नई खोजें" else "Scan New",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                // NO SCOOTER PAIRED YET
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(OlaWhite)
                        .border(1.dp, OlaCardBorder, RoundedCornerShape(24.dp))
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppStrings.notConnected(language),
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = OlaTextPrimary
                            )
                            Text(
                                text = if (language == Language.HINDI) "याकुज़ा स्कूटी से जुड़ने के लिए स्कैन करें" else "Scan to connect your Yakuza scooter",
                                fontSize = 11.sp,
                                color = OlaTextMuted
                            )
                        }
                        Button(
                            onClick = onNavigateToScan,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OlaGreenPrimary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
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

            // CONTENT BASED ON SELECTED TAB
            when (selectedTab) {
                DashboardTab.RIDE -> {
                    // ==========================================
                    // SCREEN 3: "Your ride is in progress ⚡"
                    // ==========================================
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header
                        Column {
                            Text(
                                text = if (state.isConnected && state.isIgnitionOn) {
                                    if (language == Language.HINDI) "आपकी राइड जारी है ⚡" else "Your ride is\nin progress ⚡"
                                } else if (state.isConnected) {
                                    if (language == Language.HINDI) "गाड़ी तैयार है ⚡" else "Your scooter is\nready to ride ⚡"
                                } else {
                                    if (language == Language.HINDI) "स्मार्ट राइड। हरित कल।" else "Smart rides.\nGreener tomorrow."
                                },
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = OlaTextPrimary,
                                lineHeight = 34.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (state.isConnected && state.isIgnitionOn)
                                    (if (language == Language.HINDI) "आराम से बैठें और राइड का आनंद लें।" else "Sit back, relax and enjoy the ride.")
                                else
                                    (if (language == Language.HINDI) "गाड़ी चालू करने के लिए पावर बटन दबाएँ।" else "Tap Start to power on the electric motor."),
                                fontSize = 13.sp,
                                color = OlaTextMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Hero EV Scooter Graphic
                        OlaScooterHeroGraphic(
                            isIgnitionOn = state.isConnected && state.isIgnitionOn
                        )

                        // Main Elevated Ola Dashboard Card (From Screen 3)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 4.dp, shape = RoundedCornerShape(28.dp), spotColor = Color(0x20000000))
                                .clip(RoundedCornerShape(28.dp))
                                .background(OlaWhite)
                                .border(1.dp, OlaCardBorder, RoundedCornerShape(28.dp))
                                .padding(22.dp)
                        ) {
                            Column {
                                // 1. Trip Progress Bar with Live Moving Scooter Icon
                                val tripDist = state.tripMileageKm ?: 3.2f
                                val progressFrac = ((tripDist % 10.0f) / 10.0f).coerceIn(0.15f, 0.95f)
                                TripProgressBar(
                                    progressFraction = progressFrac,
                                    distanceKm = tripDist,
                                    timeLeftMin = if (state.isConnected && state.isIgnitionOn) "14:20" else "--:--",
                                    language = language
                                )

                                Spacer(modifier = Modifier.height(20.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(OlaCardBorderLight)
                                )
                                Spacer(modifier = Modifier.height(18.dp))

                                // 2. 3-Metrics Grid: Battery | Speed | Est Cost / Range
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Battery Metric
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(
                                            text = AppStrings.battery(language),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = OlaTextMuted
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val dPct = state.batteryPercent ?: 0
                                        Text(
                                            text = if (state.isConnected && state.batteryPercent != null) "${state.batteryPercent}%" else "--",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Black,
                                            color = OlaTextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(45.dp)
                                                .height(5.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(OlaSurfaceHover)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(fraction = (dPct / 100f).coerceIn(0f, 1f))
                                                    .fillMaxHeight()
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(OlaGreenPrimary)
                                            )
                                        }
                                    }

                                    // Speed Metric
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = AppStrings.speed(language),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = OlaTextMuted
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(
                                                text = if (state.isConnected && state.speedKmh != null) "%.0f".format(state.speedKmh) else "0",
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Black,
                                                color = OlaTextPrimary
                                            )
                                            Text(
                                                text = " km/h",
                                                fontSize = 11.sp,
                                                color = OlaGreenDark,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(bottom = 2.dp)
                                            )
                                        }
                                    }

                                    // Range Metric
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = AppStrings.estimatedRange(language),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = OlaTextMuted
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(
                                                text = if (state.isConnected && state.estimatedRangeKm != null) "${state.estimatedRangeKm}" else "--",
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Black,
                                                color = OlaTextPrimary
                                            )
                                            Text(
                                                text = " km",
                                                fontSize = 11.sp,
                                                color = OlaTextSecondary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(bottom = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(22.dp))

                                // 3. Primary Vehicle Control Action Buttons (Screen 3 Bottom Controls)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Main Ignition Pill Button
                                    val isIgnitionOn = state.isConnected && state.isIgnitionOn
                                    val buttonText = if (!state.isConnected) {
                                        if (language == Language.HINDI) "स्कूटी कनेक्ट करें" else "Connect Scooter"
                                    } else if (isIgnitionOn) {
                                        if (language == Language.HINDI) "राइड समाप्त (POWER OFF)" else "End Ride (Power Off)"
                                    } else {
                                        if (language == Language.HINDI) "राइड शुरू करें (POWER ON)" else "Start Ride (Power On)"
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isIgnitionOn) {
                                                    Brush.horizontalGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626)))
                                                } else {
                                                    Brush.horizontalGradient(listOf(OlaGreenAccent, OlaGreenDark))
                                                }
                                            )
                                            .clickable(enabled = state.isConnected, onClick = onToggleIgnition)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Spacer(modifier = Modifier.size(30.dp))
                                            Text(
                                                text = buttonText,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 14.sp,
                                                color = Color.White
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isIgnitionOn) Icons.Default.PlayArrow else Icons.AutoMirrored.Filled.ArrowForward,
                                                    contentDescription = null,
                                                    tint = if (isIgnitionOn) Color(0xFFDC2626) else OlaGreenDark,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Quick Padlock Anti-Theft Lock Button
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(if (state.isLocked) Color(0xFFFEF3C7) else OlaSurfaceSubtle)
                                            .border(1.5.dp, if (state.isLocked) Color(0xFFF59E0B) else OlaCardBorder, CircleShape)
                                            .clickable(enabled = state.isConnected, onClick = onToggleLock),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (state.isLocked) Icons.Default.Lock else Icons.Default.CheckCircle,
                                            contentDescription = "Lock",
                                            tint = if (state.isLocked) Color(0xFFB45309) else OlaTextPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 4. Remote Quick Controls Row (Siren Horn, Seat Solenoid, Safety Notice)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            QuickActionButton(
                                icon = Icons.Default.Notifications,
                                label = if (language == Language.HINDI) "सायरन / हॉर्न" else "Find Horn",
                                onClick = onFindVehicle,
                                enabled = state.isConnected,
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionButton(
                                icon = Icons.Default.Build,
                                label = if (language == Language.HINDI) "सीट ट्रंक खोलें" else "Open Trunk",
                                onClick = onOpenSeat,
                                enabled = state.isConnected,
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionButton(
                                icon = Icons.Default.Settings,
                                label = if (language == Language.HINDI) "कंट्रोलर्स" else "Modes",
                                onClick = onNavigateToSettings,
                                enabled = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // 5. Real Telemetry Card Details
                        TelemetryCard(state = state, language = language)

                        // 6. Safety Warning
                        SafetyWarningBanner(language = language)
                    }
                }

                DashboardTab.EXPLORE -> {
                    // ==========================================
                    // SCREEN 2: "Where to?" Route & Places
                    // ==========================================
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Stylized Vector Map with Live Route & Pulsating GPS Radar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .border(1.dp, OlaCardBorder, RoundedCornerShape(24.dp))
                        ) {
                            OlaRouteMiniMap(destinationText = if (searchQuery.isNotBlank()) searchQuery else "City Center, MG Road")
                        }

                        // Bottom "Where to?" Elevated Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 4.dp, shape = RoundedCornerShape(28.dp), spotColor = Color(0x20000000))
                                .clip(RoundedCornerShape(28.dp))
                                .background(OlaWhite)
                                .border(1.dp, OlaCardBorder, RoundedCornerShape(28.dp))
                                .padding(22.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (language == Language.HINDI) "कहाँ जाना है?" else "Where to?",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = OlaTextPrimary
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Current Location Field
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(OlaSurfaceSubtle)
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(OlaGreenPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (language == Language.HINDI) "वर्तमान स्थान" else "Current location",
                                            fontSize = 11.sp,
                                            color = OlaTextMuted
                                        )
                                        Text(
                                            text = "Koramangala, Bengaluru",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OlaTextPrimary
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "GPS",
                                        tint = OlaTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Destination Search Field
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = {
                                        Text(
                                            text = if (language == Language.HINDI) "गंतव्य या चार्जिंग स्टेशन खोजें..." else "Where are you going?",
                                            fontSize = 13.sp,
                                            color = OlaTextMuted
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = "Search", tint = OlaTextMuted)
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = OlaCardBorder,
                                        focusedBorderColor = OlaGreenPrimary,
                                        unfocusedContainerColor = OlaWhite,
                                        focusedContainerColor = OlaWhite
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Quick Places Row: Home, Work, University, Mall
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (language == Language.HINDI) "त्वरित स्थान" else "Quick Places",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = OlaTextPrimary
                                    )
                                    Text(
                                        text = if (language == Language.HINDI) "सभी देखें" else "See all",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = OlaGreenDeepDark
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    QuickPlaceChip(icon = Icons.Default.Home, name = "Home", time = "12 min")
                                    QuickPlaceChip(icon = Icons.Default.Build, name = "Work", time = "18 min")
                                    QuickPlaceChip(icon = Icons.Default.Star, name = "University", time = "15 min")
                                    QuickPlaceChip(icon = Icons.Default.ShoppingCart, name = "Mall", time = "20 min")
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Big Green Action Button
                                OlaPillButton(
                                    text = if (language == Language.HINDI) "स्कूटर से जुड़ें / नेविगेट करें" else "Find Scooter / Navigate",
                                    onClick = onNavigateToScan
                                )
                            }
                        }
                    }
                }

                DashboardTab.SETTINGS -> {
                    // ==========================================
                    // SCREEN: Settings & Vehicle Feature Hub
                    // ==========================================
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = if (language == Language.HINDI) "वाहन सेटिंग्स व नियंत्रण" else "Vehicle Settings & Control",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = OlaTextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HubCard(
                                title = AppStrings.vehicleSettings(language),
                                subtitle = "Soft/Hard Boot, Speed",
                                icon = Icons.Default.Settings,
                                onClick = onNavigateToSettings,
                                modifier = Modifier.weight(1f)
                            )
                            HubCard(
                                title = AppStrings.nfcManagement(language),
                                subtitle = "Card Read & Bind",
                                icon = Icons.Default.DateRange,
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
                                subtitle = "Live Controller Stats",
                                icon = Icons.Default.Build,
                                onClick = onNavigateToDiagnostics,
                                modifier = Modifier.weight(1f)
                            )
                            HubCard(
                                title = AppStrings.vehicleInfo(language),
                                subtitle = "Specs & Battery Model",
                                icon = Icons.Default.Info,
                                onClick = onNavigateToVehicleInfo,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HubCard(
                            title = AppStrings.appSettings(language),
                            subtitle = "Language, Preferences & Safety",
                            icon = Icons.Default.Settings,
                            onClick = onNavigateToAppSettings,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TabPill(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(CircleShape)
            .background(if (isSelected) OlaWhite else Color.Transparent)
            .shadow(elevation = if (isSelected) 2.dp else 0.dp, shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) OlaGreenDeepDark else OlaTextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                color = if (isSelected) OlaTextPrimary else OlaTextMuted
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(OlaWhite)
            .border(1.dp, OlaCardBorder, RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) OlaGreenPrimary else OlaTextMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) OlaTextPrimary else OlaTextMuted,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QuickPlaceChip(
    icon: ImageVector,
    name: String,
    time: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(OlaSurfaceSubtle)
                .border(1.dp, OlaCardBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = OlaTextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = OlaTextPrimary
        )
        Text(
            text = time,
            fontSize = 10.sp,
            color = OlaTextMuted
        )
    }
}

@Composable
private fun HubCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(OlaWhite)
            .border(1.dp, OlaCardBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(OlaGreenSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = OlaGreenPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OlaTextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = OlaTextMuted
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = OlaTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
