package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DoomsdayAmber
import com.example.ui.theme.DoomsdayCyan
import com.example.ui.theme.DoomsdayEmerald
import com.example.ui.theme.TitaniumWhite
import kotlinx.coroutines.delay

@Composable
fun GestureCinemaController(
    onToggleControls: () -> Unit,
    onSeekRelative: (seconds: Int) -> Unit,
    onScrubSeek: (percent: Float) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    currentBrightness: Float,
    currentVolume: Float,
    skipStepSeconds: Int = 10,
    onPinchZoom: ((zoomChange: Float, panChange: Offset) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showBrightnessOsd by remember { mutableStateOf(false) }
    var showVolumeOsd by remember { mutableStateOf(false) }
    var brightnessVal by remember { mutableFloatStateOf(currentBrightness) }
    var volumeVal by remember { mutableFloatStateOf(currentVolume) }

    LaunchedEffect(currentVolume) {
        volumeVal = currentVolume
    }
    LaunchedEffect(currentBrightness) {
        brightnessVal = currentBrightness
    }

    var showDoubleTapLeft by remember { mutableStateOf(false) }
    var showDoubleTapRight by remember { mutableStateOf(false) }
    var doubleTapCountLeft by remember { mutableIntStateOf(0) }
    var doubleTapCountRight by remember { mutableIntStateOf(0) }

    // Auto-hide indicators
    LaunchedEffect(showBrightnessOsd, brightnessVal) {
        if (showBrightnessOsd) {
            delay(1200)
            showBrightnessOsd = false
        }
    }

    LaunchedEffect(showVolumeOsd, volumeVal) {
        if (showVolumeOsd) {
            delay(1200)
            showVolumeOsd = false
        }
    }

    LaunchedEffect(showDoubleTapLeft, doubleTapCountLeft) {
        if (showDoubleTapLeft) {
            delay(800)
            showDoubleTapLeft = false
            doubleTapCountLeft = 0
        }
    }

    LaunchedEffect(showDoubleTapRight, doubleTapCountRight) {
        if (showDoubleTapRight) {
            delay(800)
            showDoubleTapRight = false
            doubleTapCountRight = 0
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    if (zoom != 1.0f || pan != Offset.Zero) {
                        onPinchZoom?.invoke(zoom, pan)
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onToggleControls() },
                    onDoubleTap = { offset ->
                        val isLeft = offset.x < size.width * 0.45f
                        val isRight = offset.x > size.width * 0.55f
                        if (isLeft) {
                            doubleTapCountLeft += skipStepSeconds
                            showDoubleTapLeft = true
                            onSeekRelative(-skipStepSeconds)
                        } else if (isRight) {
                            doubleTapCountRight += skipStepSeconds
                            showDoubleTapRight = true
                            onSeekRelative(skipStepSeconds)
                        } else {
                            onToggleControls()
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                var isBrightness = false
                var isVolume = false
                var isHorizontalScrub = false

                detectDragGestures(
                    onDragStart = { offset ->
                        if (offset.x < size.width * 0.35f) {
                            isBrightness = true
                            isVolume = false
                            isHorizontalScrub = false
                        } else if (offset.x > size.width * 0.65f) {
                            isVolume = true
                            isBrightness = false
                            isHorizontalScrub = false
                        } else {
                            isHorizontalScrub = true
                            isBrightness = false
                            isVolume = false
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (isBrightness) {
                            val delta = -dragAmount.y / (size.height * 0.7f)
                            brightnessVal = (brightnessVal + delta).coerceIn(0.05f, 1.0f)
                            showBrightnessOsd = true
                            onBrightnessChange(brightnessVal)
                        } else if (isVolume) {
                            // Support volume up to 2.0f (200% volume boost)
                            val delta = -dragAmount.y / (size.height * 0.6f)
                            volumeVal = (volumeVal + delta).coerceIn(0.0f, 2.0f)
                            showVolumeOsd = true
                            onVolumeChange(volumeVal)
                        } else if (isHorizontalScrub) {
                            val deltaPercent = dragAmount.x / size.width
                            onScrubSeek(deltaPercent)
                        }
                    },
                    onDragEnd = {
                        isBrightness = false
                        isVolume = false
                        isHorizontalScrub = false
                    }
                )
            }
    ) {
        // Brightness OSD (Left Center)
        AnimatedVisibility(
            visible = showBrightnessOsd,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 28.dp)
        ) {
            CinemaGestureOsd(
                icon = if (brightnessVal > 0.5f) Icons.Default.BrightnessMedium else Icons.Default.BrightnessLow,
                title = "BRIGHTNESS",
                value = brightnessVal,
                accentColor = DoomsdayAmber,
                isBoost = false
            )
        }

        // Volume OSD (Right Center with 200% Boost visual)
        AnimatedVisibility(
            visible = showVolumeOsd,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 28.dp)
        ) {
            val isBoost = volumeVal > 1.0f
            val accent = if (isBoost) Color(0xFFFF5252) else DoomsdayCyan
            val icon = when {
                isBoost -> Icons.Default.Bolt
                volumeVal == 0f -> Icons.Default.VolumeMute
                volumeVal > 0.5f -> Icons.Default.VolumeUp
                else -> Icons.Default.VolumeDown
            }
            CinemaGestureOsd(
                icon = icon,
                title = if (isBoost) "BOOST" else "VOLUME",
                value = volumeVal,
                accentColor = accent,
                isBoost = isBoost
            )
        }

        // Double Tap Left Ripple & Counter
        AnimatedVisibility(
            visible = showDoubleTapLeft,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 48.dp)
        ) {
            DoubleTapSkipIndicator(
                directionBackward = true,
                seconds = doubleTapCountLeft
            )
        }

        // Double Tap Right Ripple & Counter
        AnimatedVisibility(
            visible = showDoubleTapRight,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 48.dp)
        ) {
            DoubleTapSkipIndicator(
                directionBackward = false,
                seconds = doubleTapCountRight
            )
        }
    }
}

