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
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.nongor.app.mesh.MeshReadiness
import org.nongor.app.ui.components.MeshHealthBanner
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.AlertDialog
import compose.icons.feathericons.Trash2

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun FamilyScreen(viewModel: FamilyViewModel, onBack: () -> Unit) {
    val ui by viewModel.ui.collectAsState()
    // Tapping someone on the radar highlights their row below, so the picture and the
    // detail stay connected rather than being two separate things to read.
    var selected by remember { mutableStateOf<String?>(null) }
    var confirmClear by remember { mutableStateOf(false) }
    // Radar rides the same radio as Mesh SOS but never asked for its permissions. Open the
    // app on this screen first, on a fresh install, and it would advertise into nothing and
    // report "0 phones in range" forever without ever prompting.
    val permState = rememberMultiplePermissionsState(MeshReadiness.requiredPermissions())
    LaunchedEffect(Unit) {
        if (!permState.allPermissionsGranted) permState.launchMultiplePermissionRequest()
    }
    DisposableEffect(Unit) {
        viewModel.enter()
        onDispose { viewModel.leave() }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(tr("Clear the radar?", "রাডার খালি করবেন?")) },
            text = {
                Text(
                    tr(
                        "Removes the family sightings logged on this phone and any practice data. " +
                            "Real SOS reports are kept — the triage queue and the briefing are " +
                            "counted from those.",
                        "এই ফোনে জমা পরিবারের সাক্ষাৎ ও অনুশীলনের তথ্য মুছে যাবে। প্রকৃত এসওএস " +
                            "রিপোর্ট থাকবে — ট্রায়াজ তালিকা ও ব্রিফিং সেগুলো থেকেই গোনা হয়।",
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearRadar(); confirmClear = false }) {
                    Text(tr("Clear", "খালি করুন"), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) {
                    Text(tr("Cancel", "বাতিল"))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Radar", "রাডার")) },
                actions = {
                    IconButton(onClick = { confirmClear = true }) {
                        Icon(
                            FeatherIcons.Trash2,
                            contentDescription = tr("Clear the radar", "রাডার খালি করুন"),
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(FeatherIcons.ArrowLeft, contentDescription = "Back") }
                },
            )
        },
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
        ) {
            MeshHealthBanner()

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
                sosBlips = ui.sosBlips,
                volunteerBlips = ui.volunteerBlips,
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
        // Digits only, and at least four of them. The code has to be agreed out loud between
        // relatives and typed identically on several phones, often by someone who is not
        // confident with a keyboard — letters invite case and spelling mismatches that silently
        // put two family members in two different families. A number keypad removes that whole
        // class of failure, and four digits is the shortest length that is not guessable by a
        // stranger in range who is trying codes.
        val digitsOnly = code.all { it.isDigit() }
        val longEnough = code.length >= FAMILY_CODE_MIN
        OutlinedTextField(
            value = code,
            onValueChange = { input -> code = input.filter { it.isDigit() }.take(FAMILY_CODE_MAX) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            isError = code.isNotEmpty() && !longEnough,
            label = { Text(tr("Family code (numbers only)", "পরিবার কোড (শুধু সংখ্যা)")) },
            supportingText = {
                Text(
                    if (code.isNotEmpty() && !longEnough) {
                        tr(
                            "At least $FAMILY_CODE_MIN digits.",
                            "কমপক্ষে $FAMILY_CODE_MIN সংখ্যা দিন।",
                        )
                    } else {
                        tr(
                            "$FAMILY_CODE_MIN digits or more. Everyone in the family types the same one.",
                            "$FAMILY_CODE_MIN বা তার বেশি সংখ্যা। পরিবারের সবাই একই কোড দেবেন।",
                        )
                    },
                )
            },
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(tr("Your name in the family", "পরিবারে আপনার নাম")) })
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = { onSave(code, name.trim()) },
            enabled = longEnough && digitsOnly && name.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(tr("Save & start", "সেভ করে শুরু করুন"))
        }
    }
}

/**
 * Four digits is the floor.
 *
 * Short enough to say down a corridor and remember without writing it down; long enough that
 * someone within radio range cannot work through the space by hand. The cap keeps it typable
 * on a feature-phone keypad.
 */
private const val FAMILY_CODE_MIN = 4
private const val FAMILY_CODE_MAX = 12

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
