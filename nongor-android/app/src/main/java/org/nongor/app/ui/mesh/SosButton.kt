package org.nongor.app.ui.mesh

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Volume2
import compose.icons.feathericons.VolumeX
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.ErrorRed
import org.nongor.app.ui.theme.ShapePill
import org.nongor.app.ui.theme.SafeGreen
import org.nongor.app.ui.theme.TextSecondary

/**
 * The one control someone can find without reading.
 *
 * Everything else in Nongor asks you to choose something. This does not: it is the biggest
 * thing on the screen, it is the only red circle in the app, and one press does the three
 * things that actually matter — broadcast a signed SOS to every phone in range, start the
 * siren so rescuers can hear where you are, and keep repeating both until you stop it.
 *
 * Sized for a thumb pressed by someone cold, wet and frightened, and for a person who cannot
 * read Bangla or English at all.
 */
@Composable
fun SosButton(
    active: Boolean,
    sirenOn: Boolean,
    peers: Int,
    onPress: () -> Unit,
    onStop: () -> Unit,
    onToggleSiren: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pulse by rememberInfiniteTransition(label = "sos").animateFloat(
        initialValue = 1f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "sosPulse",
    )

    Column(
        modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            if (active) {
                tr("Sending, and repeating", "পাঠানো হচ্ছে, বারবার")
            } else {
                tr("One press: call for help", "এক চাপে: সাহায্য চান")
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            if (active) {
                tr(
                    "Every Nongor phone in range is being told, and passing it on.",
                    "কাছের প্রতিটি নোঙর ফোন জানছে এবং এগিয়ে দিচ্ছে।",
                )
            } else {
                tr(
                    "Sends a signed SOS to nearby phones and sounds an alarm.",
                    "কাছের ফোনে স্বাক্ষরিত SOS পাঠায় এবং জোরে শব্দ করে।",
                )
            },
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
        )

        Spacer(Modifier.height(14.dp))

        Box(contentAlignment = Alignment.Center) {
            // Halo rings, only while actually transmitting — a screen that looks busy when
            // nothing is being sent is a lie told to someone who is relying on it.
            if (active) {
                Box(
                    Modifier
                        .size(240.dp)
                        .scale(pulse)
                        .clip(CircleShape)
                        .background(ErrorRed.copy(alpha = 0.10f)),
                )
                Box(
                    Modifier
                        .size(200.dp)
                        .scale(pulse)
                        .clip(CircleShape)
                        .background(ErrorRed.copy(alpha = 0.13f)),
                )
            }

            Box(
                Modifier
                    .size(170.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFE03A3A), ErrorRed),
                        ),
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = Color.White),
                        onClick = { if (active) onStop() else onPress() },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (active) tr("STOP", "থামান") else "SOS",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (active) 34.sp else 44.sp,
                        letterSpacing = 2.sp,
                    )
                    Text(
                        if (active) {
                            tr("tap to stop", "থামাতে চাপুন")
                        } else {
                            tr("press for help", "সাহায্যের জন্য চাপুন")
                        },
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Status. Honest about the fact that nobody may be listening yet.
        Text(
            when {
                !active -> tr("Nothing is being sent yet.", "এখনো কিছু পাঠানো হচ্ছে না।")
                peers > 0 -> tr(
                    "$peers phone(s) in range have it.",
                    "কাছের $peers টি ফোন এটি পেয়েছে।",
                )
                else -> tr(
                    "No phone in range yet — held and sent the moment one appears.",
                    "কাছে এখনো কোনো ফোন নেই — একটি এলেই সাথে সাথে যাবে।",
                )
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (active && peers > 0) SafeGreen else TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        // The siren is separately switchable, because there are moments in a rescue when the
        // noise has to stop — a responder shouting a question, a team listening for someone
        // else — without giving up the broadcast.
        if (active) {
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier
                    .clip(ShapePill)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onToggleSiren)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    if (sirenOn) FeatherIcons.Volume2 else FeatherIcons.VolumeX,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (sirenOn) ErrorRed else TextSecondary,
                )
                Text(
                    if (sirenOn) {
                        tr("Alarm on — tap to silence", "শব্দ চালু — বন্ধ করতে চাপুন")
                    } else {
                        tr("Alarm off — tap to sound", "শব্দ বন্ধ — বাজাতে চাপুন")
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
