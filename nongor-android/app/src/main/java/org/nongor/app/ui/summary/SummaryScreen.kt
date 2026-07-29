package org.nongor.app.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.components.PriorityCount
import org.nongor.app.ui.components.PriorityDot

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SummaryScreen(viewModel: SummaryViewModel, onBack: () -> Unit) {
    val ui by viewModel.ui.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Nongor · Coordinator Summary", "নোঙর · সমন্বয়কারী সারাংশ")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(FeatherIcons.ArrowLeft, contentDescription = "Back")
                    }
                },
            )
        },
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
        ) {
            Text(
                tr("Aggregates the field reports on this device. Counts are computed on-device " +
                    "(never hallucinated); Gemma 4 writes the briefing.",
                    "এই ডিভাইসের সব রিপোর্ট একত্র করে। সংখ্যা ডিভাইসেই গণনা করা হয় " +
                        "(কখনো বানানো নয়); Gemma 4 ব্রিফিং লেখে।"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary)

            Spacer(Modifier.height(8.dp))
            Text("${ui.reportCount} " + tr("report(s) collected so far.", "টি রিপোর্ট এ পর্যন্ত সংগৃহীত।"),
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (ui.quarantineCount > 0) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "⚠ ${ui.quarantineCount} " + tr(
                        "mesh report(s) failed signature verification — held for review, excluded from this briefing.",
                        "মেশ রিপোর্ট স্বাক্ষর যাচাইয়ে ব্যর্থ — পর্যালোচনার জন্য রাখা হয়েছে, এই ব্রিফিং থেকে বাদ।"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(12.dp))
            Button(onClick = { viewModel.generate() }, enabled = !ui.busy && ui.reportCount > 0) {
                if (ui.busy) {
                    CircularProgressIndicator(Modifier.height(18.dp).width(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(tr("Writing briefing…", "ব্রিফিং লেখা হচ্ছে…"))
                } else {
                    Text(tr("Generate coordinator briefing", "সমন্বয়কারী ব্রিফিং তৈরি করুন"))
                }
            }

            if (ui.reportCount == 0 && !ui.busy) {
                Spacer(Modifier.height(12.dp))
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(tr("No field reports yet", "এখনো কোনো রিপোর্ট নেই"),
                            fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            tr("Cases you triage in Rescue Triage, and SOS you send or receive over Mesh " +
                                "SOS, collect here. Then I'll write the coordinator briefing from them.",
                                "উদ্ধার ট্রায়াজে করা কেস এবং মেশ এসওএস-এ পাঠানো বা পাওয়া এসওএস এখানে জমা হয়। " +
                                    "তারপর সেগুলো থেকে আমি সমন্বয়কারী ব্রিফিং লিখব।"),
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            ui.error?.let {
                Spacer(Modifier.height(8.dp))
                Text("${tr("Error", "সমস্যা")}: $it", color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }

            ui.stats?.let { st ->
                Spacer(Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text("${tr("Total", "মোট")} ${st.totalSos}") })
                    PriorityCount("critical", st.critical, tr("Critical", "সংকটাপন্ন"))
                    PriorityCount("high", st.high, tr("High", "জরুরি"))
                    PriorityCount("moderate", st.moderate, tr("Moderate", "মাঝারি"))
                    PriorityCount("low", st.low, tr("Low", "কম"))
                }
            }

            ui.briefing?.let { b ->
                Spacer(Modifier.height(12.dp))
                Card(Modifier.fillMaxWidth()) {
                    Text(b, Modifier.padding(14.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Exact per-case list, rendered straight from the code-computed stats — never routed
            // through the model, whose coordinate-copying can't be trusted digit-for-digit.
            ui.stats?.top5?.takeIf { it.isNotEmpty() }?.let { cases ->
                Spacer(Modifier.height(12.dp))
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(tr("Top cases", "শীর্ষ কেস"), fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall)
                        cases.forEach { c ->
                            Spacer(Modifier.height(8.dp))
                            val loc = if (c.loc == "loc?") tr("no GPS location", "জিপিএস অবস্থান নেই")
                            else tr("GPS ${c.loc}", "জিপিএস ${c.loc}")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PriorityDot(c.priority, 8.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("${c.id} · $loc",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold)
                            }
                            Text(c.reason, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Shelter capacities, also rendered from the exact numbers rather than the model's
            // paraphrase (which mangled "500" into "50000"). Most-pressured first.
            ui.stats?.shelterPressure?.takeIf { it.isNotEmpty() }?.let { shelters ->
                Spacer(Modifier.height(12.dp))
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(tr("Shelter capacity", "আশ্রয় ধারণক্ষমতা"), fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall)
                        shelters.sortedByDescending { it.pressure }.forEach { s ->
                            Spacer(Modifier.height(6.dp))
                            Text("${s.name} — ${s.occupancy}/${s.capacity} (${(s.pressure * 100).toInt()}%)",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

