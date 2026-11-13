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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
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
    RIDE,      // Screen 3: Live Ride
    EXPLORE,   // Screen 2: Where To & Map
    SETTINGS   // Screen 1: Vehicle & App Settings
}

data class DashboardActions(
    val onLanguageToggle: () -> Unit = {},
    val onConnectSavedScooter: () -> Unit = {},
    val onForgetSavedScooter: () -> Unit = {},
    val onNavigateToScan: () -> Unit = {},
    val onDisconnect: () -> Unit = {},
    val onToggleIgnition: () -> Unit = {},
    val onToggleLock: () -> Unit = {},
    val onFindVehicle: () -> Unit = {},
    val onOpenSeat: () -> Unit = {},
    val onNavigateToSettings: () -> Unit = {},
    val onNavigateToNfc: () -> Unit = {},
    val onNavigateToDiagnostics: () -> Unit = {},
    val onNavigateToAppSettings: () -> Unit = {},
    val onNavigateToVehicleInfo: () -> Unit = {}
)

@Composable
fun DashboardScreen(
    state: ScooterState,
    savedScooter: BleDeviceItem?,
    isBluetoothEnabled: Boolean,
    language: Language,
    actions: DashboardActions
) {
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
            onLanguageToggle = actions.onLanguageToggle,
            connectionStatus = state.connectionStatus,
            isSimulated = state.isSimulated,
            showHelpAction = true,
            onHelpClick = actions.onNavigateToVehicleInfo
        )

        // 2. Segmented Pill Tab Bar (Ride | Explore | Settings)
        DashboardTabBar(
            selectedTab = selectedTab,
            language = language,
            onSelectTab = { selectedTab = it }
        )

        // 3. Scrollable Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bluetooth warning banner
            if (!isBluetoothEnabled) {
                DashboardBluetoothWarning(language = language)
            }

            // Connection status / Saved scooter / No scooter card
            DashboardConnectionHeader(
                state = state,
                savedScooter = savedScooter,
                isBluetoothEnabled = isBluetoothEnabled,
                language = language,
                actions = actions
            )

            // Content based on selected tab
            when (selectedTab) {
                DashboardTab.RIDE -> DashboardRideTab(
                    state = state,
                    language = language,
                    actions = actions
                )
                DashboardTab.EXPLORE -> DashboardExploreTab(
                    language = language,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it }
                )
                DashboardTab.SETTINGS -> DashboardSettingsTab(
                    language = language,
                    actions = actions
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Backward-compatible overload
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
    DashboardScreen(
        state = state,
        savedScooter = savedScooter,
        isBluetoothEnabled = isBluetoothEnabled,
        language = language,
        actions = DashboardActions(
            onLanguageToggle = onLanguageToggle,
            onConnectSavedScooter = onConnectSavedScooter,
            onForgetSavedScooter = onForgetSavedScooter,
            onNavigateToScan = onNavigateToScan,
            onDisconnect = onDisconnect,
            onToggleIgnition = onToggleIgnition,
            onToggleLock = onToggleLock,
            onFindVehicle = onFindVehicle,
            onOpenSeat = onOpenSeat,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToNfc = onNavigateToNfc,
            onNavigateToDiagnostics = onNavigateToDiagnostics,
            onNavigateToAppSettings = onNavigateToAppSettings,
            onNavigateToVehicleInfo = onNavigateToVehicleInfo
        )
    )
}

