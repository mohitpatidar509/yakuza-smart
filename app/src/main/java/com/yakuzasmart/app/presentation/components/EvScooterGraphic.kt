package com.yakuzasmart.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yakuzasmart.app.core.localization.Language
import com.yakuzasmart.app.core.theme.*

/**
 * Top branding header matching the clean Ola Electric design:
 * [Green Circle Icon] YAKUZA SMART • Greener tomorrow
 */
@Composable
fun OlaBrandHeader(
    language: Language,
    onLanguageToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Iconic Dual-Ring EV Logo
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
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(OlaGreenDark)
                            .align(Alignment.Center)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "YAKUZA",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = OlaTextPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SMART",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = OlaGreenPrimary,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = "ELECTRIC MOBILITY",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = OlaTextMuted,
                    letterSpacing = 1.2.sp
                )
            }
        }

        // Language Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(OlaSurfaceSubtle)
                .border(1.dp, OlaCardBorder, RoundedCornerShape(20.dp))
                .clickable { onLanguageToggle() }
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (language == Language.ENGLISH) "हिन्दी" else "EN",
                color = OlaTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * High-end Vector/Canvas EV Scooter graphic matching the lime-green electric scooter
 * with ground drop-shadow and floating eco-leaf particles.
 */
@Composable
fun OlaScooterHeroGraphic(
    modifier: Modifier = Modifier,
    isIgnitionOn: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
    ) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val cy = h * 0.55f

        // 1. Soft Ground Plane Shadow
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x35000000), Color(0x10000000), Color.Transparent),
                center = Offset(cx, cy + 62f),
                radius = w * 0.42f
            ),
            topLeft = Offset(cx - w * 0.4f, cy + 42f),
            size = Size(w * 0.8f, 36f)
        )

        // 2. Rear Wheel (Left)
        val rearWheelCenter = Offset(cx - 100f, cy + 40f)
        drawCircle(color = Color(0xFF1E2126), radius = 32f, center = rearWheelCenter)
        drawCircle(color = Color(0xFF323740), radius = 24f, center = rearWheelCenter)
        drawCircle(color = Color(0xFFD1D5DB), radius = 16f, center = rearWheelCenter)
        drawCircle(color = Color(0xFF111417), radius = 8f, center = rearWheelCenter)

        // 3. Front Wheel (Right)
        val frontWheelCenter = Offset(cx + 96f, cy + 40f)
        drawCircle(color = Color(0xFF1E2126), radius = 32f, center = frontWheelCenter)
        drawCircle(color = Color(0xFF323740), radius = 24f, center = frontWheelCenter)
        drawCircle(color = Color(0xFFD1D5DB), radius = 16f, center = frontWheelCenter)
        drawCircle(color = Color(0xFF111417), radius = 8f, center = frontWheelCenter)

        // Front Telescopic Suspension Fork
        drawLine(
            color = Color(0xFF4B5563),
            start = Offset(cx + 96f, cy + 40f),
            end = Offset(cx + 70f, cy - 35f),
            strokeWidth = 7f,
            cap = StrokeCap.Round
        )

        // 4. Scooter Floorboard (Chassis base)
        val floorboardPath = Path().apply {
            moveTo(cx - 75f, cy + 32f)
            lineTo(cx + 50f, cy + 32f)
            lineTo(cx + 56f, cy + 18f)
            lineTo(cx - 70f, cy + 18f)
            close()
        }
        drawPath(floorboardPath, Color(0xFF262A30))

        // 5. Lime Green Bodywork (Aerodynamic Cowl & Side Fairings)
        val limeGreen = Color(0xFF8CE027)
        val limeGreenDark = Color(0xFF5AB611)
        val bodyBrush = Brush.linearGradient(
            colors = listOf(Color(0xFF9EF032), limeGreen, limeGreenDark),
            start = Offset(cx - 90f, cy - 40f),
            end = Offset(cx + 80f, cy + 30f)
        )

        // Main Curved Body Pod
        val bodyPath = Path().apply {
            moveTo(cx - 105f, cy + 15f)
            cubicTo(cx - 100f, cy - 15f, cx - 60f, cy - 25f, cx - 15f, cy - 20f)
            lineTo(cx + 25f, cy - 15f)
            cubicTo(cx + 45f, cy - 10f, cx + 55f, cy + 5f, cx + 50f, cy + 22f)
            lineTo(cx - 75f, cy + 22f)
            close()
        }
        drawPath(bodyPath, bodyBrush)

        // Front Apron (Shield)
        val apronPath = Path().apply {
            moveTo(cx + 35f, cy + 20f)
            lineTo(cx + 58f, cy - 25f)
            cubicTo(cx + 65f, cy - 50f, cx + 70f, cy - 65f, cx + 80f, cy - 70f)
            lineTo(cx + 65f, cy - 70f)
            cubicTo(cx + 50f, cy - 50f, cx + 45f, cy - 20f, cx + 22f, cy + 15f)
            close()
        }
        drawPath(apronPath, bodyBrush)

        // Front Headlight Cowl
        drawRoundRect(
            brush = bodyBrush,
            topLeft = Offset(cx + 68f, cy - 82f),
            size = Size(24f, 22f),
            cornerRadius = CornerRadius(8f, 8f)
        )

        // Dual LED Headlamp
        val headlampColor = if (isIgnitionOn) Color(0xFFFFFFFF) else Color(0xFFE2E8F0)
        drawCircle(
            color = headlampColor,
            radius = 6f,
            center = Offset(cx + 82f, cy - 71f)
        )
        if (isIgnitionOn) {
            // Headlight illumination beam
            drawCircle(
                color = Color(0x608CE027),
                radius = 12f * glowAlpha,
                center = Offset(cx + 82f, cy - 71f)
            )
        }

        // Handlebars & Mirrors
        drawLine(
            color = Color(0xFF1E2126),
            start = Offset(cx + 70f, cy - 80f),
            end = Offset(cx + 60f, cy - 90f),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = Color(0xFF111417),
            radius = 6f,
            center = Offset(cx + 58f, cy - 93f)
        )

        // 6. Contoured Black Rider Seat
        val seatPath = Path().apply {
            moveTo(cx - 95f, cy - 10f)
            cubicTo(cx - 85f, cy - 32f, cx - 45f, cy - 36f, cx - 10f, cy - 30f)
            lineTo(cx + 15f, cy - 24f)
            cubicTo(cx + 20f, cy - 22f, cx + 15f, cy - 12f, cx + 8f, cy - 10f)
            lineTo(cx - 90f, cy - 5f)
            close()
        }
        drawPath(seatPath, Color(0xFF111417))

        // Seat Silver Accent Stitching Line
        drawLine(
            color = Color(0xFF4B5563),
            start = Offset(cx - 85f, cy - 20f),
            end = Offset(cx + 8f, cy - 18f),
            strokeWidth = 2f
        )

        // 7. Floating Eco Leaves (Matching image screen 1)
        val leafGreen = Color(0xFF75D118)
        drawOval(
            color = leafGreen.copy(alpha = 0.85f),
            topLeft = Offset(cx + 120f, cy - 95f),
            size = Size(14f, 8f)
        )
        drawOval(
            color = leafGreen.copy(alpha = 0.65f),
            topLeft = Offset(cx + 138f, cy - 110f),
            size = Size(10f, 6f)
        )
        drawOval(
            color = leafGreen.copy(alpha = 0.75f),
            topLeft = Offset(cx - 115f, cy - 70f),
            size = Size(12f, 7f)
        )
    }
}

