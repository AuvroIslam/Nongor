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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MeshScreen(viewModel: MeshViewModel, onBack: () -> Unit) {
    val ui by viewModel.ui.collectAsState()
    var text by remember { mutableStateOf("") }

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

            HeroBanner(R.drawable.hero_mesh,
                title = tr("Mesh SOS", "মেশ এসওএস"),
                subtitle = tr("Send SOS, no internet", "ইন্টারনেট ছাড়াই এসওএস পাঠান"))
            Spacer(Modifier.height(12.dp))

            Text("● ${ui.status}", style = MaterialTheme.typography.bodyMedium,
                color = if (ui.peers > 0) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${tr("This device", "এই ডিভাইস")}: ${viewModel.localName}  ·  " +
                "${tr("peers", "সংযুক্ত")}: ${ui.peers}",
                style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = text, onValueChange = { text = it },
                label = { Text(tr("SOS message", "এসওএস বার্তা")) },
                modifier = Modifier.fillMaxWidth(), maxLines = 3)
            Spacer(Modifier.height(6.dp))
            val rooftopSos = tr("Trapped on rooftop, water rising, need boat rescue.",
                "ছাদে আটকা, পানি বাড়ছে, নৌকায় উদ্ধার দরকার।")
            val medicalSos = tr("Elderly man, heavy bleeding, need medic urgently.",
                "বয়স্ক ব্যক্তি, প্রচুর রক্তক্ষরণ, দ্রুত চিকিৎসক দরকার।")
            Row {
                AssistChip(onClick = { text = rooftopSos },
                    label = { Text(tr("rooftop SOS", "ছাদ এসওএস")) })
                Spacer(Modifier.width(8.dp))
                AssistChip(onClick = { text = medicalSos },
                    label = { Text(tr("medical SOS", "চিকিৎসা এসওএস")) })
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { viewModel.send(text); text = "" },
                enabled = text.isNotBlank()) {
                Icon(FeatherIcons.Radio, null, modifier = Modifier.width(18.dp).height(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(tr("Broadcast SOS", "এসওএস সম্প্রচার"))
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
                Text(m.color)
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
