package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.DoomsdayCyan
import com.example.ui.theme.DoomsdayEmerald
import com.example.ui.theme.DoomsdayPurple

@Composable
fun AmbientGlow(
    enabled: Boolean,
    isPlaying: Boolean,
    primaryGlowColor: Color = DoomsdayEmerald,
    secondaryGlowColor: Color = DoomsdayCyan,
    modifier: Modifier = Modifier
) {
    if (!enabled) return

    val infiniteTransition = rememberInfiniteTransition(label = "ambientGlow")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = if (isPlaying) 0.65f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alphaGlow"
    )

    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scaleGlow"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Radial glow at top
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryGlowColor.copy(alpha = alphaAnim * 0.4f),
                    primaryGlowColor.copy(alpha = alphaAnim * 0.15f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.2f),
                radius = w * 0.6f * scaleAnim
            )
        )

        // Radial glow at bottom
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    secondaryGlowColor.copy(alpha = alphaAnim * 0.3f),
                    DoomsdayPurple.copy(alpha = alphaAnim * 0.1f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.8f),
                radius = w * 0.65f * scaleAnim
            )
        )
    }
}
