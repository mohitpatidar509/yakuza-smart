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
import com.yakuzasmart.app.presentation.components.YakuzaTopBar

@Composable
fun SmartFeaturesScreen(
    language: Language,
    onLanguageToggle: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var smartHelmet by remember { mutableStateOf(false) }
    var passwordUnlock by remember { mutableStateOf(false) }
    var childMode by remember { mutableStateOf(false) }
    var welcomeMode by remember { mutableStateOf(true) }
    var quietMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YakuzaBlack)
    ) {
        YakuzaTopBar(
            title = AppStrings.smartFeatures(language),
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
                    text = if (language == Language.HINDI) "स्मार्ट सुरक्षा व कम्फर्ट विकल्प" else "SMART MOBILITY CONTROLS",
                    color = YakuzaOrangePrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                ToggleCard(
                    title = if (language == Language.HINDI) "स्मार्ट हेलमेट (Smart Helmet)" else "Smart Helmet Interlock",
                    desc = if (language == Language.HINDI) "हेलमेट पहने बिना मोटर स्टार्ट नहीं होगी।" else "Requires wireless helmet beacon connection before motor unlocks.",
                    checked = smartHelmet,
                    onCheckedChange = { smartHelmet = it }
                )
            }

            item {
                ToggleCard(
                    title = if (language == Language.HINDI) "पासवर्ड अनलॉक (Password Unlock)" else "Password Unlock PIN",
                    desc = if (language == Language.HINDI) "वाहन शुरू करने के लिए 4-अंकीय पिन आवश्यक होगा।" else "Prompts for a 4-digit PIN on display meter before driving.",
                    checked = passwordUnlock,
                    onCheckedChange = { passwordUnlock = it }
                )
            }

            item {
                ToggleCard(
                    title = if (language == Language.HINDI) "चाइल्ड मोड (Child Mode)" else "Child Safety Mode",
                    desc = if (language == Language.HINDI) "अधिकतम गति को 10 किमी/घंटा और टॉर्क को सीमित करता है।" else "Locks speed to 10 km/h and restricts acceleration torque.",
                    checked = childMode,
                    onCheckedChange = { childMode = it }
                )
            }

            item {
                ToggleCard(
                    title = if (language == Language.HINDI) "वेलकम मोड (Welcome Mode)" else "Welcome Light Animation",
                    desc = if (language == Language.HINDI) "ब्लूटूथ कनेक्ट होते ही एलईडी हेडलाइट अभिवादन चालू होता है।" else "Illuminates courtesy headlights when phone connects over BLE.",
                    checked = welcomeMode,
                    onCheckedChange = { welcomeMode = it }
                )
            }

            item {
                ToggleCard(
                    title = if (language == Language.HINDI) "क्वाइट मोड (Quiet Mode)" else "Quiet Mode (Mute Chimes)",
                    desc = if (language == Language.HINDI) "स्कूटर के सभी बीप और कंफर्मेशन आवाजों को म्यूट करता है।" else "Silences all system confirmation beeps and startup chimes.",
                    checked = quietMode,
                    onCheckedChange = { quietMode = it }
                )
            }
        }
    }
}

@Composable
private fun ToggleCard(
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(YakuzaDarkCharcoal, RoundedCornerShape(16.dp))
            .border(1.dp, YakuzaCardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = YakuzaTextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = desc, fontSize = 11.sp, color = YakuzaTextSecondary, lineHeight = 16.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = YakuzaOrangePrimary,
                    checkedTrackColor = YakuzaOrangeContainer
                )
            )
        }
    }
}
