package com.yakuzasmart.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.yakuzasmart.app.domain.model.YakuzaModel
import com.yakuzasmart.app.presentation.components.YakuzaTopBar

@Composable
fun VehicleInfoScreen(
    state: ScooterState,
    language: Language,
    onLanguageToggle: () -> Unit,
    onSelectModel: (YakuzaModel) -> Unit = {},
    onNavigateBack: () -> Unit
) {
    var proximityMode by remember { mutableStateOf(ProximityDistance.MIDDLE) }
    var selectedModel by remember(state.selectedModel) { mutableStateOf(state.selectedModel) }

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = if (language == Language.HINDI) "याकुज़ा इलेक्ट्रिक मॉडल चयन" else "YAKUZA SCOOTER MODEL PROFILE",
                    color = YakuzaOrangePrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == Language.HINDI)
                        "अपनी याकुज़ा स्कूटी का मॉडल चुनें ताकि सटीक स्पेसिफिकेशन व वोल्टेज दिखे:"
                    else
                        "Select your Yakuza scooter model to optimize powertrain and voltage display:",
                    fontSize = 12.sp,
                    color = YakuzaTextSecondary
                )
            }

            // Model Selection Pills
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    YakuzaModel.entries.forEach { model ->
                        val isSelected = selectedModel == model
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) YakuzaOrangeContainer else YakuzaDarkCharcoal,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) YakuzaOrangePrimary else YakuzaCardBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { 
                                    selectedModel = model 
                                    onSelectModel(model)
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (language == Language.HINDI) model.modelNameHi else model.modelName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) YakuzaOrangePrimary else YakuzaTextPrimary
                                    )
                                    Text(
                                        text = "${model.voltageNominal}  •  ${model.motorWattage}  •  Max ${model.topSpeedKmh} km/h",
                                        fontSize = 11.sp,
                                        color = YakuzaTextMuted
                                    )
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { 
                                        selectedModel = model 
                                        onSelectModel(model)
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = YakuzaOrangePrimary,
                                        unselectedColor = YakuzaTextMuted
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Battery Architecture Card
            item {
                InfoCard(
                    title = if (language == Language.HINDI) "बैटरी व वोल्टेज आर्किटेक्चर" else "Battery Voltage Architecture",
                    value = if (state.isConnected && state.batteryVoltageVolts != null) {
                        "%.1f V (Live Controller Stream)".format(state.batteryVoltageVolts)
                    } else {
                        "${selectedModel.voltageNominal} Architecture (Default)"
                    },
                    subtitle = "BMS Auto-detect compatible with 48V, 60V, and 72V packs"
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
                                "फोन पास आने पर स्वतः अनलॉक होने की संवेदनशीलता"
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
