package com.yakuzasmart.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Notifications
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
    showHelpAction: Boolean = false,
    onHelpClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(OlaWhite)
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
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(OlaSurfaceSubtle)
                        .border(1.dp, OlaCardBorder, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = OlaTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                // Dual-Ring Green EV Logo
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(OlaGreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(OlaWhite)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = OlaTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val dotColor = when {
                        isSimulated -> OlaStatusSimulated
                        connectionStatus == ConnectionStatus.CONNECTED_IDLE ||
                        connectionStatus == ConnectionStatus.CONNECTED_SYNCING -> OlaStatusConnected
                        connectionStatus == ConnectionStatus.CONNECTING ||
                        connectionStatus == ConnectionStatus.SCANNING -> OlaStatusWarning
                        else -> OlaStatusDisconnected
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
                        fontSize = 9.sp,
                        letterSpacing = 1.2.sp,
                        color = OlaGreenPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showHelpAction) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(OlaSurfaceSubtle)
                        .border(1.dp, OlaCardBorder, RoundedCornerShape(20.dp))
                        .clickable { onHelpClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HeadsetMic,
                        contentDescription = "Help",
                        tint = OlaTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Help",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = OlaTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Language Switcher
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(OlaSurfaceSubtle)
                    .border(1.dp, OlaCardBorder, RoundedCornerShape(12.dp))
                    .clickable { onLanguageToggle() }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(
                    text = if (language == Language.ENGLISH) "हिन्दी" else "EN",
                    color = OlaGreenDeepDark,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}