/**
 * Trip Progress Bar (from Screen 3):
 * Horizontal green route track with start dot, end destination flag,
 * and a scooter marker placed precisely on the route!
 */
@Composable
fun TripProgressBar(
    progressFraction: Float = 0.45f,
    distanceKm: Float = 3.2f,
    timeLeftMin: String = "12:45",
    language: Language = Language.ENGLISH,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Top Overview Row: Time Left | Scooter Path | Distance
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = if (language == Language.HINDI) "समय शेष" else "Time Left",
                    color = OlaTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = timeLeftMin,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = OlaTextPrimary
                    )
                    Text(
                        text = " min",
                        fontSize = 12.sp,
                        color = OlaTextSecondary,
                        modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                    )
                }
            }

            // Small Dotted Route with Scooter Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(OlaGreenSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricScooter,
                        contentDescription = "Riding",
                        tint = OlaGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (language == Language.HINDI) "दूरी" else "Distance",
                    color = OlaTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "%.1f".format(distanceKm),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = OlaTextPrimary
                    )
                    Text(
                        text = " km",
                        fontSize = 12.sp,
                        color = OlaTextSecondary,
                        modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal Route Progress Bar with Scooter Marker
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Background Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(OlaSurfaceHover)
            )

            // Active Green Track
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progressFraction.coerceIn(0.05f, 1.0f))
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(OlaGreenAccent, OlaGreenPrimary)
                        )
                    )
            )

            // Start Dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(OlaTextPrimary)
                    .align(Alignment.CenterStart)
            )

            // End Destination Dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(OlaTextMuted)
                    .align(Alignment.CenterEnd)
            )

            // Live Moving Scooter Marker
            Row(
                modifier = Modifier
                    .fillMaxWidth(fraction = progressFraction.coerceIn(0.12f, 0.92f)),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(OlaGreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricScooter,
                        contentDescription = "Scooter Marker",
                        tint = OlaWhite,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

/**
 * Stylized Vector City Map Canvas for Screen 2 ("Where to?"):
 * Renders minimalist roads, route paths, pulsating GPS radar, and location pins.
 */
@Composable
fun OlaRouteMiniMap(
    modifier: Modifier = Modifier,
    destinationText: String = "City Center, MG Road"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarPulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(Color(0xFFF3F5F7))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Grid/City Blocks
            drawRect(
                color = Color(0xFFE9ECEF),
                topLeft = Offset(w * 0.15f, h * 0.1f),
                size = Size(w * 0.3f, h * 0.35f)
            )
            drawRect(
                color = Color(0xFFE9ECEF),
                topLeft = Offset(w * 0.55f, h * 0.05f),
                size = Size(w * 0.35f, h * 0.4f)
            )
            drawRect(
                color = Color(0xFFE9ECEF),
                topLeft = Offset(w * 0.1f, h * 0.6f),
                size = Size(w * 0.4f, h * 0.35f)
            )

            // Green park space
            drawRoundRect(
                color = Color(0xFFE2F7D2),
                topLeft = Offset(w * 0.18f, h * 0.14f),
                size = Size(w * 0.24f, h * 0.25f),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // 2. Main Roads
            drawLine(
                color = Color.White,
                start = Offset(0f, h * 0.52f),
                end = Offset(w, h * 0.52f),
                strokeWidth = 26f
            )
            drawLine(
                color = Color.White,
                start = Offset(w * 0.5f, 0f),
                end = Offset(w * 0.5f, h),
                strokeWidth = 26f
            )

            // 3. Active Black Route Line
            val routePath = Path().apply {
                moveTo(w * 0.25f, h * 0.65f)
                lineTo(w * 0.45f, h * 0.52f)
                lineTo(w * 0.65f, h * 0.25f)
            }
            drawPath(
                path = routePath,
                color = Color(0xFF1E2126),
                style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 4. GPS Radar Pulse on Starting Point
            drawCircle(
                color = Color(0x302563EB),
                radius = radarPulse,
                center = Offset(w * 0.25f, h * 0.65f)
            )
            drawCircle(
                color = Color(0xFF2563EB),
                radius = 7f,
                center = Offset(w * 0.25f, h * 0.65f)
            )

            // 5. Destination Pin (Green)
            val destCenter = Offset(w * 0.65f, h * 0.25f)
            drawCircle(
                color = OlaGreenPrimary,
                radius = 16f,
                center = destCenter
            )
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = destCenter
            )
        }

        // Floating Top Card: "Where are you going? City Center"
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(OlaWhite)
                .border(1.dp, OlaCardBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(OlaGreenSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = "Location",
                            tint = OlaGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Where are you going?",
                            fontSize = 11.sp,
                            color = OlaTextMuted,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = destinationText,
                            fontSize = 14.sp,
                            color = OlaTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = OlaGreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
