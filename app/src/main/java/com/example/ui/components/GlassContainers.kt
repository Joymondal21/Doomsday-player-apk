package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioMode
import com.example.model.GpuApi
import com.example.model.PerformanceMode
import com.example.model.RenderEngine
import com.example.ui.theme.DolbyVisionPurple
import com.example.ui.theme.DoomsdayAmber
import com.example.ui.theme.DoomsdayCyan
import com.example.ui.theme.DoomsdayCrimson
import com.example.ui.theme.DoomsdayEmerald
import com.example.ui.theme.DoomsdayGlassBg
import com.example.ui.theme.DoomsdayGlassBorder
import com.example.ui.theme.DoomsdayObsidian
import com.example.ui.theme.DoomsdaySurface
import com.example.ui.theme.DoomsdaySurfaceVariant
import com.example.ui.theme.HdrGold
import com.example.ui.theme.TitaniumMuted
import com.example.ui.theme.TitaniumSilver
import com.example.ui.theme.TitaniumWhite
import com.example.ui.theme.VulkanRed

@Composable
fun DoomsdayGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderColor: Color = DoomsdayGlassBorder,
    backgroundColor: Color = DoomsdayGlassBg,
    glowColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val cardModifier = if (onClick != null) {
        modifier
            .clip(shape)
            .clickable(onClick = onClick)
    } else {
        modifier.clip(shape)
    }

    Box(
        modifier = cardModifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.85f),
                        backgroundColor.copy(alpha = 0.65f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        borderColor.copy(alpha = 0.8f),
                        borderColor.copy(alpha = 0.2f),
                        borderColor.copy(alpha = 0.05f)
                    )
                ),
                shape = shape
            )
            .padding(14.dp)
    ) {
        content()
    }
}

@Composable
fun DoomsdayGlowingBadge(
    text: String,
    accentColor: Color = DoomsdayEmerald,
    textColor: Color = TitaniumWhite,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = accentColor.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun EngineBadge(engine: RenderEngine) {
    val (color, label) = when (engine) {
        RenderEngine.LIB_MPV_RX -> Pair(DoomsdayEmerald, "mpv-rx (Vulkan)")
        RenderEngine.EXO_PLAYER -> Pair(DoomsdayCyan, "ExoPlayer 2.19")
        RenderEngine.LIB_VLC -> Pair(DoomsdayAmber, "libVLC")
    }
    DoomsdayGlowingBadge(text = label, accentColor = color)
}

@Composable
fun HdrBadge(isDolbyVision: Boolean = false) {
    if (isDolbyVision) {
        DoomsdayGlowingBadge(text = "DOLBY VISION", accentColor = DolbyVisionPurple)
    } else {
        DoomsdayGlowingBadge(text = "VULKAN HDR10", accentColor = HdrGold)
    }
}

@Composable
fun VulkanGpuBadge() {
    DoomsdayGlowingBadge(text = "VULKAN 1.3 DIRECT", accentColor = VulkanRed)
}

@Composable
fun DeviceOptimizedBadge() {
    DoomsdayGlowingBadge(text = "SD888 • 12GB LPDDR5", accentColor = DoomsdayCyan)
}
