package com.yakuzasmart.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Warning
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
            .clip(RoundedCornerShape(24.dp))
            .background(OlaWhite)
            .border(1.dp, OlaCardBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            // Speed and Battery Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Speed Section
                Column {
                    Text(
                        text = AppStrings.speed(language).uppercase(),
                        color = OlaTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (state.isConnected && state.speedKmh != null) {
                                "%.0f".format(state.speedKmh)
                            } else {
                                "0"
                            },
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = if (state.isConnected) OlaTextPrimary else OlaTextMuted
                        )
                        Text(
                            text = " km/h",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = OlaGreenPrimary,
                            modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                        )
                    }
                }

                // Battery Section with Green Horizontal Bar
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (state.isCharging) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Charging",
                                tint = OlaGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == Language.HINDI) "चार्जिंग" else "CHARGING",
                                color = OlaGreenPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = AppStrings.battery(language).uppercase(),
                            color = OlaTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (state.isConnected) "${state.batteryPercent}%" else "--",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = if (state.isConnected) OlaTextPrimary else OlaTextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    // Horizontal Battery Indicator Pill
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(OlaSurfaceHover)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = (state.batteryPercent / 100f).coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (state.batteryPercent > 20) OlaGreenPrimary else OlaStatusDisconnected
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(OlaCardBorderLight)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 4-Column Secondary Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn(
                    label = AppStrings.estimatedRange(language),
                    value = if (state.isConnected) "${state.estimatedRangeKm} km" else "--"
                )
                MetricColumn(
                    label = AppStrings.totalMileage(language),
                    value = if (state.isConnected && state.totalMileageKm != null) "%.0f km".format(state.totalMileageKm) else "--"
                )
                MetricColumn(
                    label = AppStrings.batteryVoltage(language),
                    value = if (state.isConnected && state.batteryVoltageVolts != null) "%.1f V".format(state.batteryVoltageVolts) else "--"
                )
                MetricColumn(
                    label = AppStrings.motorCurrent(language),
                    value = if (state.isConnected && state.motorCurrentAmps != null) "%.1f A".format(state.motorCurrentAmps) else "--"
                )
            }

            // Fault Bitmask Indicators
            val activeFaults = mutableListOf<String>()
            if (state.faults.kickstandDown) activeFaults.add(if (language == Language.HINDI) "साइड स्टैंड" else "Side Stand")
            if (state.faults.brakeEngaged) activeFaults.add(if (language == Language.HINDI) "ब्रेक सक्रिय" else "Brake Engaged")
            if (state.faults.controllerError) activeFaults.add(if (language == Language.HINDI) "कंट्रोलर खराबी" else "Controller Err")
            if (state.faults.motorHallError) activeFaults.add(if (language == Language.HINDI) "हॉल सेंसर" else "Hall Sensor")
            if (state.faults.undervoltage) activeFaults.add(if (language == Language.HINDI) "कम वोल्टेज" else "Low Voltage")
            if (state.faults.throttleError) activeFaults.add(if (language == Language.HINDI) "एक्सेलरेटर" else "Throttle Err")

            if (activeFaults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(OlaStatusWarning.copy(alpha = 0.12f))
                        .border(1.dp, OlaStatusWarning.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = OlaStatusWarning,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = activeFaults.joinToString(" • "),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricColumn(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            color = OlaTextMuted,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = OlaTextPrimary
        )
    }
}
