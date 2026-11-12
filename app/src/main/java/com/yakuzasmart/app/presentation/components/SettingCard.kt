package com.yakuzasmart.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yakuzasmart.app.core.localization.Language
import com.yakuzasmart.app.core.theme.*
import com.yakuzasmart.app.domain.model.SettingCategory
import com.yakuzasmart.app.domain.model.SettingRecord
import com.yakuzasmart.app.domain.model.TransactionStage

@Composable
fun SettingCard(
    category: SettingCategory,
    currentRecord: SettingRecord?,
    language: Language,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = if (language == Language.HINDI) category.titleHi else category.titleEn
    val currentOption = currentRecord?.requestedOption
    val stage = currentRecord?.stage ?: TransactionStage.IDLE

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(YakuzaDarkCharcoal, RoundedCornerShape(16.dp))
            .border(1.dp, YakuzaCardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = YakuzaTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(stage = stage, language = language)
                }
                Spacer(modifier = Modifier.height(4.dp))
                val optionLabel = if (language == Language.HINDI) {
                    currentOption?.labelHi ?: "डिफ़ॉल्ट"
                } else {
                    currentOption?.labelEn ?: "Default"
                }
                Text(
                    text = "Current: $optionLabel",
                    fontSize = 12.sp,
                    color = YakuzaOrangePrimary
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open",
                tint = YakuzaTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
