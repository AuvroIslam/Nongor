package org.nongor.app.ui.family

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.FeatherIcons
import compose.icons.feathericons.HelpCircle
import org.nongor.app.ui.i18n.LocalBangla
import org.nongor.app.ui.i18n.localiseDigits
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.BrandSand
import org.nongor.app.ui.theme.BrandTealDeep
import org.nongor.app.ui.theme.BrandTealLite
import org.nongor.app.ui.theme.BrandTealSoft
import org.nongor.app.ui.theme.ShapeLg
import org.nongor.app.ui.theme.ShapePill
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sin
import org.nongor.app.ui.theme.ErrorRed
import org.nongor.app.ui.theme.SafeGreen

/**
 * Where your family is, as a picture.
 *
 * A list of rows saying "Rahim, 400 m east" is accurate and almost useless when you are
 * standing in water trying to decide which way to walk. The same numbers drawn as a compass
 * answer the actual question — *which direction, and is it near or far* — in one glance, and
 * it reads the same whether or not you can read at all.
 *
 * Nothing here is invented. The angle is the true bearing the view model computed from two
 * GPS fixes and the ring is the measured distance; a member whose position we never learned
 * is deliberately **not** placed on the circle, because putting them somewhere plausible
 * would be a guess that sends someone walking the wrong way. Those appear underneath instead.
 */

/** Inset between the panel edge and the outer ring. */
private val RADAR_PADDING = 14.dp

/** Distance bands the rings represent. */
private const val RING_NEAR_M = 100.0
private const val RING_MID_M = 500.0
private const val RING_FAR_M = 2000.0

/**
 * Map a distance to a fraction of the radius.
 *
 * Logarithmic, because the useful range spans 20 m to several kilometres: on a linear scale
 * everyone inside a village would pile onto the centre dot.
 */
