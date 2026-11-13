package com.yakuzasmart.app.core.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// OLA ELECTRIC INSPIRED CLEAN MODERN PALETTE
// ==========================================

// Primary Electric Lime Green Accent & Gradients
val OlaGreenPrimary = Color(0xFF74D600)        // Vibrant Electric Lime
val OlaGreenAccent = Color(0xFF8AE02B)         // Bright Lime Highlight
val OlaGreenDark = Color(0xFF5AB611)           // Gradient End Deep Lime
val OlaGreenDeepDark = Color(0xFF356B08)       // Dark green for text on light tints
val OlaGreenSurface = Color(0xFFF2FCE8)        // Very subtle green surface tint
val OlaGreenContainer = Color(0xFFE5F8D3)      // Soft green container
val OlaGreenBorder = Color(0xFFBCEAA0)         // Light green border

// Minimalist White & Clean Light Canvas
val OlaBackground = Color(0xFFF8F9FA)          // Ultra-clean modern off-white background
val OlaWhite = Color(0xFFFFFFFF)               // Pure crisp white card surface
val OlaCardBorder = Color(0xFFEAECEF)          // Soft subtle border
val OlaCardBorderLight = Color(0xFFF1F3F5)     // Ultra-light border / divider
val OlaSurfaceSubtle = Color(0xFFF3F5F7)       // Light gray button / chip background
val OlaSurfaceHover = Color(0xFFEAEDF1)        // Interactive hover surface

// High-Contrast Clean Typography
val OlaTextPrimary = Color(0xFF111417)         // Deep Jet Black
val OlaTextSecondary = Color(0xFF505763)       // Modern Slate Gray
val OlaTextMuted = Color(0xFF8E95A2)           // Soft Subtitle Gray

// Operational Status Indicators
val OlaStatusConnected = Color(0xFF16A34A)     // Emerald Live Connected
val OlaStatusDisconnected = Color(0xFFEF4444)  // Warning Red
val OlaStatusWarning = Color(0xFFF59E0B)       // Amber / Attention
val OlaStatusSimulated = Color(0xFF2563EB)     // Royal Blue for Virtual Simulator

// ==========================================
// BACKWARD COMPATIBILITY ALIASES
// (Ensures all existing screens work seamlessly)
// ==========================================
val YakuzaBlack = OlaBackground
val YakuzaDarkCharcoal = OlaWhite
val YakuzaCardBackground = OlaSurfaceSubtle
val YakuzaCardBorder = OlaCardBorder
val YakuzaSurfaceElevated = OlaWhite

val YakuzaOrangePrimary = OlaGreenPrimary
val YakuzaOrangeAccent = OlaGreenAccent
val YakuzaOrangeContainer = OlaGreenContainer
val YakuzaOrangeGlow = OlaGreenAccent

val YakuzaTextPrimary = OlaTextPrimary
val YakuzaTextSecondary = OlaTextSecondary
val YakuzaTextMuted = OlaTextMuted

val YakuzaStatusConnected = OlaStatusConnected
val YakuzaStatusDisconnected = OlaStatusDisconnected
val YakuzaStatusWarning = OlaStatusWarning
val YakuzaStatusSimulated = OlaStatusSimulated
