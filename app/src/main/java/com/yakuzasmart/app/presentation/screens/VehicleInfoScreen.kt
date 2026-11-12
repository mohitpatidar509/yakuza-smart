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
import com.yakuzasmart.app.domain.model.ProximityDistance
import com.yakuzasmart.app.domain.model.ScooterState
import com.yakuzasmart.app.presentation.components.YakuzaTopBar

@Composable
fun VehicleInfoScreen(
    state: ScooterState,
    language: Language,
    onLanguageToggle: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var proximityMode by remember { mutableStateOf(ProximityDistance.MIDDLE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YakuzaBlack)
    ) {
        YakuzaTopBar(
            title = AppStrings.vehicleInfo(language),
            language = language,
            onLanguageToggle = onLanguageToggle,
            canNavigateBack = true,
            onNavigateBack = onNavigateBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = if (language == Language.HINDI) "वाहन एवं हार्डवेयर विवरण" else "HARDWARE & CONTROLLER SPECS",
                    color = YakuzaOrangePrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Voltage Card
            item {
                InfoCard(
                    title = if (language == Language.HINDI) "वोल्टेज सिस्टम (Voltage System)" else "Battery System Voltage",
                    value = if (state.isConnected && state.batteryVoltageVolts != null) {
                        "%.1f V (Nominal 60V Architecture)".format(state.batteryVoltageVolts)
                    } else {
                        "60V Standard Architecture (Disconnected)"
                    },
                    subtitle = if (language == Language.HINDI)
                        "48V / 60V / 72V ऑटो-डिटेक्ट कंट्रोलर"
                    else
                        "48V / 60V / 72V Auto-detect BMS link"
                )
            }

            // Induction Mode (Proximity Keyless)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(YakuzaDarkCharcoal, RoundedCornerShape(16.dp))
                        .border(1.dp, YakuzaCardBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = if (language == Language.HINDI) "इंडक्शन दूरी (Induction Distance)" else "Proximity Keyless Distance",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = YakuzaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (language == Language.HINDI)
                                "पास आने पर फोन से स्वतः अनलॉक होने की संवेदनशीलता"
                            else
                                "BLE beacon unlock range when approaching scooter",
                            fontSize = 11.sp,
                            color = YakuzaTextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProximityDistance.entries.forEach { dist ->
                                val isSelected = proximityMode == dist
                                Button(
                                    onClick = { proximityMode = dist },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) YakuzaOrangePrimary else YakuzaSurfaceElevated,
                                        contentColor = if (isSelected) androidx.compose.ui.graphics.Color.White else YakuzaTextSecondary
                                    )
                                ) {
                                    Text(if (language == Language.HINDI) dist.labelHi else dist.labelEn, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            item {
                InfoCard(
                    title = "BLE Service UUID",
                    value = "0000FFF0-0000-1000-8000-00805F9B34FB",
                    subtitle = "Notify: 0x0000FFF1 • Write: 0x0000FFF2"
                )
            }

            item {
                InfoCard(
                    title = "Controller Protocol",
                    value = "Yakuza Frame v1.0 (Sync 0x88 0x01)",
                    subtitle = "Framed Modulo-256 Checksum"
                )
            }

            item {
                InfoCard(
                    title = "Connected Hardware MAC",
                    value = state.connectedDevice?.address ?: "None",
                    subtitle = if (state.isSimulated) "Simulated In-Memory Controller" else "Physical Bluetooth Peripheral"
                )
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, value: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(YakuzaDarkCharcoal, RoundedCornerShape(16.dp))
            .border(1.dp, YakuzaCardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(text = title, fontSize = 11.sp, color = YakuzaTextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = YakuzaTextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 11.sp, color = YakuzaOrangePrimary)
        }
    }
}
