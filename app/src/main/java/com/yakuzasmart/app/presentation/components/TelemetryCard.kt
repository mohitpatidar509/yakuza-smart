package com.yakuzasmart.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yakuzasmart.app.core.localization.AppStrings
import com.yakuzasmart.app.core.localization.Language
import com.yakuzasmart.app.core.theme.*
import com.yakuzasmart.app.domain.model.ScooterState

@Composable
fun TelemetryCard(
    state: ScooterState,
    language: Language,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(YakuzaDarkCharcoal, RoundedCornerShape(20.dp))
            .border(1.dp, YakuzaCardBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            // Speed and Battery Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = AppStrings.speed(language).uppercase(),
                        color = YakuzaTextMuted,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (state.isConnected && state.speedKmh != null) {
                                "%.0f".format(state.speedKmh)
                            } else {
                                "--"
                            },
                            fontSize = 46.sp,
                            fontWeight = FontWeight.Black,
                            color = if (state.isConnected) YakuzaOrangePrimary else YakuzaTextMuted
                        )
                        Text(
                            text = " km/h",
                            fontSize = 14.sp,
                            color = YakuzaTextSecondary,
                            modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                        )
                    }
                }

                // Battery Pill
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (state.isCharging) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Charging",
                                tint = YakuzaStatusConnected,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == Language.HINDI) "चार्जिंग" else "CHARGING",
                                color = YakuzaStatusConnected,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = AppStrings.battery(language).uppercase(),
                            color = YakuzaTextMuted,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (state.batteryPercent != null && state.batteryPercent > 20)
                                        YakuzaStatusConnected
                                    else if (state.batteryPercent != null)
                                        YakuzaStatusWarning
                                    else
                                        YakuzaTextMuted
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.isConnected && state.batteryPercent != null) {
                                "${state.batteryPercent}%"
                            } else {
                                "-- %"
                            },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = YakuzaTextPrimary
                        )
                    }
                    if (state.isConnected && state.batteryPercent == null) {
                        Text(
                            text = if (language == Language.HINDI) "लाइव डेटा प्रतीक्षा..." else "Awaiting live data...",
                            fontSize = 10.sp,
                            color = YakuzaTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-metrics Grid (Range, Voltage, Current, Temp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = AppStrings.estimatedRange(language),
                    value = if (state.isConnected && state.estimatedRangeKm != null) "${state.estimatedRangeKm} km" else "--"
                )
                MetricItem(
                    label = AppStrings.voltage(language),
                    value = if (state.isConnected && state.batteryVoltageVolts != null) "%.1f V".format(state.batteryVoltageVolts) else "--"
                )
                MetricItem(
                    label = AppStrings.motorCurrent(language),
                    value = if (state.isConnected && state.motorCurrentAmps != null) "%.1f A".format(state.motorCurrentAmps) else "--"
                )
                MetricItem(
                    label = AppStrings.controllerTemp(language),
                    value = if (state.isConnected && state.controllerTempCelsius != null) "${state.controllerTempCelsius}°C" else "--"
                )
            }

            // Odometer & Trip row
            if (state.isConnected && (state.totalMileageKm != null || state.tripMileageKm != null)) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(YakuzaCardBackground, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = (if (language == Language.HINDI) "कुल दूरी (ODO): " else "Odo: ") +
                                (if (state.totalMileageKm != null) "%.0f km".format(state.totalMileageKm) else "--"),
                        fontSize = 11.sp,
                        color = YakuzaTextSecondary
                    )
                    Text(
                        text = (if (language == Language.HINDI) "ट्रिप: " else "Trip: ") +
                                (if (state.tripMileageKm != null) "%.1f km".format(state.tripMileageKm) else "--"),
                        fontSize = 11.sp,
                        color = YakuzaTextSecondary
                    )
                }
            }

            // Fault Warnings Bar if active
            if (state.faults.hasFault) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(YakuzaStatusDisconnected.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .border(1.dp, YakuzaStatusDisconnected.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = if (language == Language.HINDI)
                            "चेतावनी: वाहन में खराबी (Brake/Motor/Temp Fault Detected)"
                        else
                            "WARNING: Vehicle Fault Detected (Check Diagnostics)",
                        color = YakuzaStatusDisconnected,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (state.isSimulated) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (language == Language.HINDI)
                        "[सिम्युलेटर मोड सक्रिय: डेटा आभासी कंट्रोलर द्वारा जनरेटेड है]"
                    else
                        "[SIMULATOR ACTIVE: Data generated by in-memory mock controller]",
                    color = YakuzaStatusSimulated,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column {
        Text(text = label, color = YakuzaTextMuted, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = YakuzaTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
