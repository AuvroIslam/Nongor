package org.nongor.app.ui.guide

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import compose.icons.FeatherIcons
import compose.icons.feathericons.Activity
import compose.icons.feathericons.AlertTriangle
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.BarChart2
import compose.icons.feathericons.MapPin
import compose.icons.feathericons.MessageCircle
import compose.icons.feathericons.Radio
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.nongor.app.R
import org.nongor.app.ui.i18n.LocalBangla
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.AccentPurple
import org.nongor.app.ui.theme.BgCard
import org.nongor.app.ui.theme.TextPrimary
import org.nongor.app.ui.theme.TextSecondary
import org.nongor.app.ui.theme.TileAidBg
import org.nongor.app.ui.theme.TileAidFg
import org.nongor.app.ui.theme.TileChatBg
import org.nongor.app.ui.theme.TileChatFg
import org.nongor.app.ui.theme.TileMeshBg
import org.nongor.app.ui.theme.TileMeshFg
import org.nongor.app.ui.theme.TileShelterBg
import org.nongor.app.ui.theme.TileShelterFg
import org.nongor.app.ui.theme.TileSummaryBg
import org.nongor.app.ui.theme.TileSummaryFg
import org.nongor.app.ui.theme.TileTriageBg
import org.nongor.app.ui.theme.TileTriageFg

private data class Feature(
    val icon: ImageVector, val bg: Color, val fg: Color,
    val titleEn: String, val titleBn: String, val descEn: String, val descBn: String,
)

