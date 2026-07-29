package org.nongor.app.ui.family

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.Users
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.BrandTeal
import org.nongor.app.ui.theme.CautionAmber
import org.nongor.app.ui.theme.SafeGreen
import org.nongor.app.ui.theme.ShapePill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen(viewModel: FamilyViewModel, onBack: () -> Unit) {
    val ui by viewModel.ui.collectAsState()
    // Tapping someone on the radar highlights their row below, so the picture and the
    // detail stay connected rather than being two separate things to read.
    var selected by remember { mutableStateOf<String?>(null) }
    DisposableEffect(Unit) {
        viewModel.enter()
        onDispose { viewModel.leave() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Nongor · Family Reunion", "নোঙর · পরিবার পুনর্মিলন")) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(FeatherIcons.ArrowLeft, contentDescription = "Back") }
                },
            )
        },
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
        ) {
            if (!ui.configured) {
                SetupCard(onSave = { code, name -> viewModel.saveFamily(code, name) })
                return@Column
            }

            // ---- status ----
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(CircleShape)
                    .background(if (ui.started) SafeGreen else CautionAmber))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (ui.started) tr("Listening for family · ${ui.peers} phone(s) in range",
                        "পরিবারের জন্য শুনছে · ${ui.peers} ফোন কাছে")
                    else tr("Starting…", "চালু হচ্ছে…"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary)
            }
            Text(tr("Your phone quietly announces itself to your family only — the family ID is hashed " +
                "and your name is encrypted, so strangers in range learn nothing. No internet or cell " +
                "network needed.",
                "আপনার ফোন শুধু আপনার পরিবারের কাছে নিজেকে জানায় — পরিবার আইডি হ্যাশ করা ও নাম এনক্রিপ্টেড, " +
                    "তাই কাছের অপরিচিতরা কিছুই জানতে পারে না। ইন্টারনেট বা মোবাইল নেটওয়ার্ক লাগে না।"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp))

            // ---- where they are ----
            // The picture comes first. In an evacuation the question is "which way do I
            // walk", and a compass answers that faster than a list of rows can.
            Spacer(Modifier.height(16.dp))
            FamilyRadar(
                members = ui.members,
                listening = ui.started,
                onSelect = { selected = it.member.name },
            )

            Spacer(Modifier.height(18.dp))
            Text(tr("Your family (${ui.members.size} seen)", "আপনার পরিবার (${ui.members.size} জন দেখা গেছে)"),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (ui.togetherCount >= 2) {
                Text(tr("${ui.togetherCount} family phones were last seen together.",
                    "${ui.togetherCount} জন পরিবারের ফোন একসাথে দেখা গিয়েছিল।"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary)
            }
            if (ui.members.isEmpty()) {
                Spacer(Modifier.height(8.dp))
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(tr("No family phone heard yet.", "এখনো পরিবারের কোনো ফোন পাওয়া যায়নি।"),
                            fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(tr("Ask them to open Family Reunion with the same family code. When your " +
                            "phones come within range — even once, even walking past — the meeting is recorded here.",
                            "তাদের একই পরিবার কোড দিয়ে পরিবার পুনর্মিলন খুলতে বলুন। আপনাদের ফোন একবার কাছাকাছি " +
                                "এলেই — এমনকি পাশ দিয়ে হেঁটে গেলেও — সেই সাক্ষাৎ এখানে জমা হবে।"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            ui.members.forEach { MemberRow(it, highlighted = it.member.name == selected) }

            // ---- family settings ----
            Spacer(Modifier.height(20.dp))
            Card(Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(14.dp)) {
                    Text(tr("You are “${ui.myName}” in this family.", "এই পরিবারে আপনি “${ui.myName}”।"),
                        style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { viewModel.forgetFamily() }) {
                        Text(tr("Leave / change family", "পরিবার ছাড়ুন / বদলান"),
                            color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupCard(onSave: (String, String) -> Unit) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(BrandTeal),
                contentAlignment = Alignment.Center) {
                Icon(FeatherIcons.Users, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(tr("Set up Family Reunion", "পরিবার পুনর্মিলন সেট করুন"),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Text(tr("Agree one secret family code with your relatives and enter it on every family phone " +
            "— ideally before a flood. Phones then recognise each other over the mesh, with no internet.",
            "আপনার আত্মীয়দের সাথে একটি গোপন পরিবার কোড ঠিক করুন এবং পরিবারের প্রতিটি ফোনে দিন — " +
                "সম্ভব হলে বন্যার আগেই। এরপর ফোনগুলো ইন্টারনেট ছাড়াই মেশে একে অপরকে চিনবে।"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(value = code, onValueChange = { code = it }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(tr("Family code (secret)", "পরিবার কোড (গোপন)")) })
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(tr("Your name in the family", "পরিবারে আপনার নাম")) })
        Spacer(Modifier.height(14.dp))
        Button(onClick = { onSave(code, name) },
            enabled = code.isNotBlank() && name.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text(tr("Save & start", "সেভ করে শুরু করুন"))
        }
    }
}

@Composable
private fun MemberRow(s: SeenMember, highlighted: Boolean = false) {
    Card(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlighted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            // The same initial as the radar blip, so a person tapped on the circle is
            // recognisable in the list without reading.
            Box(Modifier.size(40.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center) {
                Text(
                    s.member.name.trim().take(1).uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(s.member.name, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall)
                    // Practice entries say so, every time they are shown. Nobody should ever
                    // go looking for a relative who only existed in a drill.
                    if (s.member.isDrill) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            Modifier.clip(ShapePill).background(CautionAmber.copy(alpha = 0.18f))
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                        ) {
                            Text(tr("practice", "মহড়া"), color = CautionAmber,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(whereLine(s), style = MaterialTheme.typography.bodyMedium)
                Text(agoLine(s), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun whereLine(s: SeenMember): String {
    val d = s.distanceM
    return if (d != null && s.direction != null) {
        val dist = if (d >= 1000) "%.1f km".format(d / 1000.0) else "$d m"
        tr("Last detected ~$dist ${s.direction}", "সর্বশেষ প্রায় $dist ${s.direction} দিকে")
    } else {
        tr("Heard nearby over the mesh (${s.member.hops} hop)",
            "মেশে কাছেই পাওয়া গেছে (${s.member.hops} হপ)")
    }
}

@Composable
private fun agoLine(s: SeenMember): String = when {
    s.minutesAgo < 1L -> tr("just now", "এইমাত্র")
    s.minutesAgo < 60L -> tr("${s.minutesAgo} min ago", "${s.minutesAgo} মিনিট আগে")
    else -> tr("${s.minutesAgo / 60} h ago", "${s.minutesAgo / 60} ঘণ্টা আগে")
}
