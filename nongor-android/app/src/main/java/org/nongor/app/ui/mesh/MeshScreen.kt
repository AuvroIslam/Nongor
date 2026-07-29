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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MeshScreen(viewModel: MeshViewModel, onBack: () -> Unit, prefill: String = "") {
    val ui by viewModel.ui.collectAsState()
    var text by remember { mutableStateOf(prefill) }
    var sheet by remember { mutableStateOf(MeshSheet.NONE) }

    // A note handed over from the translation screen is already written, so open straight
    // into the composer rather than making the volunteer hunt for where it went.
    LaunchedEffect(prefill) { if (prefill.isNotBlank()) sheet = MeshSheet.COMPOSE }

    val permissions = remember {
        buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
        }
    }
    val permState = rememberMultiplePermissionsState(permissions)

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
                ui.messages.reversed().forEach { m -> MeshRow(m) }
            }
        }
    }
}

@Composable
private fun MeshRow(m: org.nongor.app.mesh.MeshMsg) {
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
        }
    }
}
