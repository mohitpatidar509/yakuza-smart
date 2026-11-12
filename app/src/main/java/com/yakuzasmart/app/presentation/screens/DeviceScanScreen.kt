package com.yakuzasmart.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yakuzasmart.app.core.localization.Language
import com.yakuzasmart.app.core.theme.*
import com.yakuzasmart.app.domain.model.BleDeviceItem
import com.yakuzasmart.app.presentation.components.YakuzaButton
import com.yakuzasmart.app.presentation.components.YakuzaTopBar

@Composable
fun DeviceScanScreen(
    scannedDevices: List<BleDeviceItem>,
    isScanning: Boolean,
    language: Language,
    onLanguageToggle: () -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onSelectDevice: (BleDeviceItem) -> Unit,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        if (!isScanning) {
            onStartScan()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YakuzaBlack)
    ) {
        YakuzaTopBar(
            title = if (language == Language.HINDI) "स्कूटर खोजें" else "Scan Devices",
            language = language,
            onLanguageToggle = onLanguageToggle,
            canNavigateBack = true,
            onNavigateBack = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isScanning) {
                        if (language == Language.HINDI) "स्कैनिंग जारी है..." else "Scanning for nearby BLE..."
                    } else {
                        if (language == Language.HINDI) "स्कैन पूरा हुआ" else "Scan Idle"
                    },
                    fontSize = 13.sp,
                    color = if (isScanning) YakuzaOrangePrimary else YakuzaTextMuted
                )

                IconButton(onClick = { if (isScanning) onStopScan() else onStartScan() }) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            color = YakuzaOrangePrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = YakuzaOrangePrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (scannedDevices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (language == Language.HINDI)
                                "कोई डिवाइस नहीं मिला\nसुनिश्चित करें कि स्कूटर की चाबी चालू है।"
                            else
                                "No Devices Found\nEnsure your scooter key is turned ON.",
                            color = YakuzaTextMuted,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(scannedDevices) { device ->
                        DeviceCard(
                            device = device,
                            language = language,
                            onConnect = { onSelectDevice(device) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            YakuzaButton(
                text = if (isScanning) {
                    if (language == Language.HINDI) "स्कैन रोकें" else "Stop Scan"
                } else {
                    if (language == Language.HINDI) "पुनः स्कैन करें" else "Scan Again"
                },
                onClick = { if (isScanning) onStopScan() else onStartScan() },
                isSecondary = isScanning
            )
        }
    }
}

@Composable
private fun DeviceCard(
    device: BleDeviceItem,
    language: Language,
    onConnect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(YakuzaDarkCharcoal, RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (device.isYakuzaDevice) YakuzaOrangePrimary.copy(alpha = 0.5f) else YakuzaCardBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable { onConnect() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = device.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = YakuzaTextPrimary
                    )
                    if (device.isYakuzaDevice) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(YakuzaOrangeContainer, RoundedCornerShape(6.dp))
                                .border(1.dp, YakuzaOrangePrimary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("YAKUZA", color = YakuzaOrangePrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${device.address}  •  RSSI: ${device.rssi} dBm",
                    fontSize = 11.sp,
                    color = YakuzaTextMuted
                )
            }

            Button(
                onClick = onConnect,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = YakuzaOrangePrimary,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(if (language == Language.HINDI) "जुड़ें" else "Connect", fontSize = 12.sp)
            }
        }
    }
}
