package com.yakuzasmart.app.presentation.screens

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yakuzasmart.app.core.localization.Language
import com.yakuzasmart.app.core.security.BluetoothPermissionHelper
import com.yakuzasmart.app.core.theme.*
import com.yakuzasmart.app.domain.model.BleDeviceItem
import com.yakuzasmart.app.presentation.components.YakuzaButton
import com.yakuzasmart.app.presentation.components.YakuzaTopBar

@Composable
fun DeviceScanScreen(
    scannedDevices: List<BleDeviceItem>,
    isScanning: Boolean,
    isBluetoothEnabled: Boolean,
    scanError: String?,
    language: Language,
    onLanguageToggle: () -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onSelectDevice: (BleDeviceItem) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // Runtime Permission Request Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val allGranted = permissionsMap.values.all { it }
        if (allGranted && isBluetoothEnabled) {
            onStartScan()
        }
    }

    LaunchedEffect(isBluetoothEnabled) {
        if (isBluetoothEnabled) {
            if (BluetoothPermissionHelper.hasRequiredPermissions(context)) {
                onStartScan()
            } else {
                permissionLauncher.launch(BluetoothPermissionHelper.getRequiredPermissions())
            }
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
            // 1. Bluetooth Disabled Warning
            if (!isBluetoothEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(YakuzaStatusWarning.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                        .border(1.5.dp, YakuzaStatusWarning, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BluetoothDisabled, contentDescription = null, tint = YakuzaStatusWarning)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == Language.HINDI) "मोबाइल का ब्लूटूथ बंद है" else "Bluetooth is Turned OFF",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = YakuzaStatusWarning
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (language == Language.HINDI)
                                "स्कूटी स्कैन करने के लिए कृपया पहले अपने मोबाइल का ब्लूटूथ चालू करें।"
                            else
                                "Please turn on your phone's Bluetooth to discover nearby Yakuza scooters.",
                            fontSize = 12.sp,
                            color = YakuzaTextPrimary
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
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = YakuzaStatusWarning)
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
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 2. Scan Error Alert if any
            scanError?.let { err ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(YakuzaStatusDisconnected.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .border(1.dp, YakuzaStatusDisconnected.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = YakuzaStatusDisconnected)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = err, color = YakuzaStatusDisconnected, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 3. Scan Status Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isScanning) {
                        if (language == Language.HINDI) "स्कैनिंग जारी है..." else "Scanning for nearby BLE..."
                    } else {
                        if (language == Language.HINDI) "स्कैन रुका हुआ है" else "Scan Idle"
                    },
                    fontSize = 13.sp,
                    color = if (isScanning) YakuzaOrangePrimary else YakuzaTextMuted
                )

                IconButton(
                    onClick = {
                        if (isScanning) {
                            onStopScan()
                        } else {
                            if (!BluetoothPermissionHelper.hasRequiredPermissions(context)) {
                                permissionLauncher.launch(BluetoothPermissionHelper.getRequiredPermissions())
                            } else {
                                onStartScan()
                            }
                        }
                    }
                ) {
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

            // 4. Device List
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
                                "कोई डिवाइस नहीं मिला\nसुनिश्चित करें कि स्कूटी की चाबी चालू है और ब्लूटूथ ऑन है।"
                            else
                                "No Devices Found\nEnsure your scooter key is turned ON and Bluetooth is active.",
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
                onClick = {
                    if (isScanning) {
                        onStopScan()
                    } else {
                        if (!BluetoothPermissionHelper.hasRequiredPermissions(context)) {
                            permissionLauncher.launch(BluetoothPermissionHelper.getRequiredPermissions())
                        } else {
                            onStartScan()
                        }
                    }
                },
                isSecondary = isScanning,
                enabled = isBluetoothEnabled
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
