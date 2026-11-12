package com.yakuzasmart.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteForever
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
import com.yakuzasmart.app.presentation.components.ConfirmationDialog
import com.yakuzasmart.app.presentation.components.YakuzaButton
import com.yakuzasmart.app.presentation.components.YakuzaTopBar

@Composable
fun NfcManagementScreen(
    language: Language,
    onLanguageToggle: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    if (showClearDialog) {
        ConfirmationDialog(
            title = if (language == Language.HINDI) "समस्त NFC कार्ड हटाएं?" else "Clear All NFC Key Fobs?",
            message = if (language == Language.HINDI)
                "चेतावनी: सभी पंजीकृत डिजिटल चाबियां मिटा दी जाएंगी। पुनः प्रोग्राम करने के लिए मास्टर कार्ड की आवश्यकता होगी।"
            else
                "CAUTION: All registered NFC ignition cards will be erased. You will need a master key to re-program.",
            language = language,
            isDestructive = true,
            onConfirm = {
                showClearDialog = false
                statusMessage = if (language == Language.HINDI)
                    "कार्ड हटाने का कमांड भेजा गया (Opcode 0x80 Payload 0x02)"
                else
                    "Clear Card command dispatched (Opcode 0x80 Payload 0x02)"
            },
            onDismiss = { showClearDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YakuzaBlack)
    ) {
        YakuzaTopBar(
            title = AppStrings.nfcManagement(language),
            language = language,
            onLanguageToggle = onLanguageToggle,
            canNavigateBack = true,
            onNavigateBack = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = if (language == Language.HINDI)
                        "डिजिटल इग्निशन कार्ड प्रबंधन"
                    else
                        "RFID / NFC KEY FOB MANAGEMENT",
                    color = YakuzaOrangePrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                // Read Card Action
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(YakuzaDarkCharcoal, RoundedCornerShape(16.dp))
                        .border(1.dp, YakuzaCardBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = YakuzaOrangePrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (language == Language.HINDI) "कार्ड पढ़ें (Read / Register Card)" else "Enroll New NFC Card",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = YakuzaTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (language == Language.HINDI)
                                "कंट्रोलर को एनरोलमेंट मोड में डालता है। नया कार्ड मीटर पर स्वाइप करें।"
                            else
                                "Puts controller in scan mode. Swipe card against the meter console.",
                            fontSize = 11.sp,
                            color = YakuzaTextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                statusMessage = if (language == Language.HINDI)
                                    "एनरोलमेंट मोड सक्रिय। कार्ड स्वाइप करें..."
                                else
                                    "Enrollment mode active. Swipe NFC card now..."
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = YakuzaOrangePrimary)
                        ) {
                            Text(if (language == Language.HINDI) "रीड कार्ड शुरू करें" else "Start Card Enrollment", fontSize = 12.sp)
                        }
                    }
                }

                // Clear Card Action
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(YakuzaDarkCharcoal, RoundedCornerShape(16.dp))
                        .border(1.dp, YakuzaStatusDisconnected.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = YakuzaStatusDisconnected)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (language == Language.HINDI) "कार्ड हटाएं (Clear Card)" else "Clear All NFC Keys",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = YakuzaStatusDisconnected
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (language == Language.HINDI)
                                "कंट्रोलर में पंजीकृत सभी NFC कार्ड मिटा दिए जाएंगे।"
                            else
                                "Erases all paired cards from controller non-volatile memory.",
                            fontSize = 11.sp,
                            color = YakuzaTextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showClearDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = YakuzaStatusDisconnected)
                        ) {
                            Text(if (language == Language.HINDI) "सभी कार्ड मिटाएं" else "Clear All Keys", fontSize = 12.sp)
                        }
                    }
                }

                statusMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(YakuzaOrangeContainer, RoundedCornerShape(12.dp))
                            .border(1.dp, YakuzaOrangePrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(text = msg, fontSize = 12.sp, color = YakuzaOrangePrimary)
                    }
                }
            }

            YakuzaButton(
                text = if (language == Language.HINDI) "वापस जाएं" else "Back to Settings",
                onClick = onNavigateBack,
                isSecondary = true
            )
        }
    }
}
