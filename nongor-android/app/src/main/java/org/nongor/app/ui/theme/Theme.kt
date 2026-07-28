package org.nongor.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = AccentPurple,
    onPrimary = Color.White,
    primaryContainer = AccentPurple.copy(alpha = 0.12f),
    onPrimaryContainer = AccentPurple,
    secondary = AccentViolet,
    onSecondary = Color.White,
    background = BgDark,
    onBackground = TextPrimary,
    surface = BgCard,
    onSurface = TextPrimary,
    surfaceVariant = BgMid,
    onSurfaceVariant = TextSecondary,
    outline = Divider,
    error = ErrorRed,
    onError = Color.White,
)

@Composable
fun NongorTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BgDark.toArgb()
            window.navigationBarColor = BgDark.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = true
            controller.isAppearanceLightNavigationBars = true
        }
    }
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content,
    )
}
