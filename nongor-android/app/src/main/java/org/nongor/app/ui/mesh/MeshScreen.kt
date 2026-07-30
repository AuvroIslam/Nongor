package org.nongor.app.ui.mesh

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.Radio
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.nongor.app.R
import org.nongor.app.ui.components.HeroBanner
import org.nongor.app.ui.i18n.LocalBangla
import org.nongor.app.ui.i18n.tr
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.nongor.app.ui.theme.TileMeshFg
import org.nongor.app.ui.components.PriorityDot
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.size
import compose.icons.feathericons.CheckCircle
import compose.icons.feathericons.Clock
import org.nongor.app.ui.theme.SafeGreen
import org.nongor.app.ui.theme.ShapeSm
import org.nongor.app.ui.theme.TextSecondary
import org.nongor.app.ui.i18n.localiseDigits
import org.nongor.app.mesh.MeshReadiness
import org.nongor.app.ui.components.MeshHealthBanner

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MeshScreen(viewModel: MeshViewModel, onBack: () -> Unit, prefill: String = "") {
    val ui by viewModel.ui.collectAsState()
    var text by remember { mutableStateOf(prefill) }
    var sheet by remember { mutableStateOf(MeshSheet.NONE) }

    // A note handed over from the translation screen is already written, so open straight
    // into the composer rather than making the volunteer hunt for where it went.
    LaunchedEffect(prefill) { if (prefill.isNotBlank()) sheet = MeshSheet.COMPOSE }

    // One list, shared with Radar and with the readiness check, so the three can never
    // disagree about what the radio actually needs.
    val permState = rememberMultiplePermissionsState(MeshReadiness.requiredPermissions())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Nongor · Mesh SOS", "নোঙর · মেশ এসওএস")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(FeatherIcons.ArrowLeft, contentDescription = "Back")
                    }
                },
            )
        },
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState())) {
            MeshHealthBanner()
            if (!permState.allPermissionsGranted) {
                Text(tr("Offline mesh needs Bluetooth, Nearby-Wi-Fi and location permissions to " +
                    "find nearby phones (no internet is used).",
                    "অফলাইন মেশে কাছের ফোন খুঁজতে ব্লুটুথ, নিয়ারবাই-ওয়াইফাই ও লোকেশন অনুমতি লাগে " +
                        "(কোনো ইন্টারনেট ব্যবহার হয় না)।"),
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { permState.launchMultiplePermissionRequest() }) {
                    Text(tr("Grant mesh permissions", "মেশ অনুমতি দিন"))
                }
                return@Column
            }

            LaunchedEffect(Unit) { viewModel.start() }
            DisposableEffect(Unit) { onDispose { viewModel.stop() } }

            HeroBanner(FeatherIcons.Radio, tint = TileMeshFg,
                title = tr("Mesh SOS", "মেশ এসওএস"),
                subtitle = tr("Send SOS, no internet", "ইন্টারনেট ছাড়াই এসওএস পাঠান"))
            Spacer(Modifier.height(12.dp))

            Text("● ${ui.status}", style = MaterialTheme.typography.bodyMedium,
                color = if (ui.peers > 0) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${tr("This device", "এই ডিভাইস")}: ${viewModel.localName}  ·  " +
                "${tr("peers", "সংযুক্ত")}: ${ui.peers}",
                style = MaterialTheme.typography.bodySmall)

            // ---- the one-press panic control, above everything that asks you to type ----
            Spacer(Modifier.height(16.dp))
            SosButton(
                active = ui.sosActive,
                sirenOn = ui.sirenOn,
                peers = ui.peers,
                onPress = { viewModel.pressSos(text) },
                onStop = { viewModel.stopSos() },
                onToggleSiren = { viewModel.toggleSiren() },
            )

            // Everything below the big button is a *considered* action, not a panic one, so
            // it lives behind a sheet. Two calm choices beat eight controls on one screen.
            Spacer(Modifier.height(22.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { sheet = MeshSheet.COMPOSE }, modifier = Modifier.weight(1f)) {
                    Text(tr("Write details", "বিস্তারিত লিখুন"))
                }
                OutlinedButton(onClick = { sheet = MeshSheet.SMS }, modifier = Modifier.weight(1f)) {
                    Text(tr("Send by SMS", "এসএমএসে পাঠান"))
                }
            }
            if (text.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    tr("Your message: $text", "আপনার বার্তা: $text"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // ---- sheets ----
            when (sheet) {
                MeshSheet.COMPOSE -> ComposeSosSheet(
                    text = text,
                    onTextChange = { text = it },
                    onBroadcast = { viewModel.send(text); text = ""; sheet = MeshSheet.NONE },
                    onDismiss = { sheet = MeshSheet.NONE },
                )
                MeshSheet.SMS -> SmsSheet(
                    viewModel = viewModel,
                    sosText = text,
                    onDismiss = { sheet = MeshSheet.NONE },
                )
                MeshSheet.NONE -> Unit
            }

            Spacer(Modifier.height(12.dp))
            Text("${tr("Messages", "বার্তা")} (${ui.messages.size})",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ui.messages.reversed().forEach { m ->
                    MeshRow(m, ui.seenBy[m.msgId].orEmpty(), onSeen = { viewModel.markSeen(it) })
                }
            }
        }
    }
}

