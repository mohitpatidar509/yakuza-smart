package com.yakuzasmart.app.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OlaLightColorScheme = lightColorScheme(
    primary = OlaGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = OlaGreenContainer,
    onPrimaryContainer = OlaGreenDeepDark,
    secondary = OlaGreenAccent,
    onSecondary = Color.White,
    background = OlaBackground,
    onBackground = OlaTextPrimary,
    surface = OlaWhite,
    onSurface = OlaTextPrimary,
    surfaceVariant = OlaSurfaceSubtle,
    onSurfaceVariant = OlaTextSecondary,
    outline = OlaCardBorder,
    error = OlaStatusDisconnected,
    onError = Color.White
)

@Composable
fun YakuzaSmartTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OlaLightColorScheme,
        typography = YakuzaTypography,
        content = content
    )
}
