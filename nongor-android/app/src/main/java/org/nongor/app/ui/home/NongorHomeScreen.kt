package org.nongor.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import compose.icons.FeatherIcons
import compose.icons.feathericons.Activity
import compose.icons.feathericons.AlertTriangle
import compose.icons.feathericons.BarChart2
import compose.icons.feathericons.ChevronRight
import compose.icons.feathericons.Heart
import compose.icons.feathericons.HelpCircle
import compose.icons.feathericons.MapPin
import compose.icons.feathericons.MessageCircle
import compose.icons.feathericons.MessageSquare
import compose.icons.feathericons.PhoneCall
import compose.icons.feathericons.Play
import compose.icons.feathericons.Radio
import compose.icons.feathericons.Settings
import compose.icons.feathericons.Shield
import compose.icons.feathericons.Users
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nongor.app.R
import org.nongor.app.ui.components.HeroBanner
import org.nongor.app.ui.demo.Actions
import org.nongor.app.ui.demo.DemoDialog
import org.nongor.app.ui.emergency.dialNumber
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.BrandBlue
import org.nongor.app.ui.theme.BgCard
import org.nongor.app.ui.theme.TextPrimary
import org.nongor.app.ui.theme.TextSecondary
import org.nongor.app.ui.theme.TileAidBg
import org.nongor.app.ui.theme.TileAidFg
import org.nongor.app.ui.theme.TileChatBg
import org.nongor.app.ui.theme.TileChatFg
import org.nongor.app.ui.theme.TileCommunityBg
import org.nongor.app.ui.theme.TileCommunityFg
import org.nongor.app.ui.theme.TileFamilyBg
import org.nongor.app.ui.theme.TileFamilyFg
import org.nongor.app.ui.theme.TileMeshBg
import org.nongor.app.ui.theme.TileMeshFg
import org.nongor.app.ui.theme.TileShelterBg
import org.nongor.app.ui.theme.TileShelterFg
import org.nongor.app.ui.theme.TileSummaryBg
import org.nongor.app.ui.theme.TileSummaryFg
import org.nongor.app.ui.theme.TileTriageBg
import org.nongor.app.ui.theme.TileTriageFg

