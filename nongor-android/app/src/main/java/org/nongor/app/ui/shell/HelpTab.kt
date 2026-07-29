package org.nongor.app.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Activity
import compose.icons.feathericons.ChevronRight
import compose.icons.feathericons.PhoneCall
import compose.icons.feathericons.Radio
import org.nongor.app.ui.emergency.dialNumber
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.BgCard
import org.nongor.app.ui.theme.CautionAmber
import org.nongor.app.ui.theme.ErrorRed
import org.nongor.app.ui.theme.SafeGreen
import org.nongor.app.ui.theme.ShapeMd
import org.nongor.app.ui.theme.ShapePill
import org.nongor.app.ui.theme.TextPrimary
import org.nongor.app.ui.theme.TextSecondary

/**
 * Getting help, and giving it.
 *
 * Ordered by how fast the thing works and how little it needs: a phone call needs a tower and
 * works in seconds; the mesh needs neither but needs somebody nearby; first aid needs only
 * you. That is also the order a person tries them in when things are going wrong, so it is
 * the order they appear.
 */
@Composable
fun HelpTab(
    sosActive: Boolean,
    sirenOn: Boolean,
    onStopSos: () -> Unit,
    onMesh: () -> Unit,
    onEmergency: () -> Unit,
    onFirstAid: () -> Unit,
    onTriage: () -> Unit,
) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 16.dp, bottom = 24.dp),
    ) {
        Text(
            tr("Get help", "সাহায্য নিন"),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
        )
        Text(
            tr(
                "Fastest first. Each one needs less than the one above it.",
                "সবচেয়ে দ্রুততম আগে। প্রতিটির জন্য উপরেরটির চেয়ে কম কিছু লাগে।",
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )

        if (sosActive) {
            Spacer(Modifier.height(14.dp))
            LiveSosStrip(sirenOn = sirenOn, onStop = onStopSos, onOpen = onMesh)
        }

        // 1 — needs a tower, works in seconds.
        Spacer(Modifier.height(16.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .clip(ShapeMd)
                .background(ErrorRed)
                .clickable(onClick = onEmergency)
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(FeatherIcons.PhoneCall, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        tr("Call for help", "সাহায্যের জন্য কল"),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        tr("999 and every official hotline", "৯৯৯ ও সব সরকারি হটলাইন"),
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Box(
                    Modifier
                        .clip(ShapePill)
                        .background(Color.White)
                        .clickable { dialNumber(context, "999") }
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                ) {
                    Text(
                        tr("999", "৯৯৯"),
                        color = ErrorRed,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                    )
                }
            }
        }

        // 2 — needs no tower, but needs somebody nearby.
        Spacer(Modifier.height(10.dp))
        HelpRow(
            icon = FeatherIcons.Radio,
            tint = ErrorRed,
            title = tr("Send an SOS with no network", "নেটওয়ার্ক ছাড়াই এসওএস পাঠান"),
            subtitle = tr(
                "Phone to phone, plus a siren so you can be heard",
                "ফোন থেকে ফোনে, সাথে জোরে শব্দ যাতে শোনা যায়",
            ),
            onClick = onMesh,
        )

        // 3 — needs nothing but you.
        Spacer(Modifier.height(10.dp))
        HelpRow(
            icon = FeatherIcons.Activity,
            tint = SafeGreen,
            title = tr("Treat an injury", "আঘাতের চিকিৎসা"),
            subtitle = tr(
                "Cited first aid, offline, in Bangla or English",
                "উৎসসহ প্রাথমিক চিকিৎসা, অফলাইনে, বাংলা বা ইংরেজিতে",
            ),
            onClick = onFirstAid,
        )

        Spacer(Modifier.height(10.dp))
        HelpRow(
            icon = FeatherIcons.Activity,
            tint = CautionAmber,
            title = tr("Who needs help first", "আগে কার সাহায্য দরকার"),
            subtitle = tr(
                "Rank the cases you and your neighbours have logged",
                "আপনি ও প্রতিবেশীদের জমা করা কেস অগ্রাধিকার অনুযায়ী সাজান",
            ),
            onClick = onTriage,
        )
    }
}

@Composable
private fun HelpRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(ShapeMd)
            .background(BgCard)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(tint.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
            )
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Icon(FeatherIcons.ChevronRight, null, tint = TextSecondary)
    }
}

/** Shown while an SOS is broadcasting, so stopping it is never more than one tap away. */
@Composable
private fun LiveSosStrip(sirenOn: Boolean, onStop: () -> Unit, onOpen: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(ShapeMd)
            .background(ErrorRed.copy(alpha = 0.12f))
            .clickable(onClick = onOpen)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(34.dp).clip(CircleShape).background(ErrorRed),
            contentAlignment = Alignment.Center,
        ) {
            Icon(FeatherIcons.Radio, null, tint = Color.White, modifier = Modifier.size(17.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                tr("SOS is being sent", "এসওএস পাঠানো হচ্ছে"),
                color = ErrorRed,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                if (sirenOn) {
                    tr("Repeating, alarm sounding", "বারবার যাচ্ছে, শব্দ বাজছে")
                } else {
                    tr("Repeating, alarm silenced", "বারবার যাচ্ছে, শব্দ বন্ধ")
                },
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Box(
            Modifier
                .clip(ShapePill)
                .background(ErrorRed)
                .clickable(onClick = onStop)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                tr("Stop", "থামান"),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
