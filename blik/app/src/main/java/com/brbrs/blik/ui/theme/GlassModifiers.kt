package com.brbrs.blik.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.glassCard(
    cornerRadius: Dp = 20.dp,
    bgAlpha: Float = 0.07f,
    borderAlpha: Float = 0.10f,
    tint: Color = Color.White,
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .clip(shape)
        .background(tint.copy(alpha = bgAlpha))
        .border(1.dp, tint.copy(alpha = borderAlpha), shape)
}
