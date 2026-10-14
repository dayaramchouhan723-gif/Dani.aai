package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun WaveformVisualizer(
    isWaveActive: Boolean,
    amplitude: Float,
    barColor: Color,
    modifier: Modifier = Modifier,
    barCount: Int = 20, // 20-bar live amplitude equalizer as requested
    maxBarHeight: Dp = 32.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformBars20")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val animFactor by infiniteTransition.animateFloat(
                initialValue = 0.15f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 280 + (i * 45) % 350,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "BarHeight_$i"
            )

            val currentHeight = if (isWaveActive) {
                val amp = (amplitude * 1.6f).coerceIn(0.20f, 1.0f)
                (maxBarHeight.value * animFactor * amp).coerceIn(3.5f, maxBarHeight.value).dp
            } else {
                3.5.dp
            }

            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height(currentHeight)
                    .background(
                        color = barColor.copy(alpha = if (isWaveActive) 0.95f else 0.30f),
                        shape = RoundedCornerShape(1.5.dp)
                    )
            )
        }
    }
}