@Composable
private fun CinemaGestureOsd(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: Float,
    accentColor: Color,
    isBoost: Boolean = false
) {
    val percent = (value * 100).toInt()
    Box(
        modifier = Modifier
            .width(88.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xEE0D1322),
                        Color(0xDD070A11)
                    )
                )
            )
            .border(
                1.5.dp,
                if (isBoost) Color(0xFFFF5252).copy(alpha = 0.85f) else accentColor.copy(alpha = 0.6f),
                RoundedCornerShape(16.dp)
            )
            .padding(vertical = 14.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(28.dp)
            )

            // Vertical Progress Bar (handles 0% to 200%)
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .height(96.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0x40FFFFFF))
            ) {
                // Base 0 to 100%
                val fillRatio = (value / 2.0f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fillRatio)
                        .align(Alignment.BottomCenter)
                        .background(
                            if (isBoost) {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFF5252),
                                        DoomsdayAmber,
                                        DoomsdayCyan
                                    )
                                )
                            } else {
                                Brush.verticalGradient(listOf(accentColor, accentColor))
                            }
                        )
                )
            }

            Text(
                text = "$percent%",
                color = if (isBoost) Color(0xFFFF5252) else TitaniumWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = title,
                color = accentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun DoubleTapSkipIndicator(
    directionBackward: Boolean,
    seconds: Int
) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        DoomsdayEmerald.copy(alpha = 0.45f),
                        DoomsdayEmerald.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                )
            )
            .border(1.5.dp, DoomsdayEmerald.copy(alpha = 0.8f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (directionBackward) Icons.Default.FastRewind else Icons.Default.FastForward,
                contentDescription = null,
                tint = DoomsdayEmerald,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = if (directionBackward) "-${seconds}s" else "+${seconds}s",
                color = TitaniumWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
