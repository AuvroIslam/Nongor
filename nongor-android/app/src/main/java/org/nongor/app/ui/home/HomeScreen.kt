package org.nongor.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.nongor.app.ui.Routes
import org.nongor.app.ui.components.Chip
import org.nongor.app.ui.i18n.LocalBangla
import org.nongor.app.ui.i18n.t
import org.nongor.app.ui.theme.NongorColors

private data class Feature(
    val route: String,
    val icon: ImageVector,
    val en: String,
    val bn: String,
    val subEn: String,
    val subBn: String,
    val tint: Color,
)

private val FEATURES = listOf(
    Feature(
        Routes.TRANSLATE, Icons.Filled.Translate,
        "Translate", "অনুবাদ",
        "Bangla ↔ Chakma, Marma, Rohingya", "বাংলা ↔ চাকমা, মারমা, রোহিঙ্গা",
        NongorColors.Amber,
    ),
    Feature(
        Routes.EMERGENCY, Icons.Filled.Call,
        "Emergency call", "জরুরি কল",
        "999 and official hotlines", "৯৯৯ ও সরকারি হটলাইন",
        NongorColors.Danger,
    ),
    Feature(
        Routes.FIRST_AID, Icons.Filled.HealthAndSafety,
        "First aid", "প্রাথমিক চিকিৎসা",
        "Cited, offline steps", "সূত্রসহ, অফলাইন",
        NongorColors.Safe,
    ),
    Feature(
        Routes.SHELTER, Icons.Outlined.LocationOn,
        "Safe shelter", "নিরাপদ আশ্রয়",
        "Route around flooded roads", "ডুবে যাওয়া রাস্তা এড়িয়ে",
        NongorColors.Surf,
    ),
    Feature(
        Routes.MESH, Icons.Filled.Hub,
        "Mesh SOS", "মেশ SOS",
        "Phone to phone, no network", "ফোন থেকে ফোন, নেটওয়ার্ক ছাড়া",
        NongorColors.Deep,
    ),
    Feature(
        Routes.TRIAGE, Icons.Filled.Bolt,
        "Rescue triage", "উদ্ধার ট্রায়াজ",
        "Who needs help first", "আগে কার সাহায্য দরকার",
        NongorColors.Caution,
    ),
    Feature(
        Routes.COMMUNITY, Icons.Filled.Campaign,
        "Area board", "এলাকা বোর্ড",
        "What neighbours are reporting", "প্রতিবেশীরা যা জানাচ্ছে",
        NongorColors.Surf,
    ),
    Feature(
        Routes.FAMILY, Icons.Filled.Diversity3,
        "Find family", "পরিবার খুঁজুন",
        "Private nearby beacon", "গোপন কাছাকাছি সংকেত",
        NongorColors.Safe,
    ),
    Feature(
        Routes.SUMMARY, Icons.Filled.Summarize,
        "Coordinator", "সমন্বয়",
        "Situation briefing", "পরিস্থিতির সারসংক্ষেপ",
        NongorColors.Deep,
    ),
    Feature(
        Routes.ASSISTANT, Icons.Filled.Forum,
        "Ask Nongor", "নোঙরকে জিজ্ঞাসা",
        "Offline answers", "অফলাইন উত্তর",
        NongorColors.Muted,
    ),
    Feature(
        Routes.GUIDE, Icons.AutoMirrored.Filled.MenuBook,
        "Guide & drill", "গাইড ও মহড়া",
        "Practise before it happens", "ঘটার আগে অনুশীলন",
        NongorColors.Caution,
    ),
)

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onToggleLanguage: () -> Unit,
) {
    val bangla = LocalBangla.current
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 28.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            Column {
                Spacer(Modifier.height(28.dp))
                HomeHeader(
                    bangla = bangla,
                    onToggleLanguage = onToggleLanguage,
                    onSettings = { onNavigate(Routes.SETTINGS) },
                )
                Spacer(Modifier.height(16.dp))
                SosBanner(onClick = { onNavigate(Routes.MESH) })
                Spacer(Modifier.height(18.dp))
                Text(
                    t("Everything below works with no internet.", "নিচের সবকিছু ইন্টারনেট ছাড়াই চলে।"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        items(FEATURES) { f ->
            FeatureTile(f, bangla) { onNavigate(f.route) }
        }
    }
}

@Composable
private fun HomeHeader(
    bangla: Boolean,
    onToggleLanguage: () -> Unit,
    onSettings: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                "নোঙর",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                t(
                    "Nongor — what holds when everything else moves",
                    "সব কিছু ভেসে গেলেও যা ধরে রাখে",
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Chip(
                text = t("Works offline", "অফলাইনে চলে"),
                color = NongorColors.Safe,
                icon = Icons.Filled.WifiOff,
            )
        }
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            modifier = Modifier.clickable(onClick = onToggleLanguage),
        ) {
            Text(
                if (bangla) "EN" else "বাং",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
        IconButton(onClick = onSettings) {
            Icon(
                Icons.Filled.Settings,
                contentDescription = t("Settings", "সেটিংস"),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SosBanner(onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Box(
            Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(NongorColors.Danger, NongorColors.DangerDark),
                    ),
                    RoundedCornerShape(20.dp),
                )
                .padding(18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        t("Send an SOS", "SOS পাঠান"),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                    )
                    Text(
                        t(
                            "Signed, relayed phone to phone. No tower needed.",
                            "স্বাক্ষরিত, ফোন থেকে ফোনে যায়। টাওয়ার লাগে না।",
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.92f),
                    )
                }
                Icon(
                    Icons.Filled.Hub,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp),
                )
            }
        }
    }
}

@Composable
private fun FeatureTile(f: Feature, bangla: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .aspectRatio(0.98f)
            .clickable(onClick = onClick),
    ) {
        Column(
            Modifier
                .border(1.dp, f.tint.copy(alpha = 0.30f), RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .background(f.tint.copy(alpha = 0.14f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(f.icon, contentDescription = null, tint = f.tint, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.weight(1f))
            Text(
                if (bangla) f.bn else f.en,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                if (bangla) f.subBn else f.subEn,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
            )
        }
    }
}
