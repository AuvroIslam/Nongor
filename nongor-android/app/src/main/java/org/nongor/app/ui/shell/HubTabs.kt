package org.nongor.app.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.Box
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
import compose.icons.feathericons.BarChart2
import compose.icons.feathericons.ChevronRight
import compose.icons.feathericons.Heart
import compose.icons.feathericons.MapPin
import compose.icons.feathericons.Users
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.BgCard
import org.nongor.app.ui.theme.BrandTeal
import org.nongor.app.ui.theme.SafeGreen
import org.nongor.app.ui.theme.ShapeMd
import org.nongor.app.ui.theme.TextPrimary
import org.nongor.app.ui.theme.TextSecondary

/** Getting somewhere: where is safe, and who is missing. */
@Composable
fun MoveTab(onShelter: () -> Unit, onFamily: () -> Unit) {
    HubColumn(
        title = tr("Get to safety", "নিরাপদে যান"),
        subtitle = tr(
            "Where to go, and who you should not leave behind.",
            "কোথায় যাবেন, আর কাকে ফেলে যাবেন না।",
        ),
    ) {
        HubRow(
            FeatherIcons.MapPin, BrandTeal,
            tr("Nearest safe shelter", "নিকটতম নিরাপদ আশ্রয়"),
            tr(
                "A route that avoids water already over the road",
                "রাস্তায় পানি উঠে গেছে এমন পথ এড়িয়ে",
            ),
            onShelter,
        )
        Spacer(Modifier.height(10.dp))
        HubRow(
            FeatherIcons.Heart, SafeGreen,
            tr("Find your family", "পরিবার খুঁজুন"),
            tr(
                "A compass to whoever passed within radio range",
                "রেডিও পাল্লায় যারা এসেছে তাদের দিক নির্দেশ",
            ),
            onFamily,
        )
    }
}

/** What everyone else can see: the neighbourhood board and the coordinator's picture. */
@Composable
fun AreaTab(onBoard: () -> Unit, onSummary: () -> Unit) {
    HubColumn(
        title = tr("Around you", "আপনার চারপাশ"),
        subtitle = tr(
            "What neighbours are reporting, and the whole picture.",
            "প্রতিবেশীরা যা জানাচ্ছে, আর সামগ্রিক চিত্র।",
        ),
    ) {
        HubRow(
            FeatherIcons.Users, BrandTeal,
            tr("Neighbourhood board", "এলাকার বোর্ড"),
            tr(
                "Flooded roads, full shelters, where supplies are",
                "ডুবে যাওয়া রাস্তা, ভরা আশ্রয়, কোথায় ত্রাণ আছে",
            ),
            onBoard,
        )
        Spacer(Modifier.height(10.dp))
        HubRow(
            FeatherIcons.BarChart2, BrandTeal,
            tr("Situation briefing", "পরিস্থিতির সারসংক্ষেপ"),
            tr(
                "Counts, worst cases and shelter pressure",
                "সংখ্যা, সবচেয়ে খারাপ কেস ও আশ্রয়ের চাপ",
            ),
            onSummary,
        )
    }
}

@Composable
private fun HubColumn(title: String, subtitle: String, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 16.dp, bottom = 24.dp),
    ) {
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
        )
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(16.dp))
        content()
    }
}

@Composable
private fun HubRow(
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
