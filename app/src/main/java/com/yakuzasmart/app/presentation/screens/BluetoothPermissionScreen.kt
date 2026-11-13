package com.yakuzasmart.app.presentation.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yakuzasmart.app.core.localization.Language
import com.yakuzasmart.app.core.security.BluetoothPermissionHelper
import com.yakuzasmart.app.core.theme.*
import com.yakuzasmart.app.presentation.components.YakuzaButton
import com.yakuzasmart.app.presentation.components.YakuzaTopBar

@Composable
fun BluetoothPermissionScreen(
    language: Language,
    onLanguageToggle: () -> Unit,
    onGrantPermissions: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onGrantPermissions()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YakuzaBlack)
    ) {
        YakuzaTopBar(
            title = if (language == Language.HINDI) "ब्लूटूथ अनुमति" else "Bluetooth Permission",
            language = language,
            onLanguageToggle = onLanguageToggle,
            canNavigateBack = true,
            onNavigateBack = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(YakuzaOrangeContainer, RoundedCornerShape(16.dp))
                        .border(1.dp, YakuzaOrangePrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Bluetooth",
                        tint = YakuzaOrangePrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (language == Language.HINDI)
                        "स्कूटर से जुड़ने के लिए ब्लूटूथ आवश्यक है"
                    else
                        "Bluetooth is Required to Connect with Scooter",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = YakuzaTextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (language == Language.HINDI)
                        "Yakuza Smart आपके इलेक्ट्रिक स्कूटर के BLE मॉड्यूल से संपर्क करने के लिए मानक Android ब्लूटूथ अनुमतियों का उपयोग करता है।"
                    else
                        "Yakuza Smart uses standard Android Bluetooth APIs to discover and communicate with your scooter's BLE controller.",
                    fontSize = 14.sp,
                    color = YakuzaTextSecondary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(YakuzaDarkCharcoal, RoundedCornerShape(14.dp))
                        .border(1.dp, YakuzaCardBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Privacy",
                            tint = YakuzaStatusConnected,
                            modifier = Modifier.size(20.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (language == Language.HINDI) "गोपनीयता सुरक्षा" else "Zero Location Tracking",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = YakuzaStatusConnected
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (language == Language.HINDI)
                                    "हम 'neverForLocation' ध्वज का उपयोग करते हैं। आपकी व्यक्तिगत लोकेशन कभी ट्रैक नहीं की जाती।"
                                else
                                    "We use 'neverForLocation' on Android 12+. Your physical location is never tracked or stored.",
                                fontSize = 12.sp,
                                color = YakuzaTextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            YakuzaButton(
                text = if (language == Language.HINDI) "अनुमति दें और आगे बढ़ें" else "Grant & Continue",
                onClick = {
                    if (BluetoothPermissionHelper.hasRequiredPermissions(context)) {
                        onGrantPermissions()
                    } else {
                        permissionLauncher.launch(BluetoothPermissionHelper.getRequiredPermissions())
                    }
                }
            )
        }
    }
}