@Composable
private fun DashboardTabBar(
    selectedTab: DashboardTab,
    language: Language,
    onSelectTab: (DashboardTab) -> Unit
) {
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
            onClick = { onSelectTab(DashboardTab.RIDE) },
            modifier = Modifier.weight(1f)
        )
        TabPill(
            title = if (language == Language.HINDI) "मार्ग व खोज" else "Where To",
            icon = Icons.Default.LocationOn,
            isSelected = selectedTab == DashboardTab.EXPLORE,
            onClick = { onSelectTab(DashboardTab.EXPLORE) },
            modifier = Modifier.weight(1f)
        )
        TabPill(
            title = if (language == Language.HINDI) "सेटिंग्स" else "Settings",
            icon = Icons.Default.Settings,
            isSelected = selectedTab == DashboardTab.SETTINGS,
            onClick = { onSelectTab(DashboardTab.SETTINGS) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DashboardBluetoothWarning(language: Language) {
    val context = LocalContext.current
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
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(enableBtIntent)
                    } catch (e: Exception) {
                        try {
                            context.startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                        } catch (ex: Exception) {}
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

@Composable
private fun DashboardConnectionHeader(
    state: ScooterState,
    savedScooter: BleDeviceItem?,
    isBluetoothEnabled: Boolean,
    language: Language,
    actions: DashboardActions
) {
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
                    onClick = actions.onDisconnect,
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
                            .clickable { actions.onForgetSavedScooter() }
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
                        onClick = actions.onConnectSavedScooter,
                        enabled = isBluetoothEnabled,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = actions.onNavigateToScan,
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
                    onClick = actions.onNavigateToScan,
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
}

@Composable
private fun DashboardRideTab(
    state: ScooterState,
    language: Language,
    actions: DashboardActions
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Ride Status Header
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

        // Main Elevated Ola Dashboard Card
        ElevatedRideCard(
            state = state,
            language = language,
            onToggleIgnition = actions.onToggleIgnition,
            onToggleLock = actions.onToggleLock
        )

        // Remote Quick Controls Row (Siren Horn, Seat Solenoid, Controller Modes)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionButton(
                icon = Icons.Default.Notifications,
                label = if (language == Language.HINDI) "सायरन / हॉर्न" else "Find Horn",
                onClick = actions.onFindVehicle,
                enabled = state.isConnected,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Default.Lock,
                label = if (language == Language.HINDI) "सीट ट्रंक खोलें" else "Open Trunk",
                onClick = actions.onOpenSeat,
                enabled = state.isConnected,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Default.Settings,
                label = if (language == Language.HINDI) "कंट्रोलर्स" else "Modes",
                onClick = actions.onNavigateToSettings,
                enabled = true,
                modifier = Modifier.weight(1f)
            )
        }

        // Telemetry details card
        TelemetryCard(state = state, language = language)

        // Safety warning banner
        SafetyWarningBanner(language = language)
    }
}

@Composable
private fun ElevatedRideCard(
    state: ScooterState,
    language: Language,
    onToggleIgnition: () -> Unit,
    onToggleLock: () -> Unit
) {
    val isIgnitionOn = state.isConnected && state.isIgnitionOn
    val tripDist = state.tripMileageKm ?: 3.2f
    val progressFrac = ((tripDist % 10.0f) / 10.0f).coerceIn(0.15f, 0.95f)

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
            // 1. Trip Progress Bar
            TripProgressBar(
                progressFraction = progressFrac,
                distanceKm = tripDist,
                timeLeftMin = if (isIgnitionOn) "14:20" else "--:--",
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

            // 2. 3-Metrics Grid: Battery | Speed | Estimated Range
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
                                .fillMaxWidth(fraction = (dPct / 100f).coerceIn(0.02f, 1f))
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

            // 3. Action Controls: Ignition Pill & Lock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                                imageVector = if (isIgnitionOn) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = if (isIgnitionOn) Color(0xFFDC2626) else OlaGreenDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

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
}

@Composable
private fun DashboardExploreTab(
    language: Language,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, OlaCardBorder, RoundedCornerShape(24.dp))
        ) {
            OlaRouteMiniMap(destinationText = if (searchQuery.isNotBlank()) searchQuery else "City Center, MG Road")
        }

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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(OlaSurfaceSubtle)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(OlaGreenPrimary)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == Language.HINDI) "वर्तमान स्थान" else "Current Location",
                            fontSize = 10.sp,
                            color = OlaTextMuted,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Indore • Sector 4",
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

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = {
                        Text(
                            text = if (language == Language.HINDI) "गंतव्य स्थान खोजें..." else "Where are you going?",
                            color = OlaTextMuted,
                            fontSize = 13.sp
                        )
                    },
                    trailingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = OlaTextMuted)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OlaGreenPrimary,
                        unfocusedBorderColor = OlaCardBorder,
                        focusedContainerColor = OlaWhite,
                        unfocusedContainerColor = OlaWhite
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = if (language == Language.HINDI) "त्वरित स्थान" else "Quick Places",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = OlaTextMuted,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickPlaceChip(icon = Icons.Default.Home, name = "Home", time = "12 min")
                    QuickPlaceChip(icon = Icons.Default.Star, name = "Work", time = "18 min")
                    QuickPlaceChip(icon = Icons.Default.Star, name = "University", time = "15 min")
                    QuickPlaceChip(icon = Icons.Default.ShoppingCart, name = "Mall", time = "20 min")
                }

                Spacer(modifier = Modifier.height(20.dp))

                OlaPillButton(
                    text = if (language == Language.HINDI) "राइड खोजें" else "Find a Ride",
                    onClick = {}
                )
            }
        }
    }
}

