package org.nongor.app.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.FeatherIcons
import compose.icons.feathericons.AlertTriangle
import compose.icons.feathericons.BarChart2
import compose.icons.feathericons.ChevronRight
import compose.icons.feathericons.HelpCircle
import compose.icons.feathericons.PhoneCall
import compose.icons.feathericons.Settings
import org.nongor.app.R
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.BgCard
import org.nongor.app.ui.theme.BrandTeal
import org.nongor.app.ui.theme.BrandTealDeep
import org.nongor.app.ui.theme.CautionAmber
import org.nongor.app.ui.theme.ErrorRed
import org.nongor.app.ui.theme.SafeGreen
import org.nongor.app.ui.theme.ShapeMd
import org.nongor.app.ui.theme.ShapePill
import org.nongor.app.ui.theme.TextPrimary
import org.nongor.app.ui.theme.TextSecondary

/**
 * The volunteer's side of the app.
 *
 * Everything here is for the person who is *not* the one in trouble: the neighbour with a
 * boat, the student who walked in from the next union, the medic on a motorbike. Their
 * problem is the opposite of the survivor's — they need to find people, understand them, and
 * be findable themselves.
 */
@Composable
fun VolunteerTab(
    sharing: Boolean,
    sharingBusy: Boolean,
    onShareLocation: () -> Unit,
    onStopSharing: () -> Unit,
    onTranslate: () -> Unit,
    onRadar: () -> Unit,
    onTriage: () -> Unit,
    onSummary: () -> Unit,
    onEmergency: () -> Unit,
    onGuide: () -> Unit,
    onSettings: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 16.dp, bottom = 24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(BrandTeal.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(R.drawable.ic_volunteer),
                    contentDescription = null,
                    tint = BrandTeal,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    tr("Volunteer", "স্বয়ংসেবক"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                )
                Text(
                    tr("Tools for helping other people", "অন্যদের সাহায্য করার সরঞ্জাম"),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        // ---- Be findable ----
        Spacer(Modifier.height(18.dp))
        ShareCard(
            sharing = sharing,
            busy = sharingBusy,
            onShare = onShareLocation,
            onStop = onStopSharing,
        )

        // ---- Do the work ----
        Spacer(Modifier.height(18.dp))
        VolunteerRow(
            painter = R.drawable.ic_translate, tint = BrandTeal,
            title = tr("Talk to anyone", "যে কারও সাথে কথা বলুন"),
            subtitle = tr(
                "Chakma · Marma · Rohingya · Kokborok · Santali · Garo · sign",
                "চাকমা · মারমা · রোহিঙ্গা · ককবরক · সাঁওতালি · গারো · ইশারা",
            ),
            onClick = onTranslate,
        )
        VolunteerRow(
            painter = R.drawable.ic_radar, tint = Color(0xFF3C5A78),
            title = tr("Radar", "রাডার"),
            subtitle = tr(
                "Family, SOS calls and other volunteers around you",
                "আপনার আশেপাশে পরিবার, এসওএস ও অন্য স্বয়ংসেবক",
            ),
            onClick = onRadar,
        )
        VolunteerRow(
            icon = FeatherIcons.AlertTriangle, tint = CautionAmber,
            title = tr("Who needs help first", "আগে কার সাহায্য দরকার"),
            subtitle = tr("Rank every case logged on this phone", "এই ফোনের সব কেস অগ্রাধিকার অনুযায়ী"),
            onClick = onTriage,
        )
        VolunteerRow(
            icon = FeatherIcons.BarChart2, tint = BrandTealDeep,
            title = tr("Situation briefing", "পরিস্থিতির সারসংক্ষেপ"),
            subtitle = tr("Counts, worst cases, shelter pressure", "সংখ্যা, খারাপ কেস, আশ্রয়ের চাপ"),
            onClick = onSummary,
        )

        // ---- Reference ----
        Spacer(Modifier.height(18.dp))
        VolunteerRow(
            icon = FeatherIcons.PhoneCall, tint = ErrorRed,
            title = tr("All emergency numbers", "সব জরুরি নম্বর"),
            subtitle = tr("999, flood warning, Coast Guard and more", "৯৯৯, বন্যা সতর্কতা, কোস্ট গার্ড ও আরও"),
            onClick = onEmergency,
        )
        VolunteerRow(
            icon = FeatherIcons.HelpCircle, tint = BrandTeal,
            title = tr("How to use Nongor", "নোঙর যেভাবে ব্যবহার করবেন"),
            subtitle = tr("What each tool does, and a flood drill", "প্রতিটি টুল কী করে, ও একটি মহড়া"),
            onClick = onGuide,
        )
        VolunteerRow(
            icon = FeatherIcons.Settings, tint = TextSecondary,
            title = tr("Settings", "সেটিংস"),
            subtitle = tr("Language, your name, the optional AI model", "ভাষা, আপনার নাম, ঐচ্ছিক এআই মডেল"),
            onClick = onSettings,
        )
    }
}

/**
 * Announce that you are here and able to help.
 *
 * This posts your position to the same signed mesh the rest of the app uses, tagged as
 * "rescue available", so it turns up on a stranger's Radar and on the neighbourhood board.
 * It is off until you switch it on, and switching it off is one tap — a volunteer walking
 * home should not still be advertising themselves as available.
 */
@Composable
private fun ShareCard(sharing: Boolean, busy: Boolean, onShare: () -> Unit, onStop: () -> Unit) {
    val tint = if (sharing) SafeGreen else BrandTeal
    Column(
        Modifier
            .fillMaxWidth()
            .clip(ShapeMd)
            .background(tint.copy(alpha = 0.10f))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(tint),
                contentAlignment = Alignment.Center,
            ) {
                if (busy) {
                    CircularProgressIndicator(
                        Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White,
                    )
                } else {
                    Icon(
                        painterResource(R.drawable.ic_volunteer),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (sharing) {
                        tr("You are visible as a volunteer", "আপনি স্বয়ংসেবক হিসেবে দেখা যাচ্ছেন")
                    } else {
                        tr("Share where you are", "আপনি কোথায় আছেন জানান")
                    },
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                )
                Text(
                    if (sharing) {
                        tr(
                            "Nearby phones can see that help is at your location.",
                            "কাছের ফোনগুলো দেখতে পাচ্ছে আপনার অবস্থানে সাহায্য আছে।",
                        )
                    } else {
                        tr(
                            "Puts you on the Radar of anyone nearby who needs help. Nothing leaves the mesh.",
                            "কাছের যার সাহায্য দরকার তার রাডারে আপনি দেখা যাবেন। কিছুই মেশের বাইরে যায় না।",
                        )
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(ShapePill)
                .background(if (sharing) BgCard else tint)
                .clickable(enabled = !busy) { if (sharing) onStop() else onShare() }
                .padding(vertical = 11.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        ) {
            Text(
                if (sharing) {
                    tr("Stop sharing", "শেয়ার বন্ধ করুন")
                } else {
                    tr("I am here and can help", "আমি এখানে আছি, সাহায্য করতে পারি")
                },
                color = if (sharing) tint else Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun VolunteerRow(
    icon: ImageVector? = null,
    painter: Int? = null,
    tint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(ShapeMd)
            .background(BgCard)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(38.dp).clip(CircleShape).background(tint.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center,
        ) {
            when {
                painter != null -> Icon(
                    painterResource(painter),
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp),
                )
                icon != null -> Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
            }
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
