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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.AlertTriangle
import compose.icons.feathericons.BarChart2
import compose.icons.feathericons.ChevronRight
import compose.icons.feathericons.HelpCircle
import compose.icons.feathericons.MessageSquare
import compose.icons.feathericons.PhoneCall
import compose.icons.feathericons.Settings
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.BgCard
import org.nongor.app.ui.theme.BrandTeal
import org.nongor.app.ui.theme.CautionAmber
import org.nongor.app.ui.theme.ErrorRed
import org.nongor.app.ui.theme.ShapeMd
import org.nongor.app.ui.theme.TextPrimary
import org.nongor.app.ui.theme.TextSecondary

/**
 * Everything else, in one list.
 *
 * The coordinator tools live here rather than on Home because they are used by a different
 * person on a different day: a volunteer sitting down to work out who to reach first, not
 * someone standing in water. Nothing is hidden — every feature in the app is reachable from
 * Home or from this list, and neither is more than two taps deep.
 */
@Composable
fun MoreTab(
    onTranslate: () -> Unit,
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
        Text(
            tr("More", "আরও"),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
        )
        Spacer(Modifier.height(16.dp))

        Section(tr("For responders", "উদ্ধারকারীদের জন্য"))
        MoreRow(
            FeatherIcons.MessageSquare, BrandTeal,
            tr("Emergency translation", "জরুরি অনুবাদ"),
            tr("Chakma · Marma · Rohingya · Kokborok · Santali · Garo",
                "চাকমা · মারমা · রোহিঙ্গা · ককবরক · সাঁওতালি · গারো"),
            onTranslate,
        )
        MoreRow(
            FeatherIcons.AlertTriangle, CautionAmber,
            tr("Who needs help first", "আগে কার সাহায্য দরকার"),
            tr("Rank every case logged on this phone", "এই ফোনের সব কেস অগ্রাধিকার অনুযায়ী"),
            onTriage,
        )
        MoreRow(
            FeatherIcons.BarChart2, BrandTeal,
            tr("Situation briefing", "পরিস্থিতির সারসংক্ষেপ"),
            tr("Counts, worst cases, shelter pressure", "সংখ্যা, খারাপ কেস, আশ্রয়ের চাপ"),
            onSummary,
        )

        Spacer(Modifier.height(18.dp))
        Section(tr("Numbers and help", "নম্বর ও সহায়তা"))
        MoreRow(
            FeatherIcons.PhoneCall, ErrorRed,
            tr("All emergency numbers", "সব জরুরি নম্বর"),
            tr("999, flood warning, Coast Guard and more", "৯৯৯, বন্যা সতর্কতা, কোস্ট গার্ড ও আরও"),
            onEmergency,
        )
        MoreRow(
            FeatherIcons.HelpCircle, BrandTeal,
            tr("How to use Nongor", "নোঙর যেভাবে ব্যবহার করবেন"),
            tr("What each tool does, and a flood drill", "প্রতিটি টুল কী করে, ও একটি মহড়া"),
            onGuide,
        )
        MoreRow(
            FeatherIcons.Settings, TextSecondary,
            tr("Settings", "সেটিংস"),
            tr("Language, your name, the optional AI model", "ভাষা, আপনার নাম, ঐচ্ছিক এআই মডেল"),
            onSettings,
        )
    }
}

@Composable
private fun Section(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = TextSecondary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun MoreRow(
    icon: ImageVector,
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
            Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
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
