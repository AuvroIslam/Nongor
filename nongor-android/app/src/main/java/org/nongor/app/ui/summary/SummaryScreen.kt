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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import compose.icons.feathericons.FileText
import compose.icons.feathericons.MapPin
import org.nongor.app.ui.components.StatPill
import org.nongor.app.ui.theme.BrandTeal
import org.nongor.app.ui.theme.ShapeMd
import org.nongor.app.ui.theme.ShapePill
import org.nongor.app.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SummaryScreen(viewModel: SummaryViewModel, onBack: () -> Unit) {
    val ui by viewModel.ui.collectAsState()
    val context = LocalContext.current

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
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatPill(tr("Total", "মোট"), st.totalSos)
                    PriorityCount("critical", st.critical, tr("Critical", "সংকটাপন্ন"))
                    PriorityCount("high", st.high, tr("High", "জরুরি"))
                    PriorityCount("moderate", st.moderate, tr("Moderate", "মাঝারি"))
                    PriorityCount("low", st.low, tr("Low", "কম"))
                }
                // Rendered here in code rather than left to the briefing prose. The engine
                // counts graph *segments* that cross the flood layer; asked to paraphrase it,
                // the model reported "455 blocked roads", which is a different and much
                // bigger claim than the data supports.
                if (st.blockedRoads.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        tr(
                            "${st.blockedRoads.size} road segments cross the flood layer " +
                                "(illustrative scenario, not live flood data).",
                            "${st.blockedRoads.size} টি রাস্তার অংশ বন্যা স্তর অতিক্রম করেছে " +
                                "(নমুনা পরিস্থিতি, সরাসরি বন্যার তথ্য নয়)।",
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            ui.briefing?.let { b ->
                Spacer(Modifier.height(12.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(ShapeMd)
                        .background(BrandTeal.copy(alpha = 0.09f))
                        .padding(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            FeatherIcons.FileText,
                            contentDescription = null,
                            tint = BrandTeal,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            tr("Briefing", "ব্রিফিং"),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = BrandTeal,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(b, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
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
                            Spacer(Modifier.height(10.dp))
                            val point = parseLatLon(c.loc)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PriorityDot(c.priority, 8.dp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    c.id,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(Modifier.weight(1f))
                                // A coordinate you cannot act on is just a number. This hands
                                // it to whatever map the phone has, via a geo: URI — no
                                // network of ours involved, and offline maps handle it too.
                                if (point != null) {
                                    Row(
                                        Modifier
                                            .clip(ShapePill)
                                            .background(BrandTeal.copy(alpha = 0.12f))
                                            .clickable { openMap(context, point.first, point.second) }
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            FeatherIcons.MapPin,
                                            contentDescription = null,
                                            tint = BrandTeal,
                                            modifier = Modifier.size(12.dp),
                                        )
                                        Spacer(Modifier.width(5.dp))
                                        Text(
                                            tr("Map", "মানচিত্র"),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = BrandTeal,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }
                            Text(
                                if (point != null) {
                                    tr("GPS ${c.loc}", "জিপিএস ${c.loc}")
                                } else {
                                    tr("No GPS location", "জিপিএস অবস্থান নেই")
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
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


/**
 * Read "22.898,89.501" back into a point, or null when the case had no fix.
 *
 * Deliberately strict about range: a corrupted coordinate that still parses would drop a
 * "Map" button on the card that sends a responder somewhere in the ocean.
 */
private fun parseLatLon(loc: String): Pair<Double, Double>? {
    val parts = loc.split(',')
    if (parts.size != 2) return null
    val lat = parts[0].trim().toDoubleOrNull() ?: return null
    val lon = parts[1].trim().toDoubleOrNull() ?: return null
    if (lat !in -90.0..90.0 || lon !in -180.0..180.0) return null
    return lat to lon
}

/** Hand the point to whichever map app is installed. Nothing is fetched by us. */
private fun openMap(context: android.content.Context, lat: Double, lon: Double) {
    val uri = android.net.Uri.parse("geo:$lat,$lon?q=$lat,$lon(SOS)")
    runCatching { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri)) }
}
