package org.nongor.app.ui.demo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import compose.icons.FeatherIcons
import compose.icons.feathericons.Activity
import compose.icons.feathericons.AlertTriangle
import compose.icons.feathericons.BarChart2
import compose.icons.feathericons.MapPin
import compose.icons.feathericons.MessageSquare
import compose.icons.feathericons.Radio
import compose.icons.feathericons.X
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.nongor.app.R
import org.nongor.app.ui.i18n.LocalBangla
import org.nongor.app.ui.theme.BrandBlue
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

private data class DemoStep(
    val icon: ImageVector?,
    val bg: Color,
    val fg: Color,
    val titleEn: String,
    val titleBn: String,
    val bodyEn: String,
    val bodyBn: String,
    val actionEn: String?,
    val actionBn: String?,
    val open: (Actions) -> Unit,
)

/** Navigation hooks the drill uses to hand the user off to a real tool. */
class Actions(
    val triage: () -> Unit,
    val firstAid: () -> Unit,
    val shelter: () -> Unit,
    val mesh: () -> Unit,
    val summary: () -> Unit,
    val translate: () -> Unit = {},
)

private val STEPS = listOf(
    DemoStep(
        null, TileSummaryBg, TileSummaryFg,
        "Flood drill", "বন্যা মহড়া",
        "Monsoon water is rising fast in your area. I'll walk you through responding, step by step, " +
            "so you can try every tool before a real emergency. A few sample reports are added so " +
            "each screen has something to work with. Nothing is sent anywhere.",
        "আপনার এলাকায় বর্ষার পানি দ্রুত বাড়ছে। আমি ধাপে ধাপে সাড়া দেওয়ার পথ দেখাব, যাতে সত্যিকারের " +
            "বিপদের আগে আপনি প্রতিটি টুল দেখে নিতে পারেন। কয়েকটি নমুনা রিপোর্ট যোগ করা হয়েছে যাতে প্রতিটি " +
            "স্ক্রিনে কিছু কাজ থাকে। কোনো কিছুই কোথাও পাঠানো হয় না।",
        null, null, {},
    ),
    DemoStep(
        FeatherIcons.AlertTriangle, TileTriageBg, TileTriageFg,
        "1 · Rescue Triage", "১ · উদ্ধার ট্রায়াজ",
        "A neighbour is trapped on a rooftop as water rises. Open Triage, describe it (or add a photo), " +
            "and Gemma ranks how urgent it is so the worst cases get help first.",
        "একজন প্রতিবেশী ছাদে আটকা পড়েছে, পানি বাড়ছে। ট্রায়াজ খুলে লিখুন (বা ছবি দিন), Gemma কতটা জরুরি " +
            "তা সাজিয়ে দেবে যাতে সবচেয়ে খারাপ অবস্থায় আগে সাহায্য যায়।",
        "Open Rescue Triage", "উদ্ধার ট্রায়াজ খুলুন", { it.triage() },
    ),
    DemoStep(
        FeatherIcons.Activity, TileAidBg, TileAidFg,
        "2 · First Aid", "২ · প্রাথমিক চিকিৎসা",
        "Someone swallowed floodwater and keeps coughing. Ask First Aid — you get calm, cited steps " +
            "grounded in WHO and IFRC guidance, fully offline.",
        "কেউ বন্যার পানি গিলে ফেলেছে এবং কাশছে। প্রাথমিক চিকিৎসায় জিজ্ঞেস করুন — WHO ও IFRC নির্দেশনার " +
            "ভিত্তিতে শান্ত, উৎসসহ ধাপ পাবেন, সম্পূর্ণ অফলাইনে।",
        "Open First Aid", "প্রাথমিক চিকিৎসা খুলুন", { it.firstAid() },
    ),
    DemoStep(
        FeatherIcons.MapPin, TileShelterBg, TileShelterFg,
        "3 · Safe Shelter", "৩ · নিরাপদ আশ্রয়",
        "You need to move. Safe Shelter finds the nearest shelter on high ground and draws a walking " +
            "route that avoids flooded roads, using your GPS.",
        "আপনাকে সরতে হবে। নিরাপদ আশ্রয় আপনার জিপিএস দিয়ে উঁচু জায়গার নিকটতম আশ্রয়কেন্দ্র খুঁজে বন্যা " +
            "এড়িয়ে হাঁটার পথ আঁকে।",
        "Open Safe Shelter", "নিরাপদ আশ্রয় খুলুন", { it.shelter() },
    ),
    DemoStep(
        FeatherIcons.Radio, TileMeshBg, TileMeshFg,
        "4 · Mesh SOS", "৪ · মেশ এসওএস",
        "The network is down. Mesh SOS sends a signed SOS phone-to-phone over Bluetooth and Wi-Fi, and " +
            "nearby phones pass it onward. Try it with a second phone close by.",
        "নেটওয়ার্ক বন্ধ। মেশ এসওএস ব্লুটুথ ও ওয়াই-ফাই দিয়ে ফোন থেকে ফোনে স্বাক্ষরিত এসওএস পাঠায়, কাছের " +
            "ফোনগুলো তা এগিয়ে দেয়। পাশে আরেকটি ফোন রেখে চেষ্টা করুন।",
        "Open Mesh SOS", "মেশ এসওএস খুলুন", { it.mesh() },
    ),
    DemoStep(
        FeatherIcons.MessageSquare, TileChatBg, TileChatFg,
        "5 · Emergency Translation", "৫ · জরুরি অনুবাদ",
        "The family you reached speaks Chakma, not Bangla. Open Translation, pick their language, and " +
            "lay the phone flat between you — they read their half upside down and answer by tapping. " +
            "Ten guided questions build a hand-over note you can send straight over the mesh.",
        "যে পরিবারটির কাছে পৌঁছেছেন তারা বাংলা নয়, চাকমা বলে। অনুবাদ খুলে তাদের ভাষা বেছে নিন, তারপর " +
            "ফোনটি দুজনের মাঝে সমতলে রাখুন — তারা উল্টো দিক থেকে পড়ে আঙুলের ছোঁয়ায় উত্তর দেবেন। দশটি " +
            "প্রশ্নে একটি হস্তান্তর নোট তৈরি হয়, যা সরাসরি মেশে পাঠানো যায়।",
        "Open Translation", "অনুবাদ খুলুন", { it.translate() },
    ),
    DemoStep(
        FeatherIcons.BarChart2, TileSummaryBg, TileSummaryFg,
        "6 · Coordinator Summary", "৬ · সমন্বয়কারী সারাংশ",
        "Finally, see the whole picture. The Summary counts every report on this device and Gemma " +
            "writes a briefing — totals, top cases and shortages — never invented numbers.",
        "শেষে, পুরো চিত্র দেখুন। সারাংশ এই ডিভাইসের প্রতিটি রিপোর্ট গণনা করে এবং Gemma একটি ব্রিফিং লেখে — " +
            "মোট, শীর্ষ কেস ও ঘাটতি — কোনো বানানো সংখ্যা নয়।",
        "Open Summary", "সারাংশ খুলুন", { it.summary() },
    ),
)

