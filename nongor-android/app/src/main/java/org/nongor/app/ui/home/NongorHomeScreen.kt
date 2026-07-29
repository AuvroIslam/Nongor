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
import org.nongor.app.ui.theme.ShapeLg
import org.nongor.app.ui.theme.ShapeMd
import org.nongor.app.ui.theme.ShapeSm
import androidx.compose.foundation.BorderStroke
import org.nongor.app.ui.theme.GlassBorder
import org.nongor.app.ui.theme.Stroke
import compose.icons.feathericons.Anchor
import org.nongor.app.ui.theme.BrandTeal
import org.nongor.app.ui.theme.CautionAmber
import org.nongor.app.ui.theme.ErrorRed
import org.nongor.app.ui.theme.SafeGreen
import org.nongor.app.ui.theme.ShapePill

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
    sosActive: Boolean = false,
    sirenOn: Boolean = false,
    onStopSos: () -> Unit = {},
) {
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

        // ---- First-run coach balloon, pointing up at the help button ----
        if (showCoach) CoachBalloon(onDismiss = onCoachDismiss)

        Spacer(Modifier.height(18.dp))
        Text(tr("Assalamu Alaikum", "আসসালামু আলাইকুম"), color = TextSecondary,
            style = MaterialTheme.typography.bodyLarge)

        // The banner carries the greeting straight into what the app is for, so there is no
        // separate headline repeating it.
        Spacer(Modifier.height(10.dp))
        HeroBanner(
            FeatherIcons.Anchor, tint = BrandTeal,
            title = tr("We're here to help", "আমরা পাশে আছি"),
            subtitle = tr("Signed SOS, shelters and translation — all offline", "স্বাক্ষরিত এসওএস, আশ্রয় ও অনুবাদ — সবই অফলাইনে"),
        )

        // ---- A live SOS outranks everything, including the call button ----
        if (sosActive) {
            Spacer(Modifier.height(14.dp))
            SosActiveBanner(sirenOn = sirenOn, onStop = onStopSos, onOpen = onMesh)
        }

        // ---- Emergency call: always the most prominent action ----
        Spacer(Modifier.height(14.dp))
        EmergencyCard(onEmergency = onEmergency)

        // ---- Emergency translation: kept alongside the call button because a rescuer who
        // cannot understand the person in front of them is stuck before any other tool helps.
        Spacer(Modifier.height(10.dp))
        TranslateCard(onTranslate = onTranslate)

        // ---- Offline mode status card ----
        Spacer(Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(Stroke.hairline, GlassBorder),
            shape = ShapeMd,
        ) {
            // Status reflects whether Gemma is actually on the device — never claim "AI ready"
            // when the model was skipped and only the deterministic core tools are available.
            val dotColor = if (modelReady) SafeGreen else CautionAmber
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
    val red = ErrorRed
    Row(
        Modifier.fillMaxWidth().clip(ShapeMd).background(red)
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
            Modifier.clip(ShapeSm).background(Color.White)
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
 * Shown on the home screen for as long as an SOS is being broadcast.
 *
 * The stop control has to be reachable from wherever the user happens to be. An alarm at full
 * volume that can only be silenced from one particular screen is the kind of thing that makes
 * someone force-quit the app — and force-quitting is exactly what must not happen to the
 * thing that is calling for help on their behalf.
 */
@Composable
private fun SosActiveBanner(sirenOn: Boolean, onStop: () -> Unit, onOpen: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(ShapeMd)
            .background(ErrorRed.copy(alpha = 0.12f))
            .clickable(onClick = onOpen)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(ErrorRed),
            contentAlignment = Alignment.Center) {
            Icon(FeatherIcons.Radio, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(tr("SOS is being sent", "এসওএস পাঠানো হচ্ছে"), color = ErrorRed,
                fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Text(
                if (sirenOn) {
                    tr("Repeating, alarm sounding", "বারবার যাচ্ছে, শব্দ বাজছে")
                } else {
                    tr("Repeating, alarm silenced", "বারবার যাচ্ছে, শব্দ বন্ধ")
                },
                color = TextSecondary, style = MaterialTheme.typography.bodySmall,
            )
        }
        Box(
            Modifier.clip(ShapePill).background(ErrorRed)
                .clickable(onClick = onStop)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(tr("Stop", "থামান"), color = Color.White, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge)
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
        Modifier.fillMaxWidth().clip(ShapeMd)
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
        // The pointer sits under the help button in the header rather than under a card,
        // because "?" is now the single place a first-time user is being sent.
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.weight(1f))
            Canvas(Modifier.size(width = 18.dp, height = 9.dp)) {
                val p = Path().apply {
                    moveTo(0f, size.height)
                    lineTo(size.width / 2f, 0f)
                    lineTo(size.width, size.height)
                    close()
                }
                drawPath(p, BrandBlue)
            }
            // Half the settings button plus the gap, so the tip lands on the "?" not the gear.
            Spacer(Modifier.width(64.dp))
        }
        Row(
            Modifier.fillMaxWidth().clip(ShapeMd).background(BrandBlue)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(tr("👋 New here?", "👋 নতুন এসেছেন?"), color = Color.White,
                    fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(2.dp))
                Text(
                    tr("Tap “?” above for what each tool does, and a guided flood drill — " +
                        "or pick any tile below to start.",
                        "উপরের “?”-এ চাপ দিন — প্রতিটি টুল কী করে ও একটি বন্যা মহড়া পাবেন। " +
                            "অথবা নিচের যেকোনো টাইলে চাপ দিন।"),
                    color = Color.White.copy(alpha = 0.92f), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier.clip(ShapeSm).background(Color.White.copy(alpha = 0.18f))
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
        modifier = modifier.height(196.dp).clip(ShapeLg).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(Stroke.hairline, GlassBorder),
        shape = ShapeLg,
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
