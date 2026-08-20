package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DiagnosticTelemetry
import com.example.model.PlayerSettings
import com.example.ui.theme.DoomsdayAmber
import com.example.ui.theme.DoomsdayCyan
import com.example.ui.theme.DoomsdayCrimson
import com.example.ui.theme.DoomsdayEmerald
import com.example.ui.theme.DoomsdayGlassBg
import com.example.ui.theme.DoomsdayGlassBorder
import com.example.ui.theme.HdrGold
import com.example.ui.theme.TitaniumMuted
import com.example.ui.theme.TitaniumSilver
import com.example.ui.theme.TitaniumWhite

@Composable
fun DiagnosticHudOverlay(
    telemetry: DiagnosticTelemetry,
    settings: PlayerSettings,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it }),
        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it }),
        modifier = modifier
    ) {
        val hudShape = RoundedCornerShape(12.dp)
        Box(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .clip(hudShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xEE090D15),
                            Color(0xDD0D1322)
                        )
                    )
                )
                .border(1.dp, DoomsdayGlassBorder.copy(alpha = 0.5f), hudShape)
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = DoomsdayEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "DOOMSDAY HUD • OSD TELEMETRY",
                            color = DoomsdayEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "${String.format("%.1f", telemetry.fps)} FPS",
                        color = if (telemetry.fps >= 58) DoomsdayEmerald else DoomsdayAmber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Grid stats
                HudStatRow(label = "GPU ENGINE", value = "${settings.renderEngine.tag} • ${settings.gpuApi.name}")
                HudStatRow(label = "HARDWARE", value = "Qualcomm SD888 (Adreno 660)")
                HudStatRow(label = "RESOLUTION", value = telemetry.resolution)
                HudStatRow(label = "VIDEO CODEC", value = telemetry.codec)
                HudStatRow(
                    label = "COLOR / HDR",
                    value = telemetry.colorSpace,
                    valueColor = if (settings.hdrOutputEnabled) HdrGold else TitaniumSilver
                )
                HudStatRow(label = "BITRATE", value = "${String.format("%.2f", telemetry.currentBitrateMbps)} Mbps")
                HudStatRow(
                    label = "APP RAM USAGE",
                    value = "${String.format("%.1f", telemetry.appRamUsageMb)} MB (Heap)",
                    valueColor = DoomsdayCyan
                )
                HudStatRow(
                    label = "DEVICE MEMORY",
                    value = "${String.format("%.1f", telemetry.systemRamUsageGb)} GB / 12.0 GB (LPDDR5)",
                    valueColor = TitaniumSilver
                )
                HudStatRow(
                    label = "AUDIO DSP",
                    value = telemetry.audioFormat,
                    valueColor = DoomsdayEmerald
                )
                HudStatRow(
                    label = "DROPPED FRAMES",
                    value = "${telemetry.droppedFrames} / ${telemetry.totalFrames}",
                    valueColor = if (telemetry.droppedFrames > 0) DoomsdayCrimson else DoomsdayEmerald
                )
                HudStatRow(
                    label = "THERMAL & LOAD",
                    value = "${String.format("%.1f", telemetry.temperatureCelsius)}°C • GPU Load ${telemetry.gpuLoadPercent}%",
                    valueColor = if (telemetry.temperatureCelsius > 42f) DoomsdayCrimson else DoomsdayCyan
                )
            }
        }
    }
}

@Composable
private fun HudStatRow(
    label: String,
    value: String,
    valueColor: Color = TitaniumWhite
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TitaniumMuted,
            fontSize = 9.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}
