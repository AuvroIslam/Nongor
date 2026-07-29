package org.nongor.app.ui.guide

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Activity
import compose.icons.feathericons.AlertTriangle
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.BarChart2
import compose.icons.feathericons.ChevronRight
import compose.icons.feathericons.MapPin
import compose.icons.feathericons.Play
import compose.icons.feathericons.Send
import compose.icons.feathericons.Users
import org.nongor.app.R
import org.nongor.app.ui.components.AnchorBadge
import org.nongor.app.ui.demo.Actions
import org.nongor.app.ui.demo.DemoDialog
import org.nongor.app.ui.i18n.LocalBangla
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.AidRose
import org.nongor.app.ui.theme.BgCard
import org.nongor.app.ui.theme.BrandTeal
import org.nongor.app.ui.theme.BrandTealDeep
import org.nongor.app.ui.theme.BriefBlue
import org.nongor.app.ui.theme.TileShelterFg as ShelterGreen
import org.nongor.app.ui.theme.CautionAmber
import org.nongor.app.ui.theme.ErrorRed
import org.nongor.app.ui.theme.GlassBorder
import org.nongor.app.ui.theme.SafeGreen
import org.nongor.app.ui.theme.ShapeMd
import org.nongor.app.ui.theme.ShapeSm
import org.nongor.app.ui.theme.Stroke
import org.nongor.app.ui.theme.TextPrimary
import org.nongor.app.ui.theme.TextSecondary

/**
 * One tool, explained in a sentence.
 *
 * The icon is either a Feather glyph or one of the app's own drawables, because several tools
 * are recognised by a custom mark (the compass, the Gemma logo) and the guide is only useful if
 * what it shows is exactly what you will see on the screen.
 */
private data class Feature(
    val tint: Color,
    val titleEn: String,
    val titleBn: String,
    val descEn: String,
    val descBn: String,
    val icon: ImageVector? = null,
    @DrawableRes val painter: Int? = null,
)

private val FEATURES = listOf(
    Feature(
        ErrorRed, "SOS", "এসওএস",
        "The red button in the middle of the bar. Hold it, and after a countdown the phone " +
            "sounds a siren and passes a signed call for help from phone to phone.",
        "নিচের বারের মাঝখানের লাল বোতাম। চেপে ধরুন — গণনা শেষে ফোন সাইরেন বাজায় এবং ফোন থেকে " +
            "ফোনে স্বাক্ষরিত সাহায্যের ডাক পাঠায়।",
        icon = FeatherIcons.AlertTriangle,
    ),
    Feature(
        BrandTeal, "Emergency Translation", "জরুরি অনুবাদ",
        "Talk to someone who does not speak Bangla — Chakma, Marma, Rohingya, Kokborok, " +
            "Santali, Garo or sign. Lay the phone flat between you; they answer by tapping.",
        "যিনি বাংলা বলেন না তাঁর সাথে কথা বলুন — চাকমা, মারমা, রোহিঙ্গা, ককবরক, সাঁওতালি, গারো " +
            "বা ইশারা। ফোনটি দুজনের মাঝে রাখুন; তিনি চাপ দিয়ে উত্তর দেবেন।",
        painter = R.drawable.ic_translate,
    ),
    Feature(
        AidRose, "First Aid", "প্রাথমিক চিকিৎসা",
        "Describe an injury and get clear first aid in Bangla or English, with the source " +
            "shown for every step.",
        "আঘাতের কথা বলুন, বাংলা বা ইংরেজিতে স্পষ্ট প্রাথমিক চিকিৎসা পান — প্রতিটি ধাপের উৎসসহ।",
        painter = R.drawable.ic_firstaid,
    ),
    Feature(
        ShelterGreen, "Safe Shelter", "নিরাপদ আশ্রয়",
        "The Map tab. Finds the nearest shelter on high ground and a walking route that avoids " +
            "the flood zone. Turn location on, or tap the map to place yourself.",
        "ম্যাপ ট্যাব। উঁচু জায়গার নিকটতম আশ্রয় ও বন্যা এড়িয়ে হেঁটে যাওয়ার পথ দেখায়। লোকেশন চালু " +
            "করুন, অথবা ম্যাপে চাপ দিয়ে নিজের জায়গা দিন।",
        icon = FeatherIcons.MapPin,
    ),
    Feature(
        Color(0xFF3C5A78), "Radar", "রাডার",
        "Which way and how far: your family, people calling for help, and volunteers offering " +
            "it — all placed around you by direction, with no map needed.",
        "কোন দিকে, কত দূরে: আপনার পরিবার, সাহায্য চাওয়া মানুষ আর সাহায্যকারী স্বয়ংসেবক — " +
            "ম্যাপ ছাড়াই দিক অনুযায়ী আপনার চারপাশে।",
        painter = R.drawable.ic_compass,
    ),
    Feature(
        Color(0xFF2F6BF0), "Offline AI", "অফলাইন এআই",
        "Gemma runs on this phone. Ask anything about the flood, your situation or what to do " +
            "next — no internet, and nothing you type leaves the handset.",
        "জেমা এই ফোনেই চলে। বন্যা, আপনার পরিস্থিতি বা পরের করণীয় নিয়ে যা খুশি জিজ্ঞাসা করুন — " +
            "ইন্টারনেট লাগে না, আপনার লেখা কিছুই ফোনের বাইরে যায় না।",
        painter = R.drawable.ic_gemma,
    ),
    Feature(
        SafeGreen, "Neighbourhood board", "পাড়ার বোর্ড",
        "The Alerts tab. Report what you can see — water rising, a road cut, a family stuck — " +
            "and read what neighbours have reported, all over the mesh.",
        "অ্যালার্ট ট্যাব। যা দেখছেন জানান — পানি বাড়ছে, রাস্তা কাটা, পরিবার আটকে আছে — এবং " +
            "প্রতিবেশীরা যা জানিয়েছেন তা পড়ুন, সবই মেশে।",
        icon = FeatherIcons.Users,
    ),
    Feature(
        CautionAmber, "Who needs help first", "আগে কার সাহায্য দরকার",
        "For volunteers. Ranks every case logged on this phone by urgency, so the worst goes " +
            "first when you cannot reach everyone.",
        "স্বয়ংসেবকদের জন্য। এই ফোনের সব কেস জরুরিতা অনুযায়ী সাজায়, যাতে সবার কাছে পৌঁছাতে না " +
            "পারলে সবচেয়ে খারাপটিতে আগে যাওয়া যায়।",
        icon = FeatherIcons.AlertTriangle,
    ),
    Feature(
        BriefBlue, "Situation briefing", "পরিস্থিতির সারসংক্ষেপ",
        "Turns every report on this phone into one short briefing: counts, the worst cases, " +
            "and where shelter is under pressure.",
        "এই ফোনের সব রিপোর্টকে একটি ছোট ব্রিফিংয়ে পরিণত করে: সংখ্যা, সবচেয়ে খারাপ কেস, আর " +
            "কোথায় আশ্রয়ের চাপ বেশি।",
        icon = FeatherIcons.BarChart2,
    ),
    Feature(
        Color(0xFF7A5B34), "SMS bridge", "এসএমএস সেতু",
        "No phone in range? Turn the SOS into one short SMS that works on any handset, even a " +
            "button phone, and can be pasted back into Nongor at the other end.",
        "কাছে কোনো ফোন নেই? এসওএসকে একটি ছোট এসএমএসে পরিণত করুন — যেকোনো ফোনে, এমনকি বাটন ফোনেও " +
            "চলে, আর অন্য প্রান্তে নোঙরে পেস্ট করা যায়।",
        icon = FeatherIcons.Send,
    ),
)

