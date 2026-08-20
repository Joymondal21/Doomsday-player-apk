package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlayerSettings

@Composable
fun SubtitleOverlay(
    text: String,
    settings: PlayerSettings,
    modifier: Modifier = Modifier
) {
    if (text.isBlank()) return

    val bgColor = if (settings.subtitleBackgroundTransparent) {
        Color.Transparent
    } else {
        Color(0xB3000000)
    }

    val shadow = if (settings.subtitleShadowEnabled) {
        Shadow(
            color = Color.Black,
            offset = Offset(2f, 2f),
            blurRadius = 4f
        )
    } else {
        Shadow.None
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .background(bgColor, RoundedCornerShape(6.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    color = Color(settings.subtitleTextColor),
                    fontSize = settings.subtitleFontSizeSp.sp,
                    fontWeight = FontWeight.Bold,
                    shadow = shadow,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}
