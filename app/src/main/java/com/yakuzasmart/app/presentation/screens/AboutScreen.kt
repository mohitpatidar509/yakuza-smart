package com.yakuzasmart.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yakuzasmart.app.core.localization.Language
import com.yakuzasmart.app.core.theme.*
import com.yakuzasmart.app.presentation.components.YakuzaTopBar

@Composable
fun AboutScreen(
    language: Language,
    onLanguageToggle: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YakuzaBlack)
    ) {
        YakuzaTopBar(
            title = if (language == Language.HINDI) "ऐप विवरण एवं सुरक्षा" else "About & Safety Policy",
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(YakuzaDarkCharcoal, RoundedCornerShape(16.dp))
                        .border(1.dp, YakuzaCardBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "YAKUZA SMART",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = YakuzaOrangePrimary
                        )
                        Text(
                            text = "Native Android Companion • Version 1.0.0",
                            fontSize = 11.sp,
                            color = YakuzaTextMuted
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (language == Language.HINDI)
                                "यह एप्लिकेशन याकुज़ा इलेक्ट्रिक स्कूटरों के लिए विशेष रूप से इंजीनियर किया गया है। यह DCloud Uni-App के पुराने हाइब्रिड आर्किटेक्चर को पूरी तरह से आधुनिक Jetpack Compose एवं नेटिव Kotlin BLE स्टैक से प्रतिस्थापित करता है।"
                            else
                                "Engineered specifically for Yakuza electric scooters. Completely replaces the legacy hybrid DCloud Uni-App architecture with a pure, high-performance native Kotlin + Jetpack Compose BLE implementation.",
                            fontSize = 12.sp,
                            color = YakuzaTextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            item {
                AboutSection(
                    title = if (language == Language.HINDI) "गोपनीयता नीति (Offline Privacy Policy)" else "Offline Privacy Policy",
                    content = if (language == Language.HINDI)
                        "1. डेटा सुरक्षा: यह ऐप किसी बाहरी क्लाउड सर्वर पर आपका डेटा नहीं भेजता।\n2. लोकेशन: हम 'neverForLocation' का उपयोग करते हैं; कोई GPS डेटा एकत्रित नहीं किया जाता।\n3. लोकल स्टोरेज: सभी प्राथमिकताएं आपके डिवाइस में Jetpack DataStore में स्थानीय रूप से सुरक्षित रहती हैं।"
                    else
                        "1. Zero Telemetry Leak: This app operates completely offline without external cloud analytics or trackers.\n2. No Location Tracking: Uses Android 12+ 'neverForLocation' flag. Your GPS coordinates are never recorded.\n3. Local Cryptography: Secrets and paired tokens are stored securely in Android Keystore."
                )
            }

            item {
                AboutSection(
                    title = if (language == Language.HINDI) "कंट्रोलर सुरक्षा निर्देश" else "Safety Interlock Rules",
                    content = if (language == Language.HINDI)
                        "• राइडिंग के दौरान सेटिंग्स बदलना प्रतिबंधित है।\n• स्पीड limit और slope parking केवल 0.0 किमी/घंटा पर ही बदले जा सकते हैं।\n• समस्त हार्डवेयर परीक्षण केवल डबल मेन स्टैंड पर करें।"
                    else
                        "• Parameter modification while moving is programmatically blocked.\n• Velocity gating enforces zero speed before slope parking or speed limits can be dispatched.\n• Stationary testing on dual center stand is strictly required."
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AboutSection(title: String, content: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(YakuzaDarkCharcoal, RoundedCornerShape(16.dp))
            .border(1.dp, YakuzaCardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = YakuzaOrangePrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = content, fontSize = 12.sp, color = YakuzaTextSecondary, lineHeight = 18.sp)
        }
    }
}
