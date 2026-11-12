package com.yakuzasmart.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yakuzasmart.app.core.localization.AppStrings
import com.yakuzasmart.app.core.localization.Language
import com.yakuzasmart.app.core.theme.*

@Composable
fun SafetyWarningBanner(
    language: Language,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(YakuzaOrangeContainer.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .border(1.dp, YakuzaOrangePrimary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Safety Alert",
                tint = YakuzaOrangePrimary,
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = if (language == Language.HINDI) "सुरक्षा निर्देश (Safety Notice)" else "Safety & Testing Notice",
                    color = YakuzaOrangePrimary,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = AppStrings.stationarySafetyWarning(language),
                    color = YakuzaTextPrimary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
