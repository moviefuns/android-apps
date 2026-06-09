package com.brbrs.merk.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
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

// ── Dark palette ──────────────────────────────────────────────────────────────
val NavyDeep     = Color(0xFF0A0F1E)
val NavyMid      = Color(0xFF111827)
val NavySurface  = Color(0xFF1A2035)
val GlassWhite   = Color(0x14FFFFFF)
val GlassBorder  = Color(0x1AFFFFFF)
val GlassBorderCyan = Color(0x33E53935)
val CyanPrimary  = Color(0xFFE53935)   // deep crimson — dark mode accent
val CyanDim      = Color(0x99E53935)
val SlateText    = Color(0xFF94A3B8)
val SlateTextDim = Color(0x66CBD5E1)
val White        = Color(0xFFF1F5F9)
val ErrorRed     = Color(0xFFFF6B6B)

// ── Light palette ─────────────────────────────────────────────────────────────
val LightBg         = Color(0xFFF5F7FA)
val LightSurface    = Color(0xFFFFFFFF)
val LightSurface2   = Color(0xFFEDF0F5)
val LightBorder     = Color(0x1A000000)
val LightText       = Color(0xFF1E293B)
val LightTextDim    = Color(0xFF64748B)
val LightTextDimmer = Color(0xFFADB5C4)
val CyanLight       = Color(0xFFC62828)  // deep crimson — light mode accent

// ── CompositionLocal for isDark ───────────────────────────────────────────────
val LocalIsDark = compositionLocalOf { true }

// ── Color schemes ─────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary           = CyanPrimary,
    onPrimary         = Color.White,
    primaryContainer  = Color(0x1AE53935),
    secondary         = SlateText,
    onSecondary       = White,
    background        = NavyDeep,
    onBackground      = White,
    surface           = NavyMid,
    onSurface         = White,
    surfaceVariant    = NavySurface,
    onSurfaceVariant  = SlateText,
    outline           = GlassBorder,
    error             = ErrorRed,
)

private val LightColorScheme = lightColorScheme(
    primary           = CyanLight,
    onPrimary         = Color.White,
    primaryContainer  = Color(0xFFFFCDD2),
    secondary         = LightTextDim,
    onSecondary       = Color.White,
    background        = LightBg,
    onBackground      = LightText,
    surface           = LightSurface,
    onSurface         = LightText,
    surfaceVariant    = LightSurface2,
    onSurfaceVariant  = LightTextDim,
    outline           = LightBorder,
    error             = ErrorRed,
)

// ── Typography ────────────────────────────────────────────────────────────────
val MerkTypography = Typography(
    displayLarge   = TextStyle(fontFamily = FontFamily.Serif, fontSize = 40.sp, fontWeight = FontWeight.Normal, lineHeight = 48.sp, letterSpacing = (-0.5).sp),
    headlineLarge  = TextStyle(fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Normal, lineHeight = 40.sp, letterSpacing = (-0.3).sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Normal),
    titleLarge     = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    titleMedium    = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, fontWeight = FontWeight.Medium),
    bodyLarge      = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 15.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
    labelSmall     = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.12.sp),
)

@Composable
fun BookmarksTheme(
    isDark: Boolean = true,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalIsDark provides isDark) {
        MaterialTheme(
            colorScheme = if (isDark) DarkColorScheme else LightColorScheme,
            typography  = MerkTypography,
            content     = content,
        )
    }
}
