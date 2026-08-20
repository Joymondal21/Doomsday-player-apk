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

    val isTransparent = settings.subtitleBackgroundTransparent
    val bgColor = if (isTransparent) {
        Color.Transparent
    } else {
        Color(0xCC000000)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .background(bgColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            val textColor = Color(settings.subtitleTextColor)
            val fontSize = settings.subtitleFontSizeSp.sp

            if (isTransparent) {
                // High-contrast multi-offset outline shadows for transparent background
                Box(contentAlignment = Alignment.Center) {
                    val outlineOffsets = listOf(
                        Offset(-2.5f, -2.5f),
                        Offset(2.5f, -2.5f),
                        Offset(-2.5f, 2.5f),
                        Offset(2.5f, 2.5f),
                        Offset(0f, 3f),
                        Offset(0f, -3f),
                        Offset(3f, 0f),
                        Offset(-3f, 0f)
                    )

                    outlineOffsets.forEach { offset ->
                        Text(
                            text = text,
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                color = Color.Black,
                                fontSize = fontSize,
                                fontWeight = FontWeight.Bold,
                                shadow = Shadow(color = Color.Black, offset = offset, blurRadius = 2f),
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    Text(
                        text = text,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            color = textColor,
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            } else {
                Text(
                    text = text,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        color = textColor,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        shadow = Shadow(color = Color.Black, offset = Offset(2f, 2f), blurRadius = 4f),
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}
