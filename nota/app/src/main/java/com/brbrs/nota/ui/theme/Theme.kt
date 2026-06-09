package com.brbrs.nota.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography

// ── Dark palette ──────────────────────────────────────────────────────────────
val NavyDeep      = Color(0xFF0A0F1E)
val NavyMid       = Color(0xFF111827)
val NavySurface   = Color(0xFF1A2035)
val GlassWhite    = Color(0x14FFFFFF)
val GlassBorder   = Color(0x1AFFFFFF)

// ── Green accent (replaces cyan) ──────────────────────────────────────────────
val GreenPrimary  = Color(0xFF4CAF78)   // forest green — main accent
val GreenDim      = Color(0x9952A875)   // dimmed green
val GreenLight    = Color(0xFF2E7D52)   // deeper green for light mode readability

val VioletAccent  = Color(0xFF9370DB)
val SlateText     = Color(0xFF94A3B8)
val SlateTextDim  = Color(0x66CBD5E1)
val White         = Color(0xFFF1F5F9)
val GreenTag      = Color(0xFF34D399)
val ErrorRed      = Color(0xFFFF6B6B)

// Keep CyanPrimary as alias so existing references still compile
val CyanPrimary   = GreenPrimary
val CyanDim       = GreenDim
val CyanLight     = GreenLight

// ── Light palette ─────────────────────────────────────────────────────────────
val LightBg         = Color(0xFFF5F7FA)
val LightSurface    = Color(0xFFFFFFFF)
val LightSurface2   = Color(0xFFEDF0F5)
val LightBorder     = Color(0x1A000000)
val LightText       = Color(0xFF1E293B)
val LightTextDim    = Color(0xFF64748B)
val LightTextDimmer = Color(0xFFADB5C4)

// ── CompositionLocal for isDark ───────────────────────────────────────────────
val LocalIsDark = compositionLocalOf { true }

// ── Color schemes ─────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary          = GreenPrimary,
    onPrimary        = NavyDeep,
    primaryContainer = Color(0x1A4CAF78),
    secondary        = VioletAccent,
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
    primary          = GreenLight,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFD1F0E0),
    secondary        = VioletAccent,
    onSecondary      = Color.White,
    background       = LightBg,
    onBackground     = LightText,
    surface          = LightSurface,
    onSurface        = LightText,
    surfaceVariant   = LightSurface2,
    onSurfaceVariant = LightTextDim,
    outline          = LightBorder,
    error            = ErrorRed,
)

// ── Typography ────────────────────────────────────────────────────────────────
private val displayFont = FontFamily.Serif
private val bodyFont    = FontFamily.SansSerif

val NotaTypography = Typography(
    displayLarge  = TextStyle(fontFamily = displayFont, fontSize = 40.sp, fontWeight = FontWeight.Normal, lineHeight = 48.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontFamily = displayFont, fontSize = 32.sp, fontWeight = FontWeight.Normal, lineHeight = 40.sp, letterSpacing = (-0.3).sp),
    headlineMedium= TextStyle(fontFamily = displayFont, fontSize = 24.sp, fontWeight = FontWeight.Normal),
    titleLarge    = TextStyle(fontFamily = bodyFont,    fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    titleMedium   = TextStyle(fontFamily = bodyFont,    fontSize = 14.sp, fontWeight = FontWeight.Medium),
    bodyLarge     = TextStyle(fontFamily = bodyFont,    fontSize = 15.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontFamily = bodyFont,    fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
    labelSmall    = TextStyle(fontFamily = bodyFont,    fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.12.sp),
)

@Composable
fun NotaTheme(
    isDark: Boolean = true,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalIsDark provides isDark) {
        MaterialTheme(
            colorScheme = if (isDark) DarkColorScheme else LightColorScheme,
            typography  = NotaTypography,
            content     = content,
        )
    }
}
