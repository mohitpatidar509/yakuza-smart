package com.yakuzasmart.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yakuzasmart.app.core.localization.Language
import com.yakuzasmart.app.core.theme.*
import com.yakuzasmart.app.presentation.components.YakuzaTopBar

@Composable
fun LanguageSelectionScreen(
    currentLanguage: Language,
    onSelectLanguage: (Language) -> Unit,
    onLanguageToggle: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YakuzaBlack)
    ) {
        YakuzaTopBar(
            title = if (currentLanguage == Language.HINDI) "भाषा चयन" else "Language Selection",
            language = currentLanguage,
            onLanguageToggle = onLanguageToggle,
            canNavigateBack = true,
            onNavigateBack = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (currentLanguage == Language.HINDI)
                    "अपनी पसंदीदा भाषा चुनें (Select your preferred language):"
                else
                    "Select your preferred language:",
                fontSize = 13.sp,
                color = YakuzaTextSecondary
            )

            Language.entries.forEach { lang ->
                val isSelected = currentLanguage == lang
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isSelected) YakuzaOrangeContainer else YakuzaDarkCharcoal,
                            RoundedCornerShape(16.dp)
                        )
                        .border(
                            1.5.dp,
                            if (isSelected) YakuzaOrangePrimary else YakuzaCardBorder,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelectLanguage(lang) }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = lang.nativeName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isSelected) YakuzaOrangePrimary else YakuzaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = lang.displayName,
                                fontSize = 12.sp,
                                color = YakuzaTextMuted
                            )
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelectLanguage(lang) },
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
}