@Composable
fun DemoDialog(
    onDismiss: () -> Unit,
    onSeed: () -> Unit,
    actions: Actions,
) {
    val bangla = LocalBangla.current
    var step by remember { mutableIntStateOf(0) }
    val s = STEPS[step]
    val last = step == STEPS.lastIndex

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.fillMaxWidth(0.92f).clip(RoundedCornerShape(24.dp)).background(BgCard)
                .padding(20.dp),
        ) {
            // Header row: progress dots + close
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    STEPS.indices.forEach { i ->
                        Box(
                            Modifier.height(6.dp).width(if (i == step) 20.dp else 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (i == step) BrandBlue else BrandBlue.copy(alpha = 0.2f)),
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(30.dp).clip(CircleShape).clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center) {
                    Icon(FeatherIcons.X, contentDescription = "Close", tint = TextSecondary,
                        modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
            // Illustration: mascot on intro, tool icon otherwise
            if (s.icon == null) {
                Image(painterResource(R.drawable.mascot_1), null,
                    modifier = Modifier.size(120.dp).align(Alignment.CenterHorizontally))
            } else {
                Box(Modifier.size(72.dp).clip(CircleShape).background(s.bg)
                    .align(Alignment.CenterHorizontally), contentAlignment = Alignment.Center) {
                    Icon(s.icon, null, tint = s.fg, modifier = Modifier.size(34.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(if (bangla) s.titleBn else s.titleEn, color = TextPrimary,
                fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(if (bangla) s.bodyBn else s.bodyEn, color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium)

            // Primary action for the step (open a tool)
            if (s.actionEn != null) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { s.open(actions); onDismiss() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = s.fg),
                ) {
                    s.icon?.let {
                        Icon(it, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                    }
                    Text(if (bangla) s.actionBn!! else s.actionEn)
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (step > 0) {
                    TextButton(onClick = { step-- }) {
                        Text(if (bangla) "পিছনে" else "Back", color = TextSecondary)
                    }
                }
                Spacer(Modifier.weight(1f))
                Button(onClick = {
                    if (step == 0) onSeed()
                    if (last) onDismiss() else step++
                }) {
                    Text(
                        when {
                            step == 0 -> if (bangla) "মহড়া শুরু করুন" else "Start drill"
                            last -> if (bangla) "শেষ" else "Finish"
                            else -> if (bangla) "পরবর্তী" else "Next"
                        },
                    )
                }
            }
        }
    }
}
