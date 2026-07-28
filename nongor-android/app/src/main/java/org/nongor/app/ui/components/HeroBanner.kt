package org.nongor.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Rounded hero illustration banner. Uses [aspectRatio] (width / height) so the ~3:2 art is only
 * lightly cropped. The subject sits on the right; an optional [title]/[subtitle] sits on the left
 * over a subtle left-to-right scrim so the text stays readable and never clashes with a character.
 */
@Composable
fun HeroBanner(
    @DrawableRes resId: Int,
    aspectRatio: Float = 1.72f,
    title: String? = null,
    subtitle: String? = null,
) {
    Box(
        Modifier.fillMaxWidth().aspectRatio(aspectRatio).clip(RoundedCornerShape(20.dp)),
    ) {
        Image(
            painter = painterResource(resId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (title != null) {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.horizontalGradient(
                        0.0f to Color(0xFF2A1A66).copy(alpha = 0.55f),
                        0.42f to Color(0xFF2A1A66).copy(alpha = 0.18f),
                        0.62f to Color.Transparent,
                    ),
                ),
            )
            Column(
                Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.46f)
                    .padding(start = 18.dp, end = 4.dp),
            ) {
                Text(title, color = Color.White, fontWeight = FontWeight.ExtraBold,
                    fontSize = 21.sp, lineHeight = 25.sp)
                if (subtitle != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(subtitle, color = Color.White.copy(alpha = 0.95f),
                        fontSize = 13.sp, lineHeight = 17.sp)
                }
            }
        }
    }
}