/** One row of the bottom bar, described the way it looks on screen. */
private data class TabNote(val label: String, val what: String, val tint: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(
    onBack: () -> Unit,
    actions: Actions = Actions({}, {}, {}, {}, {}),
    onSeedDemo: () -> Unit = {},
) {
    // The drill lives here rather than on the home screen. Home should be the shortest possible
    // path to a tool in an emergency; learning belongs behind the "?".
    var showDemo by remember { mutableStateOf(false) }
    if (showDemo) {
        DemoDialog(onDismiss = { showDemo = false }, onSeed = onSeedDemo, actions = actions)
    }

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
            // ---- Intro ----
            AnchorBadge(112.dp, Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(4.dp))
            Text(
                tr("Hi, I am Nongor", "আমি নোঙর"),
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                tr(
                    "Your offline flood helper. Everything — the maps, the first aid, the AI — " +
                        "already lives on this phone. No internet, no signal, no account.",
                    "আপনার অফলাইন বন্যা সহায়ক। সবকিছু — ম্যাপ, প্রাথমিক চিকিৎসা, এআই — আগে থেকেই " +
                        "এই ফোনে আছে। ইন্টারনেট, নেটওয়ার্ক বা অ্যাকাউন্ট কিছুই লাগে না।",
                ),
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )

            // ---- Drill launcher ----
            Spacer(Modifier.height(20.dp))
            Row(
                Modifier.fillMaxWidth().clip(ShapeMd)
                    .background(BrandTeal.copy(alpha = 0.10f))
                    .clickable { showDemo = true }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(36.dp).clip(CircleShape).background(BrandTeal),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(FeatherIcons.Play, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        tr("Try a flood drill", "একটি বন্যা মহড়া করুন"),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        tr("Practise with sample reports, before you need it", "প্রয়োজনের আগেই নমুনা রিপোর্ট দিয়ে অনুশীলন"),
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Icon(FeatherIcons.ChevronRight, null, tint = TextSecondary)
            }

            // ---- Getting around: the bar, described as it appears ----
            Spacer(Modifier.height(24.dp))
            SectionTitle(tr("Finding your way", "কোথায় কী আছে"))
            Spacer(Modifier.height(4.dp))
            Text(
                tr(
                    "Five things sit along the bottom of the screen, and they never move.",
                    "স্ক্রিনের নিচে পাঁচটি জিনিস থাকে, এবং সেগুলো কখনো সরে না।",
                ),
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(10.dp))
            listOf(
                TabNote(
                    tr("Home", "হোম"),
                    tr("Every tool, one tap away", "প্রতিটি টুল, এক চাপে"),
                    BrandTeal,
                ),
                TabNote(
                    tr("Map", "ম্যাপ"),
                    tr("Shelters and the safe way there", "আশ্রয় ও সেখানে যাওয়ার নিরাপদ পথ"),
                    BrandTealDeep,
                ),
                TabNote(
                    tr("SOS", "এসওএস"),
                    tr("The red circle in the middle — hold it to call for help", "মাঝের লাল বৃত্ত — সাহায্য চাইতে চেপে ধরুন"),
                    ErrorRed,
                ),
                TabNote(
                    tr("Alerts", "অ্যালার্ট"),
                    tr("What your neighbours are reporting", "প্রতিবেশীরা যা জানাচ্ছেন"),
                    SafeGreen,
                ),
                TabNote(
                    tr("Volunteer", "স্বয়ংসেবক"),
                    tr("For when you are the one helping", "আপনি যখন সাহায্যকারী"),
                    CautionAmber,
                ),
            ).forEach { TabRow(it) }

            // ---- The tools ----
            Spacer(Modifier.height(24.dp))
            SectionTitle(tr("What each tool does", "প্রতিটি টুল কী করে"))
            Spacer(Modifier.height(10.dp))
            FEATURES.forEach { f ->
                FeatureExplainer(f)
                Spacer(Modifier.height(8.dp))
            }

            // ---- Honesty about the limits ----
            Spacer(Modifier.height(16.dp))
            Note(
                title = tr("Worth knowing", "জেনে রাখা ভালো"),
                lines = listOf(
                    tr(
                        "Everything works in airplane mode. Nothing you type or photograph is uploaded anywhere.",
                        "সবকিছু এয়ারপ্লেন মোডেও চলে। আপনার লেখা বা তোলা ছবি কোথাও আপলোড হয় না।",
                    ),
                    tr(
                        "First aid answers are grounded in WHO and IFRC guidance, and every step shows its source.",
                        "প্রাথমিক চিকিৎসার উত্তর WHO ও IFRC নির্দেশনার ভিত্তিতে, প্রতিটি ধাপে উৎস দেখানো হয়।",
                    ),
                    tr(
                        "Phone-to-phone needs Bluetooth and Wi-Fi switched on, and a second phone within about 100 m.",
                        "ফোন থেকে ফোনে পাঠাতে ব্লুটুথ ও ওয়াই-ফাই চালু থাকতে হবে, আর প্রায় ১০০ মিটারের মধ্যে আরেকটি ফোন লাগবে।",
                    ),
                    tr(
                        "Translations marked \"unverified\" came from the AI, not a checked dictionary — read the person's face, not just the screen.",
                        "\"যাচাই করা হয়নি\" লেখা অনুবাদ এআই থেকে এসেছে, যাচাই করা অভিধান থেকে নয় — শুধু স্ক্রিন নয়, মানুষটির মুখও দেখুন।",
                    ),
                    tr(
                        "Nongor supports professional help. It does not replace it. Call 999 whenever you can.",
                        "নোঙর পেশাদার সাহায্যের পরিপূরক, বিকল্প নয়। সুযোগ পেলেই ৯৯৯-এ কল করুন।",
                    ),
                ),
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        color = TextPrimary,
        fontWeight = FontWeight.ExtraBold,
        style = MaterialTheme.typography.titleMedium,
    )
}

/** A bottom-bar destination, drawn as a coloured dot and a line — deliberately not a card. */
@Composable
private fun TabRow(note: TabNote) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(note.tint))
        Spacer(Modifier.width(12.dp))
        Text(
            note.label,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            note.what,
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun FeatureExplainer(f: Feature) {
    val bangla = LocalBangla.current
    Row(
        Modifier
            .fillMaxWidth()
            .clip(ShapeMd)
            .background(BgCard)
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            Modifier.size(44.dp).clip(CircleShape).background(f.tint.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center,
        ) {
            when {
                f.painter != null -> Icon(
                    painterResource(f.painter),
                    contentDescription = null,
                    tint = f.tint,
                    modifier = Modifier.size(22.dp),
                )
                f.icon != null -> Icon(f.icon, null, tint = f.tint, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                if (bangla) f.titleBn else f.titleEn,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                if (bangla) f.descBn else f.descEn,
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/**
 * The limits, stated plainly.
 *
 * Bulleted so it reads as a list of caveats rather than reassurance — an app used in a flood
 * should be clear about what it cannot do before someone relies on it.
 */
@Composable
private fun Note(title: String, lines: List<String>) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(ShapeMd)
            .background(BrandTeal.copy(alpha = 0.07f))
            .padding(16.dp),
    ) {
        Text(
            title,
            color = BrandTealDeep,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.titleSmall,
        )
        Spacer(Modifier.height(8.dp))
        lines.forEach {
            Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                Box(
                    Modifier.padding(top = 6.dp).size(5.dp).clip(CircleShape)
                        .background(BrandTeal),
                )
                Spacer(Modifier.width(10.dp))
                Text(it, color = TextPrimary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