private val FEATURES = listOf(
    Feature(FeatherIcons.AlertTriangle, TileTriageBg, TileTriageFg,
        "Rescue Triage", "উদ্ধার ট্রায়াজ",
        "Type or photograph an SOS and I rank its urgency, so the worst cases get help first.",
        "এসওএস লিখুন বা ছবি দিন, আমি জরুরিতা সাজিয়ে দিই যাতে সবচেয়ে খারাপ অবস্থায় আগে সাহায্য যায়।"),
    Feature(FeatherIcons.Activity, TileAidBg, TileAidFg,
        "First Aid", "প্রাথমিক চিকিৎসা",
        "Describe an injury and I give clear, cited first aid in Bangla and English.",
        "আঘাতের কথা বলুন, আমি বাংলা ও ইংরেজিতে স্পষ্ট, উৎসসহ প্রাথমিক চিকিৎসা দিই।"),
    Feature(FeatherIcons.MapPin, TileShelterBg, TileShelterFg,
        "Safe Shelter", "নিরাপদ আশ্রয়",
        "I find the nearest safe shelter on high ground, and a way there that avoids flooding.",
        "উঁচু জায়গার নিকটতম নিরাপদ আশ্রয় ও বন্যা এড়িয়ে সেখানে যাওয়ার পথ খুঁজে দিই।"),
    Feature(FeatherIcons.BarChart2, TileSummaryBg, TileSummaryFg,
        "Coordinator Summary", "সমন্বয়কারী সারাংশ",
        "I turn all the field reports into one clear briefing with counts, top cases and shortages.",
        "সব রিপোর্টকে একটি স্পষ্ট ব্রিফিংয়ে পরিণত করি — সংখ্যা, শীর্ষ কেস ও ঘাটতিসহ।"),
    Feature(FeatherIcons.Radio, TileMeshBg, TileMeshFg,
        "Mesh SOS", "মেশ এসওএস",
        "Send an SOS from phone to phone with no internet. Nearby phones pass it onward.",
        "ইন্টারনেট ছাড়াই ফোন থেকে ফোনে এসওএস পাঠান। কাছের ফোনগুলো তা এগিয়ে দেয়।"),
    Feature(FeatherIcons.MessageCircle, TileChatBg, TileChatFg,
        "AI Assistant", "এআই সহকারী",
        "Ask me anything about flood safety or first aid, answered right on your phone.",
        "বন্যা নিরাপত্তা বা প্রাথমিক চিকিৎসা নিয়ে যা খুশি জিজ্ঞেস করুন, উত্তর মিলবে আপনার ফোনেই।"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("How to use Nongor", "নোঙর যেভাবে ব্যবহার করবেন")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(FeatherIcons.ArrowLeft, contentDescription = "Back")
                    }
                },
            )
        },
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
        ) {
            // Intro with mascot
            Image(painterResource(R.drawable.mascot_1), null,
                modifier = Modifier.size(150.dp).align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Fit)
            Text(tr("Hi, I'm Nongor! 🦉", "আমি নোঙর! 🦉"), color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(6.dp))
            Text(tr("Your offline flood helper. Everything runs on this phone, with no internet.",
                "আপনার অফলাইন বন্যা সহায়ক। সবকিছু ইন্টারনেট ছাড়াই এই ফোনেই চলে।"),
                color = TextSecondary, style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(22.dp))
            SectionTitle(tr("What each tool does", "প্রতিটি টুল কী করে"))
            Spacer(Modifier.height(10.dp))
            FEATURES.forEach { f ->
                FeatureExplainer(f)
                Spacer(Modifier.height(10.dp))
            }

            // How to navigate
            Spacer(Modifier.height(12.dp))
            MascotNote(
                mascot = R.drawable.mascot_2,
                title = tr("How to get around", "কীভাবে চলাচল করবেন"),
                lines = listOf(
                    tr("1.  Tap any card on the Home screen to open a tool.",
                        "১.  হোম স্ক্রিনে যেকোনো কার্ডে চাপ দিলে টুল খুলবে।"),
                    tr("2.  Use the  ←  back arrow at the top to return Home.",
                        "২.  উপরের  ←  তীর দিয়ে হোমে ফিরে আসুন।"),
                    tr("3.  In an emergency, start with Rescue Triage or Mesh SOS.",
                        "৩.  জরুরি অবস্থায় উদ্ধার ট্রায়াজ বা মেশ এসওএস দিয়ে শুরু করুন।"),
                ),
            )

            Spacer(Modifier.height(16.dp))
            MascotNote(
                mascot = R.drawable.mascot_3,
                title = tr("Good to know", "জেনে রাখা ভালো"),
                lines = listOf(
                    tr("• Everything works fully offline, even in airplane mode.",
                        "• সবকিছু সম্পূর্ণ অফলাইনে চলে, এমনকি এয়ারপ্লেন মোডেও।"),
                    tr("• First Aid answers are grounded in WHO and IFRC guidance, and cited.",
                        "• প্রাথমিক চিকিৎসার উত্তর WHO ও IFRC নির্দেশনার ভিত্তিতে, উৎসসহ।"),
                    tr("• Mesh SOS needs Bluetooth and Wi-Fi on, plus a second phone nearby.",
                        "• মেশ এসওএস-এর জন্য ব্লুটুথ ও ওয়াই-ফাই চালু এবং কাছে আরেকটি ফোন লাগে।"),
                    tr("• AI guidance supports, and does not replace, professional help.",
                        "• এআই পরামর্শ পেশাদার সাহায্যের পরিপূরক, বিকল্প নয়।"),
                ),
            )

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = TextPrimary, fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun FeatureExplainer(f: Feature) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).clip(CircleShape).background(f.bg),
                contentAlignment = Alignment.Center) {
                Icon(f.icon, null, tint = f.fg, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(14.dp))
            val bangla = LocalBangla.current
            Column {
                Text(if (bangla) f.titleBn else f.titleEn, color = TextPrimary,
                    fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(if (bangla) f.descBn else f.descEn, color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun MascotNote(@DrawableRes mascot: Int, title: String, lines: List<String>) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AccentPurple.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Image(painterResource(mascot), null, modifier = Modifier.size(74.dp),
                contentScale = ContentScale.Fit)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.padding(top = 6.dp)) {
                Text(title, color = AccentPurple, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                lines.forEach {
                    Text(it, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}
