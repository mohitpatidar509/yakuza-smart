package com.yakuzasmart.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yakuzasmart.app.core.localization.Language
import com.yakuzasmart.app.core.theme.*
import com.yakuzasmart.app.domain.model.ConnectionStatus

@Composable
fun YakuzaTopBar(
    title: String,
    language: Language,
    onLanguageToggle: () -> Unit,
    canNavigateBack: Boolean = false,
    onNavigateBack: () -> Unit = {},
    connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    isSimulated: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(YakuzaBlack)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (canNavigateBack) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(38.dp)
                        .background(YakuzaDarkCharcoal, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = YakuzaOrangePrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = YakuzaTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val dotColor = when {
                        isSimulated -> YakuzaStatusSimulated
                        connectionStatus == ConnectionStatus.CONNECTED_IDLE ||
                        connectionStatus == ConnectionStatus.CONNECTED_SYNCING -> YakuzaStatusConnected
                        connectionStatus == ConnectionStatus.CONNECTING ||
                        connectionStatus == ConnectionStatus.SCANNING -> YakuzaStatusWarning
                        else -> YakuzaStatusDisconnected
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
                if (!canNavigateBack) {
                    Text(
                        text = "YAKUZA SMART MOBILITY",
                        fontSize = 10.sp,
                        letterSpacing = 1.2.sp,
                        color = YakuzaOrangePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .background(YakuzaDarkCharcoal, RoundedCornerShape(12.dp))
                .border(1.dp, YakuzaCardBorder, RoundedCornerShape(12.dp))
                .clickable { onLanguageToggle() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (language == Language.ENGLISH) "हिन्दी" else "EN",
                color = YakuzaOrangePrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}
