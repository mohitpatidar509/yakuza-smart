package com.yakuzasmart.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.yakuzasmart.app.core.localization.Language
import com.yakuzasmart.app.core.theme.*

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    language: Language,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(YakuzaDarkCharcoal, RoundedCornerShape(18.dp))
                .border(1.dp, YakuzaCardBorder, RoundedCornerShape(18.dp))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = if (isDestructive) YakuzaStatusDisconnected else YakuzaOrangePrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = message,
                    color = YakuzaTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YakuzaSurfaceElevated,
                            contentColor = YakuzaTextSecondary
                        )
                    ) {
                        Text(if (language == Language.HINDI) "रद्द करें" else "Cancel")
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDestructive) YakuzaStatusDisconnected else YakuzaOrangePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(if (language == Language.HINDI) "पुष्टि करें" else "Confirm")
                    }
                }
            }
        }
    }
}
