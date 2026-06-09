package com.brbrs.bookmarks.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Palette ──────────────────────────────────────────────────────────────────
val NavyBg          = Color(0xFF0D1117)
val SurfaceCard     = Color(0xFF141923)
val SurfaceElevated = Color(0xFF1A2030)
val CyanPrimary     = Color(0xFF63B3ED)
val CyanDim         = Color(0x2963B3ED)
val GlassBorder     = Color(0x1AFFFFFF)
val GlassBorderCyan = Color(0x3363B3ED)
val SlateText       = Color(0xFFE2E8F0)
val SlateTextDim    = Color(0xFF8892A4)
val SlateTextMuted  = Color(0xFF4A5568)
val ErrorRed        = Color(0xFFFC8181)

private val DarkColors = darkColorScheme(
    primary          = CyanPrimary,
    onPrimary        = NavyBg,
    primaryContainer = CyanDim,
    onPrimaryContainer = CyanPrimary,
    secondary        = SlateTextDim,
    onSecondary      = NavyBg,
    background       = NavyBg,
    onBackground     = SlateText,
    surface          = SurfaceCard,
    onSurface        = SlateText,
    surfaceVariant   = SurfaceElevated,
    onSurfaceVariant = SlateTextDim,
    outline          = GlassBorder,
    error            = ErrorRed,
    onError          = NavyBg,
)

@Composable
fun BookmarksTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content     = content,
    )
}
