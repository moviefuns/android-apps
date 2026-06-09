package com.brbrs.blik.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val NavyDeep     = Color(0xFF0A0F1E)
val NavyMid      = Color(0xFF111827)
val NavySurface  = Color(0xFF1A2035)
val GlassWhite   = Color(0x14FFFFFF)
val GlassBorder  = Color(0x1AFFFFFF)
val CyanPrimary  = Color(0xFFFF8F00)   // golden orange — dark mode primary
val SlateText    = Color(0xFF94A3B8)
val White        = Color(0xFFF1F5F9)
val ErrorRed     = Color(0xFFFF6B6B)
val SuccessGreen = Color(0xFF4ADE80)
val WarnYellow   = Color(0xFFFBBF24)

val LightBg         = Color(0xFFF5F7FA)
val LightSurface    = Color(0xFFFFFFFF)
val LightSurface2   = Color(0xFFEDF0F5)
val LightBorder     = Color(0x1A000000)
val LightText       = Color(0xFF1E293B)
val LightTextDim    = Color(0xFF64748B)
val CyanLight       = Color(0xFFF57C00)  // golden orange — light mode primary

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
    outline          = LightBorder,
    error            = ErrorRed,
)

val BlikTypography = Typography(
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
fun BlikTheme(isDark: Boolean = true, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalIsDark provides isDark) {
        MaterialTheme(
            colorScheme = if (isDark) DarkColorScheme else LightColorScheme,
            typography  = BlikTypography,
            content     = content,
        )
    }
}
