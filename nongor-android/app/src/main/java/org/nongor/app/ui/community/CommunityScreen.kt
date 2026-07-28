package org.nongor.app.ui.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.CircularProgressIndicator
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
import compose.icons.feathericons.Radio
import org.nongor.app.data.CommunityKinds
import org.nongor.app.data.CommunityReport
import org.nongor.app.ui.i18n.LocalBangla
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.ShapeSm
import org.nongor.app.ui.theme.CautionAmber
import org.nongor.app.ui.theme.SafeGreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CommunityScreen(viewModel: CommunityViewModel, onBack: () -> Unit) {
    val ui by viewModel.ui.collectAsState()
    DisposableEffect(Unit) {
        viewModel.enter()
        onDispose { viewModel.leave() }
    }
    var selectedKind by remember { mutableStateOf(CommunityKinds.ALL.first().id) }
    var note by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Nongor · Community Board", "নোঙর · কমিউনিটি বোর্ড")) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(FeatherIcons.ArrowLeft, contentDescription = "Back") }
                },
            )
        },
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
        ) {
            // ---- mesh status ----
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(CircleShape)
                    .background(if (ui.started) SafeGreen else CautionAmber))
                Spacer(Modifier.width(8.dp))
                Text(
                    (if (ui.started) tr("Sharing over mesh · ${ui.peers} peer(s)",
                        "মেশে শেয়ার হচ্ছে · ${ui.peers} পিয়ার")
                    else tr("Starting mesh…", "মেশ চালু হচ্ছে…")) +
                        (if (ui.district.isNotBlank()) " · ${ui.district}" else ""),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            Text(tr("Every phone is a sensor. Tag what you see — it spreads to nearby phones with no internet.",
                "প্রতিটি ফোন একটি সেন্সর। যা দেখছেন তা চিহ্নিত করুন — ইন্টারনেট ছাড়াই কাছের ফোনে ছড়িয়ে পড়ে।"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp))

            // ---- post a report ----
            Spacer(Modifier.height(14.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(tr("Report what you see", "যা দেখছেন জানান"),
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CommunityKinds.ALL.forEach { k ->
                            KindChip(
                                label = "${k.emoji} " + (if (LocalBangla.current) k.bn else k.en),
                                selected = k.id == selectedKind,
                                danger = k.danger,
                                onClick = { selectedKind = k.id },
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = note, onValueChange = { note = it }, modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(tr("Add a detail (optional) — e.g. which road",
                            "বিস্তারিত দিন (ঐচ্ছিক) — যেমন কোন রাস্তা")) },
                        singleLine = true,
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.post(selectedKind, note); note = "" },
                        modifier = Modifier.fillMaxWidth(), enabled = ui.started,
                    ) {
                        Icon(FeatherIcons.Radio, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(tr("Share report", "রিপোর্ট শেয়ার করুন"))
                    }
                }
            }

            // ---- Gemma situation briefing ----
            Spacer(Modifier.height(14.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(tr("Situation briefing", "পরিস্থিতি ব্রিফিং"),
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(tr("Counts are computed on-device; Gemma writes the briefing from the reports.",
                        "সংখ্যা ডিভাইসেই গণনা হয়; Gemma রিপোর্ট থেকে ব্রিফিং লেখে।"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.summarize() },
                        enabled = !ui.summaryBusy && ui.reports.isNotEmpty()) {
                        Text(tr("Generate situation briefing", "পরিস্থিতি ব্রিফিং তৈরি করুন"))
                    }
                    if (ui.summaryBusy) {
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(tr("Gemma is working…", "Gemma কাজ করছে…"),
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    ui.summary?.let {
                        Spacer(Modifier.height(10.dp))
                        Card(colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Column(Modifier.padding(12.dp)) {
                                Text("✦ Gemma", style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(4.dp))
                                Text(it, style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }
            }

            // ---- the shared board ----
            Spacer(Modifier.height(16.dp))
            Text(tr("Area board (${ui.reports.size})", "এলাকা বোর্ড (${ui.reports.size})"),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (ui.reports.isEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(tr("No reports yet. Share one above, or wait for nearby phones.",
                    "এখনো কোনো রিপোর্ট নেই। উপরে একটি শেয়ার করুন, বা কাছের ফোনের জন্য অপেক্ষা করুন।"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            ui.reports.forEach { ReportRow(it) }

            // Reports from other districts — kept separate so they don't flood this area's board,
            // but still visible so nothing a neighbour shared is silently lost.
            if (ui.otherAreas.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(tr("Other areas (${ui.otherAreas.size})", "অন্যান্য এলাকা (${ui.otherAreas.size})"),
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                ui.otherAreas.forEach { r ->
                    Text("• ${CommunityKinds.byId(r.kind).emoji} " +
                        (if (LocalBangla.current) CommunityKinds.byId(r.kind).bn else CommunityKinds.byId(r.kind).en) +
                        (if (r.districtEn.isNotBlank()) " — ${r.districtEn}" else "") +
                        (if (r.note.isNotBlank()) ": ${r.note}" else ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 2.dp))
                }
            }
            if (ui.quarantined > 0) {
                Spacer(Modifier.height(8.dp))
                Text(tr("${ui.quarantined} unverified report(s) quarantined (failed signature).",
                    "${ui.quarantined}টি অযাচাইকৃত রিপোর্ট আলাদা রাখা হয়েছে (স্বাক্ষর ব্যর্থ)।"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun KindChip(label: String, selected: Boolean, danger: Boolean, onClick: () -> Unit) {
    val base = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Box(
        Modifier.clip(ShapeSm)
            .background(if (selected) base.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge,
            color = if (selected) base else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun ReportRow(r: CommunityReport) {
    val k = CommunityKinds.byId(r.kind)
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(k.emoji, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(if (LocalBangla.current) k.bn else k.en, fontWeight = FontWeight.Bold,
                    color = if (k.danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                if (r.note.isNotBlank()) Text(r.note, style = MaterialTheme.typography.bodyMedium)
                Text(
                    (if (r.mine) tr("You", "আপনি") else r.sender) +
                        (if (r.verified) tr(" · ✓ signed", " · ✓ স্বাক্ষরিত") else tr(" · unverified", " · অযাচাইকৃত")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
