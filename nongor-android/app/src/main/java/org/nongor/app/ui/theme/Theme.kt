package org.nongor.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Nongor's palette is built for a phone held at arm's length in bad light with wet hands:
 * deep water blue for structure, amber for "act now", one unmistakable red for danger.
 */
object NongorColors {
    val Deep = Color(0xFF0C3B5E)
    val DeepDark = Color(0xFF072941)
    val Surf = Color(0xFF1B6E9C)
    val Amber = Color(0xFFF5A524)
    val AmberDark = Color(0xFFB9770F)
    val Danger = Color(0xFFD62828)
    val DangerDark = Color(0xFF8E1616)
    val Safe = Color(0xFF1B9C6B)
    val Caution = Color(0xFFE07B00)
    val Sand = Color(0xFFF6F3EC)
    val Ink = Color(0xFF101820)
    val Muted = Color(0xFF5A6672)
}

private val LightScheme = lightColorScheme(
    primary = NongorColors.Deep,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E9F5),
    onPrimaryContainer = NongorColors.DeepDark,
    secondary = NongorColors.Amber,
    onSecondary = NongorColors.Ink,
    secondaryContainer = Color(0xFFFFEBC7),
    onSecondaryContainer = Color(0xFF4A3200),
    error = NongorColors.Danger,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF5F1412),
    background = NongorColors.Sand,
    onBackground = NongorColors.Ink,
    surface = Color.White,
    onSurface = NongorColors.Ink,
    surfaceVariant = Color(0xFFE7E3DA),
    onSurfaceVariant = NongorColors.Muted,
    outline = Color(0xFFB9C0C8),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF8FCBEC),
    onPrimary = NongorColors.DeepDark,
    primaryContainer = Color(0xFF10456B),
    onPrimaryContainer = Color(0xFFD7E9F5),
    secondary = NongorColors.Amber,
    onSecondary = Color(0xFF3A2700),
    secondaryContainer = Color(0xFF5C3F00),
    onSecondaryContainer = Color(0xFFFFEBC7),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0B0F14),
    onBackground = Color(0xFFE6EAF0),
    surface = Color(0xFF121821),
    onSurface = Color(0xFFE6EAF0),
    surfaceVariant = Color(0xFF1D2632),
    onSurfaceVariant = Color(0xFF9FAAB8),
    outline = Color(0xFF3A4653),
)

private val NongorTypography = Typography(
    displaySmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 38.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 25.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 23.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 19.sp),
)

@Composable
fun NongorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = NongorTypography,
        content = content,
    )
}
