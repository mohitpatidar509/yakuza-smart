package com.yakuzasmart.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yakuzasmart.app.core.theme.*

/**
 * Modern Ola-style Pill Button with vibrant Lime Green gradient,
 * crisp white text, and a circular action badge on the right.
 */
@Composable
fun OlaPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector = Icons.AutoMirrored.Filled.ArrowForward,
    isSecondary: Boolean = false
) {
    val gradientBrush = Brush.horizontalGradient(
        colors = if (enabled) {
            listOf(OlaGreenAccent, OlaGreenDark)
        } else {
            listOf(Color(0xFFD1D5DB), Color(0xFF9CA3AF))
        }
    )

    if (isSecondary) {
        // Clean White Outlined Pill Button
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(CircleShape)
                .background(OlaWhite)
                .border(1.5.dp, OlaCardBorder, CircleShape)
                .clickable(enabled = enabled && !isLoading, onClick = onClick)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = OlaGreenPrimary,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (enabled) OlaTextPrimary else OlaTextMuted,
                    letterSpacing = 0.3.sp
                )
            }
        }
    } else {
        // Primary Vibrant Gradient Pill Button with Circular Arrow Disc
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(elevation = if (enabled) 6.dp else 0.dp, shape = CircleShape, spotColor = OlaGreenPrimary)
                .clip(CircleShape)
                .background(gradientBrush)
                .clickable(enabled = enabled && !isLoading, onClick = onClick)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.size(32.dp)) // balance center

                    Text(
                        text = text,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = Color.White,
                        letterSpacing = 0.4.sp
                    )

                    // Right Circular Action Disc
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (enabled) OlaGreenDark else OlaTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Backward compatibility alias for YakuzaButton
 */
@Composable
fun YakuzaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    isSecondary: Boolean = false
) {
    OlaPillButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isLoading = isLoading,
        isSecondary = isSecondary
    )
}