@Composable
fun NongorHomeScreen(
    onTriage: () -> Unit,
    onFirstAid: () -> Unit,
    onGis: () -> Unit,
    onSummary: () -> Unit,
    onMesh: () -> Unit,
    onChat: () -> Unit,
    onGuide: () -> Unit = {},
    onSettings: () -> Unit = {},
    onEmergency: () -> Unit = {},
    onCommunity: () -> Unit = {},
    onFamily: () -> Unit = {},
    onTranslate: () -> Unit = {},
    onSeedDemo: () -> Unit = {},
    modelReady: Boolean = true,
    showCoach: Boolean = false,
    onCoachDismiss: () -> Unit = {},
) {
    var showDemo by remember { mutableStateOf(false) }
    if (showDemo) {
        DemoDialog(
            onDismiss = { showDemo = false },
            onSeed = onSeedDemo,
            actions = Actions(
                triage = onTriage, firstAid = onFirstAid, shelter = onGis,
                mesh = onMesh, summary = onSummary,
            ),
        )
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 12.dp, bottom = 28.dp),
    ) {
        // ---- Header: logo + brand + actions ----
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Image(painterResource(R.drawable.nongor_app_icon), contentDescription = null,
                modifier = Modifier.size(48.dp).clip(CircleShape))
            Spacer(Modifier.width(8.dp))
            Column {
                Text("নোঙর", color = BrandBlue, fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp, lineHeight = 26.sp)
                Text("NONGOR", color = BrandBlue, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 3.sp,
                    modifier = Modifier.offset(y = (-5).dp))
            }
            Spacer(Modifier.weight(1f))
            CircleIconButton(FeatherIcons.HelpCircle, onClick = onGuide)
            Spacer(Modifier.width(10.dp))
            CircleIconButton(FeatherIcons.Settings, onClick = onSettings)
        }

        Spacer(Modifier.height(18.dp))
        Text(tr("Assalamu Alaikum", "আসসালামু আলাইকুম"), color = TextSecondary,
            style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(2.dp))
        Text("কিভাবে সাহায্য করতে পারি?", color = TextPrimary,
            fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp)
        Spacer(Modifier.height(6.dp))
        Text(tr("Everything here works with no internet and no signal",
            "এখানকার সবকিছু ইন্টারনেট বা নেটওয়ার্ক ছাড়াই চলে"),
            color = TextSecondary, style = MaterialTheme.typography.bodyMedium)

        // ---- Emergency call: always the most prominent action ----
        Spacer(Modifier.height(14.dp))
        EmergencyCard(onEmergency = onEmergency)

        // ---- Emergency translation: kept alongside the call button because a rescuer who
        // cannot understand the person in front of them is stuck before any other tool helps.
        Spacer(Modifier.height(10.dp))
        TranslateCard(onTranslate = onTranslate)

        // ---- Flood drill launcher ----
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(BrandBlue.copy(alpha = 0.10f))
                .clickable { showDemo = true }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(36.dp).clip(CircleShape).background(BrandBlue),
                contentAlignment = Alignment.Center) {
                Icon(FeatherIcons.Play, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(tr("Try a flood drill", "একটি বন্যা মহড়া করুন"), color = TextPrimary,
                    fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(tr("A guided walkthrough of every tool", "প্রতিটি টুলের ধাপে ধাপে পরিচিতি"),
                    color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }

        // ---- First-run coach balloon (shown once) ----
        if (showCoach) CoachBalloon(onDismiss = onCoachDismiss)

        // ---- Hero illustration ----
        Spacer(Modifier.height(16.dp))
        HeroBanner(
            R.drawable.hero_home,
            title = tr("We're here to help", "আমরা পাশে আছি"),
            subtitle = tr("Offline AI, right on your device", "অফলাইন এআই, আপনার ডিভাইসেই"),
        )

        // ---- Offline mode status card ----
        Spacer(Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(18.dp),
        ) {
            // Status reflects whether Gemma is actually on the device — never claim "AI ready"
            // when the model was skipped and only the deterministic core tools are available.
            val dotColor = if (modelReady) Color(0xFF22A565) else Color(0xFFF5822B)
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(CircleShape).background(BrandBlue.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center) {
                    Icon(FeatherIcons.Shield, null, tint = BrandBlue, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(tr("You are in ", "আপনি এখন "), color = TextPrimary,
                            style = MaterialTheme.typography.bodyMedium)
                        Text(tr("Offline Mode", "অফলাইন মোডে"), color = BrandBlue,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium)
                        Text(" ●", color = dotColor,
                            style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        if (modelReady)
                            tr("All tools ready, plus the optional AI model on this device.",
                                "সব টুল প্রস্তুত, সাথে এই ডিভাইসে ঐচ্ছিক এআই মডেলও আছে।")
                        else
                            tr("All rescue tools ready. The optional AI model is not installed.",
                                "সব উদ্ধার টুল প্রস্তুত। ঐচ্ছিক এআই মডেল ইনস্টল করা নেই।"),
                        color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // ---- Feature grid (2 x 4), ordered most-urgent first: reaching help and getting to
        // safety lead; the coordinator tools (Triage, Summary) sit at the bottom. ----
        Spacer(Modifier.height(18.dp))
        FeatureRow(
            left = { FeatureCard(TileMeshBg, TileMeshFg, FeatherIcons.Radio,
                tr("Mesh SOS", "মেশ এসওএস"),
                tr("Send & receive SOS offline via mesh", "মেশে অফলাইনে এসওএস পাঠান ও নিন"), onMesh, it) },
            right = { FeatureCard(TileShelterBg, TileShelterFg, FeatherIcons.MapPin,
                tr("Safe Shelter & Route", "নিরাপদ আশ্রয় ও পথ"),
                tr("Find safe places and best routes", "নিরাপদ জায়গা ও সেরা পথ খুঁজুন"), onGis, it) },
        )
        Spacer(Modifier.height(14.dp))
        FeatureRow(
            left = { FeatureCard(TileAidBg, TileAidFg, FeatherIcons.Activity,
                tr("First Aid", "প্রাথমিক চিকিৎসা"),
                tr("Simple, trusted medical guidance", "সহজ, নির্ভরযোগ্য চিকিৎসা পরামর্শ"), onFirstAid, it) },
            right = { FeatureCard(TileFamilyBg, TileFamilyFg, FeatherIcons.Heart,
                tr("Family Reunion", "পরিবার পুনর্মিলন"),
                tr("Find separated family when phones pass nearby", "ফোন কাছাকাছি এলে বিচ্ছিন্ন পরিবার খুঁজুন"), onFamily, it) },
        )
        Spacer(Modifier.height(14.dp))
        FeatureRow(
            left = { FeatureCard(TileCommunityBg, TileCommunityFg, FeatherIcons.Users,
                tr("Community Board", "কমিউনিটি বোর্ড"),
                tr("See what's happening nearby over mesh", "কাছে কী ঘটছে দেখুন — মেশে অফলাইনে"), onCommunity, it) },
            right = { FeatureCard(TileChatBg, TileChatFg, FeatherIcons.MessageCircle,
                tr("AI Assistant", "এআই সহকারী"),
                tr("Ask anything about flood safety & first aid", "বন্যা ও প্রাথমিক চিকিৎসা নিয়ে জিজ্ঞেস করুন"), onChat, it) },
        )
        Spacer(Modifier.height(14.dp))
        FeatureRow(
            left = { FeatureCard(TileTriageBg, TileTriageFg, FeatherIcons.AlertTriangle,
                tr("Rescue Triage", "উদ্ধার ট্রায়াজ"),
                tr("Prioritize SOS and urgent cases", "জরুরি এসওএস অগ্রাধিকার দিন"), onTriage, it) },
            right = { FeatureCard(TileSummaryBg, TileSummaryFg, FeatherIcons.BarChart2,
                tr("Coordinator Summary", "সমন্বয়কারী সারাংশ"),
                tr("Overview, reports and insights", "সারচিত্র, রিপোর্ট ও বিশ্লেষণ"), onSummary, it) },
        )
    }
}

/**
 * High-contrast emergency-call entry, kept at the top so Nongor reads as an emergency tool first.
 * Tapping the body opens the full list of official hotlines; the "999" pill dials the national
 * emergency line straight away (via the dialer, so the user still confirms the call).
 */
@Composable
private fun EmergencyCard(onEmergency: () -> Unit) {
    val context = LocalContext.current
    val red = Color(0xFFD92D20)
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(red)
            .clickable(onClick = onEmergency).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center) {
            Icon(FeatherIcons.PhoneCall, null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(tr("Emergency call", "জরুরি কল"), color = Color.White,
                fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Text(tr("Police · Fire · Ambulance, and more", "পুলিশ · ফায়ার · অ্যাম্বুলেন্স, আরও"),
                color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier.clip(RoundedCornerShape(12.dp)).background(Color.White)
                .clickable { dialNumber(context, "999") }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(tr("Call 999", "৯৯৯"), color = red, fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleSmall)
        }
    }
}

/**
 * Entry point to the phrasebook.
 *
 * Deliberately not buried in the feature grid: in the hill tracts and the Cox's Bazar camps
 * the language gap is the first thing a responder hits, before triage or routing matter.
 */
@Composable
private fun TranslateCard(onTranslate: () -> Unit) {
    val teal = Color(0xFF0E7C86)
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(teal.copy(alpha = 0.12f))
            .clickable(onClick = onTranslate).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(teal),
            contentAlignment = Alignment.Center) {
            Icon(FeatherIcons.MessageSquare, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(tr("Emergency translation", "জরুরি অনুবাদ"), color = TextPrimary,
                fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Text(
                tr("Chakma · Marma · Rohingya · sign — pictures when there are no words",
                    "চাকমা · মারমা · রোহিঙ্গা · ইশারা — শব্দ না থাকলে ছবি"),
                color = TextSecondary, style = MaterialTheme.typography.bodySmall,
            )
        }
        Icon(FeatherIcons.ChevronRight, null, tint = TextSecondary)
    }
}

/** One-time speech balloon that points up at the flood-drill card to orient a first-time user. */
@Composable
private fun CoachBalloon(onDismiss: () -> Unit) {
    Column {
        Spacer(Modifier.height(6.dp))
        // little upward pointer aligned under the drill card's icon
        Canvas(Modifier.padding(start = 22.dp).size(width = 18.dp, height = 9.dp)) {
            val p = Path().apply {
                moveTo(0f, size.height); lineTo(size.width / 2f, 0f); lineTo(size.width, size.height); close()
            }
            drawPath(p, BrandBlue)
        }
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(BrandBlue)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(tr("👋 New here?", "👋 নতুন এসেছেন?"), color = Color.White,
                    fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(2.dp))
                Text(
                    tr("Tap “Try a flood drill” above for a quick guided tour of every tool — " +
                        "or pick any tile below to start.",
                        "উপরে “একটি বন্যা মহড়া করুন”-এ চাপ দিন প্রতিটি টুলের দ্রুত পরিচিতির জন্য — " +
                            "অথবা নিচের যেকোনো টাইলে চাপ দিন।"),
                    color = Color.White.copy(alpha = 0.92f), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier.clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.18f))
                    .clickable(onClick = onDismiss).padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(tr("Got it", "বুঝেছি"), color = Color.White, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun FeatureRow(
    left: @Composable (Modifier) -> Unit,
    right: @Composable (Modifier) -> Unit,
) {
    Row(Modifier.fillMaxWidth()) {
        left(Modifier.weight(1f))
        Spacer(Modifier.width(14.dp))
        right(Modifier.weight(1f))
    }
}

@Composable
private fun FeatureCard(
    bg: Color, fg: Color, icon: ImageVector, title: String, subtitle: String,
    onClick: () -> Unit, modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.height(196.dp).clip(RoundedCornerShape(22.dp)).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(fg.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = fg, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun CircleIconButton(icon: ImageVector, onClick: () -> Unit) {
    Box(
        Modifier.size(42.dp).clip(CircleShape).background(BgCard).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = TextPrimary, modifier = Modifier.size(22.dp))
    }
}
