package org.nongor.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nongor.app.ui.theme.ShapeMd

/**
 * The banner at the top of a feature screen.
 *
 * This used to be a photographic illustration per screen — five files, 9 MB before
 * compression, none of them themeable and none of them matching each other closely enough to
 * read as one product. It is now drawn: a two-stop gradient in the feature's own colour, a
 * pair of waves, and the screen's own icon bleeding off the right edge.
 *
 * That buys three things. It costs the APK nothing, it follows the palette automatically when
 * the theme changes, and it is short — the old art forced a 1.72 aspect that ate about 200dp
 * of every screen before any content appeared. A crisis app should not make you scroll to
 * reach the thing you opened it for.
 */
@Composable
fun HeroBanner(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    tint: Color,
    aspectRatio: Float = 2.9f,
) {
    val deep = lerp(tint, Color.Black, 0.28f)

    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .clip(ShapeMd)
            .background(Brush.linearGradient(listOf(tint, deep))),
    ) {
        // Two waves, low contrast, echoing the anchor mark without competing with the text.
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            fun wave(yFactor: Float, amplitude: Float, alpha: Float) {
                val y = h * yFactor
                val path = Path().apply {
                    moveTo(0f, y)
                    cubicTo(w * 0.25f, y - amplitude, w * 0.55f, y + amplitude, w, y - amplitude * 0.4f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(path, Color.White.copy(alpha = alpha))
            }
            wave(yFactor = 0.62f, amplitude = h * 0.16f, alpha = 0.07f)
            wave(yFactor = 0.80f, amplitude = h * 0.12f, alpha = 0.07f)
        }

        // The screen's own icon, oversized and half off the edge so it reads as texture
        // rather than as a control someone might try to press.
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.16f),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 26.dp)
                .size(132.dp),
        )

        Column(
            Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(0.72f)
                .padding(horizontal = 18.dp),
        ) {
            Text(
                title,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 21.sp,
                lineHeight = 25.sp,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                )
            }
        }
    }
}
