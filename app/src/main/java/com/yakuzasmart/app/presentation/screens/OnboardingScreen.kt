package com.yakuzasmart.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yakuzasmart.app.core.localization.Language
import com.yakuzasmart.app.core.theme.*
import com.yakuzasmart.app.presentation.components.YakuzaButton
import com.yakuzasmart.app.presentation.components.YakuzaTopBar

@Composable
fun OnboardingScreen(
    language: Language,
    onLanguageToggle: () -> Unit,
    onGetStarted: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YakuzaBlack)
    ) {
        YakuzaTopBar(
            title = "Welcome",
            language = language,
            onLanguageToggle = onLanguageToggle
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.padding(top = 32.dp)) {
                Text(
                    text = if (language == Language.HINDI)
                        "याकुज़ा स्मार्ट में आपका स्वागत है"
                    else
                        "Welcome to\nYakuza Smart",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = YakuzaTextPrimary,
                    lineHeight = 38.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = if (language == Language.HINDI)
                        "आपके याकुज़ा इलेक्ट्रिक स्कूटर के लिए आधुनिक, सुरक्षित एवं द्विभाषी साथी ऐप।"
                    else
                        "The modern, secure, and bilingual companion for your Yakuza electric scooter.",
                    fontSize = 15.sp,
                    color = YakuzaTextSecondary,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                FeaturePoint(
                    title = if (language == Language.HINDI) "1. सुरक्षित ब्लूटूथ कनेक्शन" else "1. Reliable BLE Connection",
                    desc = if (language == Language.HINDI)
                        "न्यूनतम अनुमतियों के साथ तेज़ और सुरक्षित पेयरिंग।"
                    else
                        "Fast and verified connection using least-privilege permissions."
                )
                Spacer(modifier = Modifier.height(18.dp))
                FeaturePoint(
                    title = if (language == Language.HINDI) "2. सेटिंग्स सत्यापन (Read-Back)" else "2. Verified Setting Transactions",
                    desc = if (language == Language.HINDI)
                        "कंट्रोलर से पुष्टि के बाद ही सेटिंग को लागू माना जाता है।"
                    else
                        "Changes are only marked successful when confirmed by the controller."
                )
                Spacer(modifier = Modifier.height(18.dp))
                FeaturePoint(
                    title = if (language == Language.HINDI) "3. पर्सिस्टेंस टेस्ट व डायग्नोस्टिक्स" else "3. Persistence Testing & Diagnostics",
                    desc = if (language == Language.HINDI)
                        "जांचें कि क्या आपका कंट्रोलर चाबी बंद होने पर सेटिंग्स याद रखता है।"
                    else
                        "Verify whether your scooter controller retains settings after power-cycling."
                )
            }

            YakuzaButton(
                text = if (language == Language.HINDI) "शुरू करें" else "Get Started",
                onClick = onGetStarted
            )
        }
    }
}

@Composable
private fun FeaturePoint(title: String, desc: String) {
    Column {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = YakuzaOrangePrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = desc, fontSize = 13.sp, color = YakuzaTextSecondary, lineHeight = 18.sp)
    }
}
