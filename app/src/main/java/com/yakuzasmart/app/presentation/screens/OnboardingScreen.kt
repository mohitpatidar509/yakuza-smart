package com.yakuzasmart.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yakuzasmart.app.core.localization.Language
import com.yakuzasmart.app.core.theme.*
import com.yakuzasmart.app.presentation.components.OlaBrandHeader
import com.yakuzasmart.app.presentation.components.OlaPillButton
import com.yakuzasmart.app.presentation.components.OlaScooterHeroGraphic

/**
 * Screen 1 from user reference:
 * Minimalist White EV Onboarding with "Smart rides. Greener tomorrow.",
 * Hero EV Scooter with floating eco-leaves, and vibrant Lime Green pill button.
 */
@Composable
fun OnboardingScreen(
    language: Language,
    onLanguageToggle: () -> Unit,
    onGetStarted: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OlaBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // 1. Top EV Branding Header
            OlaBrandHeader(
                language = language,
                onLanguageToggle = onLanguageToggle
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Bold Clean Headlines
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = if (language == Language.HINDI)
                        "स्मार्ट यात्रा।\nहरित कल।"
                    else
                        "Smart rides.\nGreener tomorrow.",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = OlaTextPrimary,
                    lineHeight = 38.sp,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (language == Language.HINDI)
                        "कम लागत। शून्य उत्सर्जन।\nअधिकतम सुविधा।"
                    else
                        "Low cost. Zero emissions.\nMaximum convenience.",
                    fontSize = 14.sp,
                    color = OlaTextMuted,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Hero Modern EV Scooter Graphic (with ground shadow & leaves)
            OlaScooterHeroGraphic(
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 4. Bottom Action Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OlaPillButton(
                text = if (language == Language.HINDI) "स्कूटर से जुड़ें" else "Scan Your Scooter",
                onClick = onGetStarted,
                icon = Icons.AutoMirrored.Filled.ArrowForward
            )

            OlaPillButton(
                text = if (language == Language.HINDI) "डैशबोर्ड पर जाएँ" else "Go to Dashboard",
                onClick = onGetStarted,
                isSecondary = true
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (language == Language.HINDI)
                    "याकुज़ा स्मार्ट मोबिलिटी द्वारा संचालित"
                else
                    "Powered by Yakuza Smart Mobility",
                fontSize = 11.sp,
                color = OlaTextMuted,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
