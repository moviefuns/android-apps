package com.brbrs.bookmarks.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter

private val FALLBACK_COLORS = listOf(
    Color(0xFF1A6B9A), Color(0xFF9A3A1A), Color(0xFF1A9A5A),
    Color(0xFF6B1A9A), Color(0xFF9A8A1A), Color(0xFF1A4A9A),
)

@Composable
fun FaviconImage(
    url: String,
    title: String,
    size: Dp = 36.dp,
) {
    var useFallback by remember(url) { mutableStateOf(url.isBlank()) }
    val letter = title.firstOrNull { it.isLetter() }?.uppercaseChar()?.toString() ?: "B"
    val color  = FALLBACK_COLORS[title.hashCode().and(0x7FFFFFFF) % FALLBACK_COLORS.size]

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(color),
    ) {
        if (!useFallback) {
            AsyncImage(
                model     = url,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier  = Modifier.size(size),
                onState   = { state ->
                    if (state is AsyncImagePainter.State.Error) useFallback = true
                },
            )
        } else {
            Text(
                text       = letter,
                color      = Color.White,
                fontSize   = (size.value * 0.42f).sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
