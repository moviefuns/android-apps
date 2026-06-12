package com.brbrs.blik.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit

// ── Dark palette — deep, rich ─────────────────────────────────────────────────
val NavyDeep     = Color(0xFF080C18)   // deepest background
val NavyMid      = Color(0xFF0E1525)   // mid layer
val NavySurface  = Color(0xFF161D30)   // surface / cards
val GlassWhite   = Color(0x14FFFFFF)
val GlassBorder  = Color(0x1AFFFFFF)
val GlowAmber    = Color(0x33F57C00)   // ambient amber glow overlay

// ── Orange accent ─────────────────────────────────────────────────────────────
val CyanPrimary   = Color(0xFFFF8F00)   // golden orange — dark mode primary
val CyanLight     = Color(0xFFF57C00)   // golden orange — light mode primary
val OrangeBorder  = Color(0x33FF8F00)   // orange-tinted border for dark elevated cards
val OrangeGlow    = Color(0x1AFF8F00)   // subtle orange glow for card backgrounds

val SlateText    = Color(0xFF94A3B8)
val White        = Color(0xFFF1F5F9)
val ErrorRed     = Color(0xFFFF6B6B)
val SuccessGreen = Color(0xFF4ADE80)
val WarnYellow   = Color(0xFFFBBF24)

// ── Light palette — warm, amber-tinted ───────────────────────────────────────
val LightBg          = Color(0xFFFDF5EC)   // warm cream background
val LightSurface     = Color(0xFFFFFBF7)   // near-white warm surface
val LightSurface2    = Color(0xFFFAE8D0)   // chips, input backgrounds
val LightSurface3    = Color(0xFFF5D8B0)   // deeper surface for emphasis
val LightBorderMed   = Color(0x33E65100)   // medium amber-tinted border
val LightBorderSoft  = Color(0x22E65100)   // soft amber-tinted border
val LightText        = Color(0xFF1E0E00)   // very dark warm brown-black
val LightTextDim     = Color(0xFF7A4A1A)   // muted warm brown

val LocalIsDark = compositionLocalOf { true }

private val DarkColorScheme = darkColorScheme(
    primary          = CyanPrimary,
    onPrimary        = NavyDeep,
    primaryContainer = Color(0x1AFF8F00),
    secondary        = SlateText,
    onSecondary      = White,
    background       = NavyDeep,
    onBackground     = White,
    surface          = NavyMid,
    onSurface        = White,
    surfaceVariant   = NavySurface,
    onSurfaceVariant = SlateText,
    outline          = GlassBorder,
    error            = ErrorRed,
)

private val LightColorScheme = lightColorScheme(
    primary          = CyanLight,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFFFE0B2),
    secondary        = LightTextDim,
    onSecondary      = Color.White,
    background       = LightBg,
    onBackground     = LightText,
    surface          = LightSurface,
    onSurface        = LightText,
    surfaceVariant   = LightSurface2,
    onSurfaceVariant = LightTextDim,
    outline          = LightBorderSoft,
    error            = ErrorRed,
)

// ── Typography — expanded scale ────────────────────────────────────────────────
private val displayFont = FontFamily.Serif
private val bodyFont    = FontFamily.SansSerif

private fun TextStyle.scaled(multiplier: Float): TextStyle = copy(
    fontSize   = fontSize * multiplier,
    lineHeight = if (lineHeight != TextUnit.Unspecified) lineHeight * multiplier else lineHeight,
)

fun blikTypography(scaleMultiplier: Float = 1f): Typography {
    val base = Typography(
        displayLarge   = TextStyle(fontFamily = displayFont, fontSize = 40.sp, fontWeight = FontWeight.Normal,    lineHeight = 48.sp, letterSpacing = (-0.5).sp),
        headlineLarge  = TextStyle(fontFamily = displayFont, fontSize = 32.sp, fontWeight = FontWeight.Normal,    lineHeight = 40.sp, letterSpacing = (-0.3).sp),
        headlineMedium = TextStyle(fontFamily = displayFont, fontSize = 24.sp, fontWeight = FontWeight.Normal),
        titleLarge     = TextStyle(fontFamily = bodyFont,    fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
        titleMedium    = TextStyle(fontFamily = bodyFont,    fontSize = 14.sp, fontWeight = FontWeight.Medium),
        titleSmall     = TextStyle(fontFamily = bodyFont,    fontSize = 12.sp, fontWeight = FontWeight.Medium,    letterSpacing = 0.1.sp),
        bodyLarge      = TextStyle(fontFamily = bodyFont,    fontSize = 15.sp, fontWeight = FontWeight.Normal,    lineHeight = 24.sp),
        bodyMedium     = TextStyle(fontFamily = bodyFont,    fontSize = 13.sp, fontWeight = FontWeight.Normal,    lineHeight = 20.sp),
        bodySmall      = TextStyle(fontFamily = bodyFont,    fontSize = 11.sp, fontWeight = FontWeight.Normal,    lineHeight = 16.sp),
        labelLarge     = TextStyle(fontFamily = bodyFont,    fontSize = 12.sp, fontWeight = FontWeight.SemiBold,  letterSpacing = 0.5.sp),
        labelMedium    = TextStyle(fontFamily = bodyFont,    fontSize = 11.sp, fontWeight = FontWeight.Medium,    letterSpacing = 0.4.sp),
        labelSmall     = TextStyle(fontFamily = bodyFont,    fontSize = 10.sp, fontWeight = FontWeight.SemiBold,  letterSpacing = 0.12.sp),
    )
    if (scaleMultiplier == 1f) return base
    return Typography(
        displayLarge   = base.displayLarge.scaled(scaleMultiplier),
        headlineLarge  = base.headlineLarge.scaled(scaleMultiplier),
        headlineMedium = base.headlineMedium.scaled(scaleMultiplier),
        titleLarge     = base.titleLarge.scaled(scaleMultiplier),
        titleMedium    = base.titleMedium.scaled(scaleMultiplier),
        titleSmall     = base.titleSmall.scaled(scaleMultiplier),
        bodyLarge      = base.bodyLarge.scaled(scaleMultiplier),
        bodyMedium     = base.bodyMedium.scaled(scaleMultiplier),
        bodySmall      = base.bodySmall.scaled(scaleMultiplier),
        labelLarge     = base.labelLarge.scaled(scaleMultiplier),
        labelMedium    = base.labelMedium.scaled(scaleMultiplier),
        labelSmall     = base.labelSmall.scaled(scaleMultiplier),
    )
}

// Default (1x) typography — kept for any direct references
val BlikTypography = blikTypography(1f)

@Composable
fun BlikTheme(
    isDark: Boolean = true,
    textScaleMultiplier: Float = 1f,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalIsDark provides isDark) {
        MaterialTheme(
            colorScheme = if (isDark) DarkColorScheme else LightColorScheme,
            typography  = blikTypography(textScaleMultiplier),
            content     = content,
        )
    }
}
