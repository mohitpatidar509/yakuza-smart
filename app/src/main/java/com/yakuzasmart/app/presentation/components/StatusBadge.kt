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
import androidx.compose.ui.text.font.FontWeight
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
            Color(0xFFDCFCE7),
            Color(0xFF15803D),
            Color(0xFF86EFAC)
        )
        TransactionStage.MISMATCH_WARNING -> Quadruple(
            if (language == Language.HINDI) "बेमेल" else "MISMATCH",
            Color(0xFFFEF3C7),
            Color(0xFFB45309),
            Color(0xFFFDE68A)
        )
        TransactionStage.TRANSMITTING,
        TransactionStage.AWAITING_CONTROLLER_ACK,
        TransactionStage.READING_BACK -> Quadruple(
            if (language == Language.HINDI) "सिंक हो रहा है" else "SYNCING",
            Color(0xFFF1FCE8),
            Color(0xFF4D7C0F),
            Color(0xFFBEF264)
        )
        TransactionStage.TIMEOUT_ERROR -> Quadruple(
            if (language == Language.HINDI) "समय समाप्त" else "TIMEOUT",
            Color(0xFFFEE2E2),
            Color(0xFFB91C1C),
            Color(0xFFFECACA)
        )
        TransactionStage.BLOCKED_BY_SAFETY -> Quadruple(
            if (language == Language.HINDI) "अवरुद्ध" else "BLOCKED",
            Color(0xFFFEE2E2),
            Color(0xFFB91C1C),
            Color(0xFFFECACA)
        )
        TransactionStage.IDLE,
        TransactionStage.VALIDATING_SAFETY -> Quadruple(
            if (language == Language.HINDI) "लंबित" else "PENDING",
            Color(0xFFF3F4F6),
            Color(0xFF4B5563),
            Color(0xFFE5E7EB)
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
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
