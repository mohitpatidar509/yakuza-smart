package com.yakuzasmart.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.yakuzasmart.app.domain.model.ScooterState
import com.yakuzasmart.app.presentation.components.YakuzaButton
import com.yakuzasmart.app.presentation.components.YakuzaTopBar
import kotlinx.coroutines.delay

@Composable
fun DeviceConnectionScreen(
    state: ScooterState,
    language: Language,
    onLanguageToggle: () -> Unit,
    onConnectedSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    LaunchedEffect(state.connectionStatus) {
        if (state.connectionStatus == ConnectionStatus.CONNECTED_IDLE ||
            state.connectionStatus == ConnectionStatus.CONNECTED_SYNCING) {
            delay(800)
            onConnectedSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YakuzaBlack)
    ) {
        YakuzaTopBar(
            title = if (language == Language.HINDI) "कनेक्शन स्थिति" else "Connecting",
            language = language,
            onLanguageToggle = onLanguageToggle,
            canNavigateBack = true,
            onNavigateBack = onCancel
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Box(
                    modifier = Modifier.size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isConnected) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(YakuzaStatusConnected),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Connected",
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    } else {
                        CircularProgressIndicator(
                            color = YakuzaOrangePrimary,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(70.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = when (state.connectionStatus) {
                        ConnectionStatus.CONNECTING ->
                            if (language == Language.HINDI) "स्कूटर से कनेक्ट हो रहा है..." else "Connecting to GATT Server..."
                        ConnectionStatus.DISCOVERING_SERVICES ->
                            if (language == Language.HINDI) "FFF0 सर्विसेज खोजी जा रही हैं..." else "Discovering FFF0 Services..."
                        ConnectionStatus.CONNECTED_SYNCING,
                        ConnectionStatus.CONNECTED_IDLE ->
                            if (language == Language.HINDI) "सफलतापूर्वक कनेक्टेड!" else "Successfully Connected!"
                        else ->
                            if (language == Language.HINDI) "डिस्कनेक्टेड" else "Disconnected"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = YakuzaTextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = state.connectedDevice?.displayName ?: "Yakuza Scooter",
                    fontSize = 14.sp,
                    color = YakuzaOrangePrimary
                )

                Spacer(modifier = Modifier.height(40.dp))

                ConnectionStepItem(
                    title = "GATT Handshake",
                    isDone = state.connectionStatus != ConnectionStatus.DISCONNECTED
                )
                Spacer(modifier = Modifier.height(12.dp))
                ConnectionStepItem(
                    title = "Service 0x0000FFF0 Discovery",
                    isDone = state.connectionStatus == ConnectionStatus.DISCOVERING_SERVICES || state.isConnected
                )
                Spacer(modifier = Modifier.height(12.dp))
                ConnectionStepItem(
                    title = "CCCD 0x0000FFF1 Notification Enablement",
                    isDone = state.isConnected
                )
            }

            YakuzaButton(
                text = if (state.isConnected) {
                    if (language == Language.HINDI) "डैशबोर्ड पर जाएं" else "Open Dashboard"
                } else {
                    if (language == Language.HINDI) "रद्द करें" else "Cancel"
                },
                onClick = { if (state.isConnected) onConnectedSuccess() else onCancel() },
                isSecondary = !state.isConnected
            )
        }
    }
}

@Composable
private fun ConnectionStepItem(title: String, isDone: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (isDone) YakuzaStatusConnected else YakuzaTextMuted)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            color = if (isDone) YakuzaTextPrimary else YakuzaTextMuted
        )
    }
}
