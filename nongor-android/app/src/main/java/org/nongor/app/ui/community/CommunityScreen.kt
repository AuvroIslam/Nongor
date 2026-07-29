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
import androidx.compose.material3.FloatingActionButton
import compose.icons.feathericons.Plus
import org.nongor.app.ui.theme.BrandTeal
import compose.icons.feathericons.Shield
import compose.icons.feathericons.AlertCircle
import org.nongor.app.ui.theme.ShapeMd

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CommunityScreen(viewModel: CommunityViewModel, onBack: () -> Unit) {
    val ui by viewModel.ui.collectAsState()
    DisposableEffect(Unit) {
        viewModel.enter()
        onDispose { viewModel.leave() }
    }
    var reporting by remember { mutableStateOf(false) }

    if (reporting) {
        ReportSheet(
            canPost = ui.started,
            onPost = { kind, note -> viewModel.post(kind, note); reporting = false },
            onDismiss = { reporting = false },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Nongor · Community Board", "নোঙর · কমিউনিটি বোর্ড")) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(FeatherIcons.ArrowLeft, contentDescription = "Back") }
                },
            )
        },
        floatingActionButton = {
            // The one thing you come here to *do*. Everything else on this screen is reading.
            FloatingActionButton(
                onClick = { reporting = true },
                containerColor = BrandTeal,
                contentColor = Color.White,
                shape = CircleShape,
            ) {
                Icon(FeatherIcons.Plus, contentDescription = tr("Report", "জানান"))
            }
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
                Text(tr("No reports yet. Tap + to share one, or wait for nearby phones.",
                    "এখনো কোনো রিপোর্ট নেই। + চাপ দিয়ে জানান, বা কাছের ফোনের অপেক্ষা করুন।"),
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
                    Text("• " +
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

/**
 * One report on the board.
 *
 * Laid out the way a timetable is: a small muted line of context on top, then the thing
 * itself in bold. The whole card is tinted by whether this is a warning or an offer of help,
 * so the board can be understood by colour alone before a single word is read.
 */
@Composable
private fun ReportRow(r: CommunityReport) {
    val kind = CommunityKinds.byId(r.kind)
    val style = kindStyle(r.kind)
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(ShapeMd)
            .background(style.bg)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        // Context line: when, who, and whether the signature checked out.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                ago(r.ts),
                style = MaterialTheme.typography.labelMedium,
                color = style.fg.copy(alpha = 0.72f),
            )
            Text(
                "  ·  " + (if (r.mine) tr("You", "আপনি") else r.sender),
                style = MaterialTheme.typography.labelMedium,
                color = style.fg.copy(alpha = 0.72f),
            )
            Spacer(Modifier.weight(1f))
            // A forged report must never look like a trusted one, whatever it claims.
            Icon(
                if (r.verified) FeatherIcons.Shield else FeatherIcons.AlertCircle,
                contentDescription = if (r.verified) {
                    tr("Signed", "স্বাক্ষরিত")
                } else {
                    tr("Unverified", "অযাচাইকৃত")
                },
                tint = if (r.verified) style.fg.copy(alpha = 0.6f) else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(13.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(style.icon, contentDescription = null, tint = style.fg, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                if (LocalBangla.current) kind.bn else kind.en,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = style.fg,
            )
        }
        if (r.note.isNotBlank()) {
            Spacer(Modifier.height(3.dp))
            Text(
                r.note,
                style = MaterialTheme.typography.bodyMedium,
                color = style.fg.copy(alpha = 0.85f),
            )
        }
    }
}

/** Relative time, in the two words a person actually needs. */
@Composable
private fun ago(ts: Long): String {
    val minutes = ((System.currentTimeMillis() - ts) / 60_000L).coerceAtLeast(0)
    return when {
        minutes < 1 -> tr("just now", "এইমাত্র")
        minutes < 60 -> tr("$minutes min ago", "$minutes মিনিট আগে")
        minutes < 1440 -> tr("${minutes / 60} h ago", "${minutes / 60} ঘণ্টা আগে")
        else -> tr("${minutes / 1440} d ago", "${minutes / 1440} দিন আগে")
    }
}
