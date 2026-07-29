package org.nongor.app.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun NongorGradientBackground(
    modifier: Modifier = Modifier,
    showDotPattern: Boolean = false,
    content: @Composable () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "mesh_bg")
    val driftA = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "driftA",
    ).value
    val driftB = transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "driftB",
    ).value
    val driftC = transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(26000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "driftC",
    ).value

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    // The wash at the top of the chat. It was a strong cornflower blue, which made the
                    // one screen with the most brand presence the one screen that did not look
                    // like Nongor.
                    colors = listOf(Color(0xFF11806A), BgMid, BgDark),
                ),
            ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            // Mesh blobs
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(BrandBlue.copy(alpha = 0.20f), Color.Transparent),
                    center = Offset(size.width * (0.18f + 0.08f * driftA), size.height * (0.16f + 0.07f * driftB)),
                    radius = size.width * 0.52f,
                ),
                center = Offset(size.width * (0.18f + 0.08f * driftA), size.height * (0.16f + 0.07f * driftB)),
                radius = size.width * 0.52f,
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(BrandTeal.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(size.width * (0.80f - 0.10f * driftB), size.height * (0.18f + 0.06f * driftC)),
                    radius = size.width * 0.46f,
                ),
                center = Offset(size.width * (0.80f - 0.10f * driftB), size.height * (0.18f + 0.06f * driftC)),
                radius = size.width * 0.46f,
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF0B6E5F).copy(alpha = 0.14f), Color.Transparent),
                    center = Offset(size.width * (0.30f + 0.10f * driftC), size.height * (0.76f - 0.08f * driftA)),
                    radius = size.width * 0.50f,
                ),
                center = Offset(size.width * (0.30f + 0.10f * driftC), size.height * (0.76f - 0.08f * driftA)),
                radius = size.width * 0.50f,
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(BrandBlueGlow.copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(size.width * (0.76f - 0.06f * driftA), size.height * (0.72f - 0.08f * driftC)),
                    radius = size.width * 0.42f,
                ),
                center = Offset(size.width * (0.76f - 0.06f * driftA), size.height * (0.72f - 0.08f * driftC)),
                radius = size.width * 0.42f,
            )

            // Floating particle field
            repeat(90) { i ->
                val seed = (i * 73).toFloat()
                val x = ((seed * 17.3f) % size.width)
                val y = ((seed * 31.7f) % size.height)
                val alpha = 0.035f + ((i % 6) / 120f)
                val radius = 0.8f + (i % 3) * 0.8f
                drawCircle(
                    color = BrandTeal.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(
                        x + (driftA - 0.5f) * ((i % 5) * 1.5f),
                        y + (driftB - 0.5f) * ((i % 7) * 1.2f),
                    ),
                )
            }

            // Noise / grain overlay
            repeat(500) { i ->
                val n = ((i * 9301 + 49297) % 233280).toFloat() / 233280f
                val x = ((i * 37f) % size.width)
                val y = ((i * 91f) % size.height)
                drawCircle(
                    color = Color.White.copy(alpha = 0.008f + (n * 0.014f)),
                    radius = 0.45f + (n * 0.6f),
                    center = Offset(x, y),
                )
            }
        }
        content()
    }
}
