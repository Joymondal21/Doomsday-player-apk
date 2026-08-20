package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DoomsdayColorScheme = darkColorScheme(
    primary = DoomsdayEmerald,
    onPrimary = DoomsdayObsidian,
    primaryContainer = DoomsdayEmeraldDark,
    onPrimaryContainer = TitaniumWhite,
    secondary = DoomsdayCyan,
    onSecondary = DoomsdayObsidian,
    secondaryContainer = DoomsdaySurfaceVariant,
    onSecondaryContainer = DoomsdayCyan,
    tertiary = DoomsdayCrimson,
    onTertiary = TitaniumWhite,
    background = DoomsdayObsidian,
    onBackground = TitaniumWhite,
    surface = DoomsdaySurface,
    onSurface = TitaniumWhite,
    surfaceVariant = DoomsdaySurfaceVariant,
    onSurfaceVariant = TitaniumSilver,
    outline = DoomsdayGlassBorder,
    error = DoomsdayCrimson,
    onError = TitaniumWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Cinema player defaults to immersive dark
    content: @Composable () -> Unit
) {
    val colorScheme = DoomsdayColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DoomsdayObsidian.toArgb()
                window.navigationBarColor = DoomsdayObsidian.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