/**
 * One message on the mesh log, and its read receipt.
 *
 * Messages you sent show who has confirmed seeing them. Messages from other people send that
 * confirmation, once, when this row is first composed - the receipt is only worth anything if
 * it means a human looked at the screen, so it fires on display rather than on delivery.
 */
@Composable
private fun MeshRow(
    m: org.nongor.app.mesh.MeshMsg,
    seenBy: Set<String>,
    onSeen: (String) -> Unit,
) {
    if (!m.mine && m.verified && m.msgId.isNotBlank()) {
        LaunchedEffect(m.msgId) { onSeen(m.msgId) }
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PriorityDot(m.priority)
                Spacer(Modifier.width(6.dp))
                Text(m.priority.uppercase(), fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (m.mine) "you → broadcast"
                    else "from ${m.sender}" + (if (m.hops > 0) " · ${m.hops} hop(s)" else ""),
                    style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.width(8.dp))
                if (!m.mine) {
                    Text(if (m.verified) "✓ signed" else "✗ unverified",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (m.verified) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(m.text, style = MaterialTheme.typography.bodyMedium)

            if (m.mine) {
                Spacer(Modifier.height(10.dp))
                SeenReceipt(seenBy)
            }
        }
    }
}

/**
 * "Seen by N phones" - and, immediately, what that does not mean.
 *
 * The caveat is not boilerplate. Someone on a roof who reads "seen by 3" and takes it as
 * "three people are coming" may stop shouting, stop looking for another way out, and wait.
 * The receipt is real and worth having: it is the difference between shouting into the dark
 * and knowing the message left the building. But it is a delivery confirmation, not a rescue,
 * and the screen has to say which one it is in the same breath.
 */
@Composable
private fun SeenReceipt(seenBy: Set<String>) {
    val n = seenBy.size
    val bangla = LocalBangla.current
    Row(
        Modifier
            .fillMaxWidth()
            .clip(ShapeSm)
            .background(
                if (n == 0) TextSecondary.copy(alpha = 0.08f) else SafeGreen.copy(alpha = 0.12f),
            )
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            if (n == 0) FeatherIcons.Clock else FeatherIcons.CheckCircle,
            contentDescription = null,
            tint = if (n == 0) TextSecondary else SafeGreen,
            modifier = Modifier.size(15.dp).padding(top = 1.dp),
        )
        Spacer(Modifier.width(9.dp))
        Column {
            Text(
                if (n == 0) {
                    tr("Not seen yet", "এখনও কেউ দেখেনি")
                } else {
                    localiseDigits(
                        if (n == 1) tr("Seen on 1 phone", "১টি ফোনে দেখা হয়েছে")
                        else tr("Seen on ${n} phones", "${n}টি ফোনে দেখা হয়েছে"),
                        bangla,
                    )
                },
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge,
                color = if (n == 0) TextSecondary else SafeGreen,
            )
            Text(
                if (n == 0) {
                    tr(
                        "It keeps going out until a phone picks it up.",
                        "কোনো ফোন না পাওয়া পর্যন্ত এটি যেতেই থাকবে।",
                    )
                } else {
                    tr(
                        "Your message got through. That is not a promise that help is on the way — keep trying other ways too.",
                        "আপনার বার্তা পৌঁছেছে। এর মানে এই নয় যে সাহায্য আসছে — অন্য ভাবেও চেষ্টা চালিয়ে যান।",
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}