private fun radiusFraction(distanceM: Int): Float {
    val f = ln(1 + distanceM / 25.0) / ln(1 + RING_FAR_M / 25.0)
    return f.coerceIn(0.14, 0.95).toFloat()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FamilyRadar(
    members: List<SeenMember>,
    listening: Boolean,
    modifier: Modifier = Modifier,
    sosBlips: List<RadarBlip> = emptyList(),
    volunteerBlips: List<RadarBlip> = emptyList(),
    onSelect: (SeenMember) -> Unit = {},
) {
    val bangla = LocalBangla.current
    val placed = members.filter { it.hasFix }
    val unplaced = members.filter { !it.hasFix }

    val sweep by rememberInfiniteTransition(label = "sweep").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sweepAngle",
    )

    Column(modifier) {
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(ShapeLg)
                .background(
                    Brush.radialGradient(
                        listOf(BrandTealDeep, Color(0xFF03302A)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            // The usable radius, measured rather than assumed, so blips land correctly on
            // any screen size and in split-screen.
            val radius = (maxWidth - RADAR_PADDING * 2) / 2
            Canvas(Modifier.fillMaxSize().padding(RADAR_PADDING)) {
                val c = Offset(size.width / 2f, size.height / 2f)
                val maxR = size.minDimension / 2f

                // Distance rings.
                listOf(RING_NEAR_M, RING_MID_M, RING_FAR_M).forEach { m ->
                    drawCircle(
                        color = BrandTealSoft.copy(alpha = 0.22f),
                        radius = radiusFraction(m.toInt()) * maxR,
                        center = c,
                        style = Stroke(width = 1.5f),
                    )
                }

                // Cross hairs, so north is unambiguous.
                drawLine(
                    BrandTealSoft.copy(alpha = 0.14f),
                    Offset(c.x, c.y - maxR), Offset(c.x, c.y + maxR), strokeWidth = 1.5f,
                )
                drawLine(
                    BrandTealSoft.copy(alpha = 0.14f),
                    Offset(c.x - maxR, c.y), Offset(c.x + maxR, c.y), strokeWidth = 1.5f,
                )

                // The sweep only turns while the radio is actually listening — a moving
                // screen must never imply the app is doing something it is not.
                if (listening) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            0f to Color.Transparent,
                            0.10f to BrandTealLite.copy(alpha = 0.30f),
                            0.16f to Color.Transparent,
                            1f to Color.Transparent,
                            center = c,
                        ),
                        startAngle = sweep,
                        sweepAngle = 60f,
                        useCenter = true,
                        topLeft = Offset(c.x - maxR, c.y - maxR),
                        size = Size(maxR * 2, maxR * 2),
                    )
                }
            }

            // Compass letters.
            CompassLabel("N", Alignment.TopCenter)
            CompassLabel("S", Alignment.BottomCenter)
            CompassLabel("E", Alignment.CenterEnd)
            CompassLabel("W", Alignment.CenterStart)

            // You, at the centre.
            Box(
                Modifier.size(58.dp).clip(CircleShape).background(BrandTealLite.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(BrandSand)
                        .border(2.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        tr("YOU", "আপনি"),
                        color = BrandTealDeep,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }

            // Everyone we have a real fix for. Drawn worst-first so an SOS is never hidden
            // under a volunteer marker sitting at the same bearing.
            volunteerBlips.filter { it.hasFix }.forEach { OtherBlip(it, radius, SafeGreen) }
            placed.forEach { seen -> MemberBlip(seen, radius, bangla, onSelect) }
            sosBlips.filter { it.hasFix }.forEach { OtherBlip(it, radius, ErrorRed) }

            if (members.isEmpty()) {
                // Sits just under the centre dot rather than at the bottom edge, where it
                // would collide with the "S" compass label.
                Text(
                    tr(
                        "No family phone heard yet",
                        "এখনো পরিবারের কোনো ফোন পাওয়া যায়নি",
                    ),
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(y = 54.dp).padding(horizontal = 40.dp),
                )
            }
        }

        // Ring legend — the picture is only trustworthy if the scale is stated.
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            RingKey(localiseDigits(tr("100 m", "১০০ মি"), bangla))
            RingKey(localiseDigits(tr("500 m", "৫০০ মি"), bangla))
            RingKey(localiseDigits(tr("2 km", "২ কিমি"), bangla))
        }

        // Heard, but no position — never placed on the circle.
        if (unplaced.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                tr(
                    "Heard nearby, direction unknown",
                    "কাছেই পাওয়া গেছে, দিক জানা যায়নি",
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                unplaced.forEach { seen ->
                    Row(
                        Modifier
                            .clip(ShapePill)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onSelect(seen) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            FeatherIcons.HelpCircle,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            seen.member.name,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

/**
 * One person on the circle.
 *
 * Placed with a fractional offset so it lands correctly at any screen size, and faded by how
 * long ago they were heard — a contact from an hour ago should not look as solid as one from
 * a minute ago.
 */
@Composable
private fun BoxScope.MemberBlip(
    seen: SeenMember,
    radius: Dp,
    bangla: Boolean,
    onSelect: (SeenMember) -> Unit,
) {
    val distance = seen.distanceM ?: return
    val bearing = seen.bearingDeg ?: return

    val r = radiusFraction(distance)
    val rad = Math.toRadians(bearing.toDouble())
    // Screen y grows downwards, so north (0 degrees) is negative y.
    val fx = (sin(rad) * r).toFloat()
    val fy = (-cos(rad) * r).toFloat()

    // Older contacts fade. A fix from an hour ago should not look as solid as one from a
    // minute ago, because it is not as good a reason to start walking that way.
    val freshness = when {
        seen.minutesAgo < 5 -> 1f
        seen.minutesAgo < 30 -> 0.8f
        seen.minutesAgo < 120 -> 0.6f
        else -> 0.45f
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(x = radius * fx, y = radius * fy)
                .clickable { onSelect(seen) },
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = freshness))
                    .border(2.dp, BrandSand.copy(alpha = freshness), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    seen.member.name.trim().take(1).uppercase(),
                    color = BrandTealDeep,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                )
            }
            Spacer(Modifier.height(3.dp))
            Text(
                seen.member.name.take(10),
                color = Color.White.copy(alpha = freshness),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                localiseDigits(
                    if (distance >= 1000) "%.1fkm".format(distance / 1000.0) else "${distance}m",
                    bangla,
                ),
                color = BrandSand.copy(alpha = freshness),
                fontSize = 9.sp,
            )
        }
    }
}

@Composable
private fun BoxScope.CompassLabel(text: String, at: Alignment) {
    Text(
        text,
        color = BrandTealSoft.copy(alpha = 0.55f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.align(at).padding(6.dp),
    )
}

@Composable
private fun RingKey(label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
        )
        Spacer(Modifier.width(5.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * A blip that is not family: someone calling for help, or someone offering it.
 *
 * Smaller and unlabelled by name, because the useful fact here is the *direction* and the
 * colour - red means somebody there needs help, green means help is standing there.
 */
@Composable
private fun BoxScope.OtherBlip(blip: RadarBlip, radius: Dp, colour: Color) {
    val distance = blip.distanceM ?: return
    val bearing = blip.bearingDeg ?: return
    val r = radiusFraction(distance)
    val rad = Math.toRadians(bearing.toDouble())
    val fx = (sin(rad) * r).toFloat()
    val fy = (-cos(rad) * r).toFloat()

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(x = radius * fx, y = radius * fy),
        ) {
            Box(
                Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(colour)
                    .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                blip.name.take(8),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
