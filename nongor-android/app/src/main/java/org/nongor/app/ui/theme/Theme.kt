package org.nongor.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = BrandTeal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEDE8),
    onPrimaryContainer = BrandTealDeep,
    secondary = BrandTealLite,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE1F0EC),
    onSecondaryContainer = BrandTealDeep,
    tertiary = BrandSand,
    onTertiary = Color.White,
    background = BgDark,
    onBackground = TextPrimary,
    surface = BgCard,
    onSurface = TextPrimary,
    surfaceVariant = BgMid,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    outlineVariant = GlassBorder,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFBEAE7),
    onErrorContainer = Color(0xFF7A1A1A),
    scrim = Color(0x99101410),
)

/**
 * Material's shape slots wired to our three radii, so any component we do not style by hand
 * — dialogs, menus, snackbars, date pickers — still lands on the same system.
 */
private val NongorShapes = Shapes(
    extraSmall = ShapeSm,
    small = ShapeSm,
    medium = ShapeMd,
    large = ShapeLg,
    extraLarge = ShapeLg,
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
        shapes = NongorShapes,
        content = content,
    )
}