@Composable
private fun DashboardSettingsTab(
    language: Language,
    actions: DashboardActions
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            Text(
                text = if (language == Language.HINDI) "वाहन एवं सिस्टम सेटिंग्स" else "Vehicle & System Hub",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = OlaTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (language == Language.HINDI)
                    "कंट्रोलर मोड्स, एनएफसी कार्ड, सुरक्षा और डायग्नोस्टिक्स प्रबंधित करें।"
                else
                    "Manage controller modes, NFC digital keys, safety interlocks and live telemetry.",
                fontSize = 13.sp,
                color = OlaTextMuted
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HubCard(
                title = AppStrings.vehicleSettings(language),
                subtitle = "Soft/Hard Boot, Speed",
                icon = Icons.Default.Settings,
                onClick = actions.onNavigateToSettings,
                modifier = Modifier.weight(1f)
            )
            HubCard(
                title = AppStrings.nfcManagement(language),
                subtitle = "Card Read & Bind",
                icon = Icons.Default.CheckCircle,
                onClick = actions.onNavigateToNfc,
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
                icon = Icons.Default.Settings,
                onClick = actions.onNavigateToDiagnostics,
                modifier = Modifier.weight(1f)
            )
            HubCard(
                title = AppStrings.vehicleInfo(language),
                subtitle = "Specs & Battery Model",
                icon = Icons.Default.Info,
                onClick = actions.onNavigateToVehicleInfo,
                modifier = Modifier.weight(1f)
            )
        }

        HubCard(
            title = AppStrings.appSettings(language),
            subtitle = "Language, Preferences & Safety",
            icon = Icons.Default.Settings,
            onClick = actions.onNavigateToAppSettings,
            modifier = Modifier.fillMaxWidth()
        )
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
    val bg = if (isSelected) OlaWhite else Color.Transparent
    val textCol = if (isSelected) OlaTextPrimary else OlaTextMuted
    val iconCol = if (isSelected) OlaGreenPrimary else OlaTextMuted

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconCol, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = title, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium, color = textCol)
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
            .clip(RoundedCornerShape(20.dp))
            .background(OlaWhite)
            .border(1.dp, OlaCardBorder, RoundedCornerShape(20.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (enabled) OlaGreenSurface else OlaSurfaceSubtle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (enabled) OlaGreenPrimary else OlaTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
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
                .size(46.dp)
                .clip(CircleShape)
                .background(OlaSurfaceSubtle)
                .border(1.dp, OlaCardBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = OlaGreenPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            fontSize = 12.sp,
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(OlaGreenSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = OlaGreenPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
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
