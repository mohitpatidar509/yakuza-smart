package com.yakuzasmart.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yakuzasmart.app.core.localization.Language
import com.yakuzasmart.app.core.theme.*
import com.yakuzasmart.app.domain.model.TransactionStage

@Composable
fun StatusBadge(
    stage: TransactionStage,
    language: Language,
    modifier: Modifier = Modifier
) {
    val (label, bgColor, textColor, borderColor) = when (stage) {
        TransactionStage.VERIFIED_SUCCESS -> Quadruple(
            if (language == Language.HINDI) "सत्यापित" else "VERIFIED",
            YakuzaStatusConnected.copy(alpha = 0.15f),
            YakuzaStatusConnected,
            YakuzaStatusConnected.copy(alpha = 0.4f)
        )
        TransactionStage.MISMATCH_WARNING -> Quadruple(
            if (language == Language.HINDI) "बेमेल" else "MISMATCH",
            YakuzaStatusWarning.copy(alpha = 0.15f),
            YakuzaStatusWarning,
            YakuzaStatusWarning.copy(alpha = 0.4f)
        )
        TransactionStage.TRANSMITTING,
        TransactionStage.AWAITING_CONTROLLER_ACK,
        TransactionStage.READING_BACK -> Quadruple(
            if (language == Language.HINDI) "सिंक हो रहा है" else "SYNCING",
            YakuzaOrangePrimary.copy(alpha = 0.15f),
            YakuzaOrangePrimary,
            YakuzaOrangePrimary.copy(alpha = 0.4f)
        )
        TransactionStage.TIMEOUT_ERROR -> Quadruple(
            if (language == Language.HINDI) "टाइमआउट" else "TIMEOUT",
            YakuzaStatusDisconnected.copy(alpha = 0.15f),
            YakuzaStatusDisconnected,
            YakuzaStatusDisconnected.copy(alpha = 0.4f)
        )
        TransactionStage.BLOCKED_BY_SAFETY -> Quadruple(
            if (language == Language.HINDI) "अवरुद्ध" else "BLOCKED",
            YakuzaStatusDisconnected.copy(alpha = 0.15f),
            YakuzaStatusDisconnected,
            YakuzaStatusDisconnected.copy(alpha = 0.4f)
        )
        TransactionStage.IDLE,
        TransactionStage.VALIDATING_SAFETY -> Quadruple(
            if (language == Language.HINDI) "सत्यापन लंबित" else "PENDING",
            YakuzaTextMuted.copy(alpha = 0.15f),
            YakuzaTextSecondary,
            YakuzaTextMuted.copy(alpha = 0.3f)
        )
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
