package org.nongor.app.ui.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.FeatherIcons
import compose.icons.feathericons.AlertCircle
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.BarChart2
import compose.icons.feathericons.FileText
import compose.icons.feathericons.Home
import compose.icons.feathericons.MapPin
import org.nongor.app.ui.i18n.LocalBangla
import org.nongor.app.ui.i18n.localiseDigits
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.BgCard
import org.nongor.app.ui.theme.BriefBlue
import org.nongor.app.ui.theme.CautionAmber
import org.nongor.app.ui.theme.ErrorRed
import org.nongor.app.ui.theme.SafeGreen
import org.nongor.app.ui.theme.ShapeMd
import org.nongor.app.ui.theme.ShapePill
import org.nongor.app.ui.theme.ShapeSm
import org.nongor.app.ui.theme.TextPrimary
import org.nongor.app.ui.theme.TextSecondary

/**
 * The situation briefing.
 *
 * Two different things live on this screen and they are deliberately kept apart. The numbers
 * are computed in code and are exact. The prose is written by Gemma from those numbers and is
 * a paraphrase. Everything below the briefing block — case coordinates, shelter occupancy — is
 * rendered straight from the stats and never routed through the model, because it has been
 * caught turning "500" into "50000" and "455 crossing segments" into "455 blocked roads".
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SummaryScreen(viewModel: SummaryViewModel, onBack: () -> Unit) {
    val ui by viewModel.ui.collectAsState()
    val context = LocalContext.current
    val bangla = LocalBangla.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Situation briefing", "পরিস্থিতির সারসংক্ষেপ")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(FeatherIcons.ArrowLeft, contentDescription = "Back")
                    }
                },
            )
        },
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                // The hero banner used to provide this gap. Without one, the first block sat
                // flush against the top bar and read as clipped.
                .padding(top = 14.dp, bottom = 28.dp),
        ) {
            // ---- The dashboard ----
            ui.stats?.let { st ->
                Row(Modifier.fillMaxWidth()) {
                    BigStat(
                        localiseDigits("${st.totalSos}", bangla),
                        tr("cases in total", "মোট কেস"),
                        BriefBlue, Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(10.dp))
                    BigStat(
                        localiseDigits("${st.critical}", bangla),
                        tr("critical right now", "এখন সংকটাপন্ন"),
                        ErrorRed, Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth()) {
                    SmallStat(tr("High", "জরুরি"), st.high, CautionAmber, bangla, Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    SmallStat(tr("Moderate", "মাঝারি"), st.moderate, BriefBlue, bangla, Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    SmallStat(tr("Low", "কম"), st.low, SafeGreen, bangla, Modifier.weight(1f))
                }
                Spacer(Modifier.height(14.dp))
            }

            // ---- Where the numbers come from, and the button that writes the prose ----
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(ShapeMd)
                    .background(BriefBlue.copy(alpha = 0.08f))
                    .padding(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(38.dp).clip(CircleShape).background(BriefBlue.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(FeatherIcons.BarChart2, null, tint = BriefBlue, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            localiseDigits(
                                tr("${ui.reportCount} reports on this phone", "এই ফোনে ${ui.reportCount}টি রিপোর্ট"),
                                bangla,
                            ),
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                        )
                        Text(
                            tr(
                                "Counted in code. Gemma only writes them up.",
                                "সংখ্যা কোডেই গোনা হয়। জেমা শুধু তা লিখে দেয়।",
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }

                if (ui.quarantineCount > 0) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(ShapeSm)
                            .background(ErrorRed.copy(alpha = 0.10f))
                            .padding(horizontal = 11.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            FeatherIcons.AlertCircle, null,
                            tint = ErrorRed, modifier = Modifier.size(15.dp).padding(top = 1.dp),
                        )
                        Spacer(Modifier.width(9.dp))
                        Text(
                            localiseDigits(
                                tr(
                                    "${ui.quarantineCount} mesh reports failed their signature check. " +
                                        "Held back, and left out of this briefing.",
                                    "${ui.quarantineCount}টি মেশ রিপোর্ট স্বাক্ষর যাচাইয়ে ব্যর্থ। " +
                                        "সেগুলো আটকে রাখা হয়েছে, এই ব্রিফিংয়ে নেই।",
                                ),
                                bangla,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                val canRun = !ui.busy && ui.reportCount > 0
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(ShapePill)
                        .background(if (canRun) BriefBlue else BriefBlue.copy(alpha = 0.32f))
                        .clickable(enabled = canRun) { viewModel.generate() }
                        .padding(vertical = 13.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (ui.busy) {
                        CircularProgressIndicator(Modifier.size(17.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(Modifier.width(9.dp))
                    }
                    Text(
                        if (ui.busy) tr("Writing the briefing…", "ব্রিফিং লেখা হচ্ছে…")
                        else tr("Write the briefing", "ব্রিফিং লিখুন"),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                    )
                }
            }

            if (ui.reportCount == 0 && !ui.busy) {
                Spacer(Modifier.height(26.dp))
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier.size(56.dp).clip(CircleShape).background(BriefBlue.copy(alpha = 0.11f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(FeatherIcons.FileText, null, tint = BriefBlue, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        tr("Nothing to brief on yet", "এখনো ব্রিফ করার কিছু নেই"),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        tr(
                            "Cases you rank, and SOS calls this phone sends or hears, collect here.",
                            "আপনার সাজানো কেস আর এই ফোনের পাঠানো বা শোনা এসওএস এখানে জমা হয়।",
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            ui.error?.let {
                Spacer(Modifier.height(10.dp))
                Text(
                    "${tr("Error", "সমস্যা")}: $it",
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            // ---- The prose ----
            ui.briefing?.let { b ->
                Spacer(Modifier.height(16.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(ShapeMd)
                        .background(BriefBlue)
                        .padding(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            FeatherIcons.FileText, null,
                            tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            tr("Briefing", "ব্রিফিং"),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        b,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                    )
                }
            }

            ui.stats?.let { st ->
                if (st.blockedRoads.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    // Rendered here in code rather than left to the briefing prose. The engine
                    // counts graph *segments* that cross the flood layer; asked to paraphrase
                    // it, the model reported "455 blocked roads", a different and much bigger
                    // claim than the data supports.
                    Text(
                        localiseDigits(
                            tr(
                                "${st.blockedRoads.size} road segments cross the flood layer " +
                                    "(illustrative scenario, not live flood data).",
                                "${st.blockedRoads.size}টি রাস্তার অংশ বন্যা স্তর অতিক্রম করেছে " +
                                    "(নমুনা পরিস্থিতি, সরাসরি বন্যার তথ্য নয়)।",
                            ),
                            bangla,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                }
            }

            // ---- Exact per-case list ----
            ui.stats?.top5?.takeIf { it.isNotEmpty() }?.let { cases ->
                Spacer(Modifier.height(20.dp))
                SectionHeading(tr("Worst cases", "সবচেয়ে খারাপ কেস"))
                Spacer(Modifier.height(10.dp))
                cases.forEach { c ->
                    val point = parseLatLon(c.loc)
                    val tint = priorityColour(c.priority)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(ShapeMd)
                            .background(BgCard),
                    ) {
                        Box(Modifier.width(5.dp).fillMaxHeight().background(tint))
                        Column(Modifier.padding(13.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    c.priority.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = tint,
                                    modifier = Modifier
                                        .clip(ShapePill)
                                        .background(tint.copy(alpha = 0.13f))
                                        .padding(horizontal = 9.dp, vertical = 3.dp),
                                )
                                Spacer(Modifier.width(9.dp))
                                Text(
                                    c.id,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary,
                                )
                                Spacer(Modifier.weight(1f))
                                // A coordinate you cannot act on is just a number. This hands
                                // it to whatever map the phone has, via a geo: URI — no network
                                // of ours involved, and offline maps handle it too.
                                if (point != null) {
                                    Row(
                                        Modifier
                                            .clip(ShapePill)
                                            .background(BriefBlue.copy(alpha = 0.12f))
                                            .clickable { openMap(context, point.first, point.second) }
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            FeatherIcons.MapPin, null,
                                            tint = BriefBlue, modifier = Modifier.size(12.dp),
                                        )
                                        Spacer(Modifier.width(5.dp))
                                        Text(
                                            tr("Open map", "ম্যাপে দেখুন"),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = BriefBlue,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(7.dp))
                            Text(c.reason, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (point != null) "GPS ${c.loc}" else tr("No GPS location", "জিপিএস অবস্থান নেই"),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                            )
                        }
                    }
                }
            }

            // ---- Shelter pressure, as bars ----
            // A percentage in a sentence is a number you have to think about. A bar that is
            // nearly full is a shelter you can see is nearly full.
            ui.stats?.shelterPressure?.takeIf { it.isNotEmpty() }?.let { shelters ->
                Spacer(Modifier.height(14.dp))
                SectionHeading(tr("Shelter pressure", "আশ্রয়ের চাপ"))
                Spacer(Modifier.height(10.dp))
                shelters.sortedByDescending { it.pressure }.forEach { s ->
                    val pct = (s.pressure * 100).toInt()
                    val tint = when {
                        s.pressure >= 0.95 -> ErrorRed
                        s.pressure >= 0.75 -> CautionAmber
                        else -> SafeGreen
                    }
                    Column(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(FeatherIcons.Home, null, tint = tint, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                s.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                localiseDigits("${s.occupancy}/${s.capacity}", bangla),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = tint,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(ShapePill)
                                .background(tint.copy(alpha = 0.15f)),
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth(s.pressure.toFloat().coerceIn(0f, 1f))
                                    .height(8.dp)
                                    .clip(ShapePill)
                                    .background(tint),
                            )
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            localiseDigits(tr("$pct% full", "$pct% পূর্ণ"), bangla),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text,
        fontWeight = FontWeight.ExtraBold,
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
    )
}

/** The two numbers a coordinator reads first, at a size that does not need reading twice. */
@Composable
private fun BigStat(value: String, label: String, tint: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(ShapeMd)
            .background(tint.copy(alpha = 0.11f))
            .padding(16.dp),
    ) {
        Text(value, color = tint, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp)
        Text(
            label,
            color = tint,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun SmallStat(
    label: String,
    n: Int,
    tint: Color,
    bangla: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(ShapeSm)
            .background(tint.copy(alpha = 0.10f))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            localiseDigits("$n", bangla),
            color = tint,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
        )
        Text(label, color = tint, style = MaterialTheme.typography.labelSmall)
    }
}

private fun priorityColour(priority: String): Color = when (priority.lowercase()) {
    "critical" -> ErrorRed
    "high" -> CautionAmber
    "moderate", "medium" -> BriefBlue
    else -> SafeGreen
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
