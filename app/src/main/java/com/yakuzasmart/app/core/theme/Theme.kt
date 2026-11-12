package com.yakuzasmart.app.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val YakuzaDarkColorScheme = darkColorScheme(
    primary = YakuzaOrangePrimary,
    onPrimary = Color.White,
    primaryContainer = YakuzaOrangeContainer,
    onPrimaryContainer = YakuzaOrangeGlow,
    secondary = YakuzaOrangeAccent,
    onSecondary = Color.White,
    background = YakuzaBlack,
    onBackground = YakuzaTextPrimary,
    surface = YakuzaDarkCharcoal,
    onSurface = YakuzaTextPrimary,
    surfaceVariant = YakuzaCardBackground,
    onSurfaceVariant = YakuzaTextSecondary,
    outline = YakuzaCardBorder,
    error = YakuzaStatusDisconnected,
    onError = Color.White
)

@Composable
fun YakuzaSmartTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = YakuzaDarkColorScheme,
        typography = YakuzaTypography,
        content = content
    )
}
