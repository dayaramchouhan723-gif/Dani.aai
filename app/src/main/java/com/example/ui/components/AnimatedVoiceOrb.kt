package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.AssistantPersona
import com.example.model.AssistantState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedVoiceOrb(
    state: AssistantState,
    rmsLevel: Float, // 0.0f .. 1.0f from mic or speech
    persona: AssistantPersona = AssistantPersona.GF_MODE,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "DaniOrbMotion")

    // Breathing pulse (0.9 to 1.1)
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbBreathing"
    )

    // Orbital rotation for ambient energy
    val rotationDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRotation"
    )

    // Fast rotation for thinking state
    val fastRotationDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbFastRotation"
    )

    // Dynamic color transition per state and persona
    val primaryColor by animateColorAsState(
        targetValue = when (state) {
            AssistantState.LISTENING -> Color(0xFFFF1744) // Electric Red Ripple
            AssistantState.THINKING -> Color(0xFF00E5FF)  // Radiant Cyan Particles
            AssistantState.SPEAKING -> Color(0xFFE040FB)  // Luminous Purple Waveform
            AssistantState.ERROR -> Color(0xFFFF5252)
            AssistantState.IDLE -> {
                if (persona == AssistantPersona.GF_MODE) Color(0xFFFF4081) // Kitsune Rose
                else Color(0xFF9C27B0) // Cosmic Purple
            }
        },
        animationSpec = tween(350),
        label = "PrimaryOrbColor"
    )

    val secondaryColor by animateColorAsState(
        targetValue = when (state) {
            AssistantState.LISTENING -> Color(0xFFFF5252)
            AssistantState.THINKING -> Color(0xFF18FFFF)
            AssistantState.SPEAKING -> Color(0xFF7C4DFF)
            AssistantState.ERROR -> Color(0xFFB71C1C)
            AssistantState.IDLE -> {
                if (persona == AssistantPersona.GF_MODE) Color(0xFFFFD54F) // Golden Border
                else Color(0xFF673AB7)
            }
        },
        animationSpec = tween(350),
        label = "SecondaryOrbColor"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension * 0.32f

            val speechAmp = if (state == AssistantState.LISTENING || state == AssistantState.SPEAKING) {
                rmsLevel.coerceIn(0f, 1f) * 0.40f
            } else {
                0f
            }
            val currentRadius = baseRadius * (breathingScale + speechAmp)

            // 1. Outermost Cosmic Aura Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.40f),
                        secondaryColor.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentRadius * 1.7f
                ),
                radius = currentRadius * 1.7f,
                center = center
            )

            // 2. Listening: 3 Concentric Expanding Ripple Sound Rings
            if (state == AssistantState.LISTENING) {
                val ripple1 = currentRadius * (1.18f + rmsLevel * 0.35f)
                val ripple2 = currentRadius * (1.40f + rmsLevel * 0.55f)
                val ripple3 = currentRadius * (1.65f + rmsLevel * 0.75f)

                drawCircle(
                    color = primaryColor.copy(alpha = (0.6f - rmsLevel * 0.2f).coerceAtLeast(0.15f)),
                    radius = ripple1,
                    center = center,
                    style = Stroke(width = 3f)
                )
                drawCircle(
                    color = secondaryColor.copy(alpha = (0.45f - rmsLevel * 0.2f).coerceAtLeast(0.1f)),
                    radius = ripple2,
                    center = center,
                    style = Stroke(width = 2f)
                )
                drawCircle(
                    color = primaryColor.copy(alpha = (0.3f - rmsLevel * 0.15f).coerceAtLeast(0.05f)),
                    radius = ripple3,
                    center = center,
                    style = Stroke(width = 1.2f)
                )
            }

            // 3. Orbiting Energy Particles (Thinking / Idle)
            val orbitAngle = if (state == AssistantState.THINKING) fastRotationDeg else rotationDeg
            val numParticles = if (state == AssistantState.THINKING) 5 else 3
            for (i in 0 until numParticles) {
                val angleOffset = (i * (360f / numParticles) + orbitAngle) * (Math.PI / 180.0)
                val arcDist = currentRadius * (1.15f + if (state == AssistantState.THINKING) 0.1f else 0f)
                val orbitX = center.x + (arcDist * cos(angleOffset)).toFloat()
                val orbitY = center.y + (arcDist * sin(angleOffset)).toFloat()

                drawCircle(
                    color = if (i % 2 == 0) primaryColor else secondaryColor,
                    radius = if (state == AssistantState.THINKING) 6.5f else 4.5f,
                    center = Offset(orbitX, orbitY)
                )
            }

            // 4. Core Metallic Orb Sphere
            val coreGradient = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.90f),
                    primaryColor,
                    secondaryColor,
                    Color(0xFF0A0A0A)
                ),
                center = Offset(center.x - currentRadius * 0.28f, center.y - currentRadius * 0.28f),
                radius = currentRadius * 1.15f
            )

            drawCircle(
                brush = coreGradient,
                radius = currentRadius,
                center = center
            )

            // 5. Specular 3D Gloss Highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.75f),
                        Color.Transparent
                    ),
                    center = Offset(center.x - currentRadius * 0.35f, center.y - currentRadius * 0.35f),
                    radius = currentRadius * 0.42f
                ),
                radius = currentRadius * 0.42f,
                center = Offset(center.x - currentRadius * 0.35f, center.y - currentRadius * 0.35f)
            )

            // 6. Speaking Soundwave Vibration across core
            if (state == AssistantState.SPEAKING) {
                val barCount = 7
                val spacing = (currentRadius * 1.3f) / barCount
                val startX = center.x - (currentRadius * 0.65f)
                for (j in 0 until barCount) {
                    val barX = startX + j * spacing
                    val factor = sin((j * 0.7f + fastRotationDeg * 0.12f)) * 0.5f + 0.5f
                    val barHeight = currentRadius * 0.6f * (factor.coerceIn(0.25f, 1f))
                    drawLine(
                        color = Color.White.copy(alpha = 0.95f),
                        start = Offset(barX, center.y - barHeight / 2),
                        end = Offset(barX, center.y + barHeight / 2),
                        strokeWidth = 3.5f
                    )
                }
            }
        }
    }
}
