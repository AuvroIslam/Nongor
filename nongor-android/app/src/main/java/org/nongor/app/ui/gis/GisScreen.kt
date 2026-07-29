package org.nongor.app.ui.gis

import android.Manifest
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.verticalScroll
import compose.icons.FeatherIcons
import compose.icons.feathericons.AlertTriangle
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.ChevronDown
import compose.icons.feathericons.MapPin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.nongor.app.R
import org.nongor.app.core.Gis
import org.nongor.app.data.PublicShelterHit
import org.nongor.app.ui.components.HeroBanner
import org.nongor.app.ui.i18n.LocalBangla
import org.nongor.app.ui.i18n.tr
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.nongor.app.ui.theme.ShapeSm
import org.nongor.app.ui.theme.TileShelterFg
import org.nongor.app.ui.theme.CautionAmber
import org.nongor.app.ui.theme.ErrorRed
import org.nongor.app.ui.theme.GlassBorder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

private val FLOOD = Color(0xFF2196F3)
private val ROAD = Color(0xFF9E9E9E)
private val FLOODED_ROAD = ErrorRed
private val ROUTE = Color(0xFFFF9800)
private val SHELTER = Color(0xFF43A047)
private val USER = Color(0xFF1B1030)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class, ExperimentalLayoutApi::class)
@Composable
fun GisScreen(viewModel: GisViewModel, onBack: (() -> Unit)? = null) {
    val ui by viewModel.ui.collectAsState()
    val locationPerm = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION) {
        viewModel.findNearestShelter()
    }

    // Frame the map on what matters — you, the route and the shelters — not the whole road
    // extract, so the default view is readable.
    val bbox = remember(ui.userLat, ui.userLon, ui.shelters, ui.detailed, ui.nearbyPublic, ui.route) {
        val lats = ArrayList<Double>(); val lons = ArrayList<Double>()
        ui.route?.polyline?.forEach { lats.add(it[0]); lons.add(it[1]) }
        ui.floodPolys.forEach { r -> r.forEach { lats.add(it[1]); lons.add(it[0]) } }
        val pts = if (ui.detailed) ui.shelters.map { it.lat to it.lon }
        else ui.nearbyPublic.map { it.shelter.lat to it.shelter.lon }
        pts.forEach { lats.add(it.first); lons.add(it.second) }
        lats.add(ui.userLat); lons.add(ui.userLon)
        if (lats.size < 2) { lats.add(ui.userLat + 0.02); lons.add(ui.userLon + 0.02) }
        val padLat = (lats.max() - lats.min()).coerceAtLeast(0.004) * 0.12
        val padLon = (lons.max() - lons.min()).coerceAtLeast(0.004) * 0.12
        doubleArrayOf(lats.min() - padLat, lats.max() + padLat, lons.min() - padLon, lons.max() + padLon)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Nongor · Safe Shelter & Route", "নোঙর · নিরাপদ আশ্রয় ও পথ")) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(FeatherIcons.ArrowLeft, contentDescription = "Back")
                        }
                    }
                },
            )
        },
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
        ) {
            HeroBanner(FeatherIcons.MapPin, tint = TileShelterFg,
                title = tr("Safe Shelter", "নিরাপদ আশ্রয়"),
                subtitle = tr("Nearest shelter, safest way there", "নিকটতম আশ্রয়, নিরাপদতম পথ"))
            Spacer(Modifier.height(12.dp))
            val district = if (LocalBangla.current) ui.districtBn else ui.districtEn
            Text(
                when {
                    ui.locating -> tr("Locating…", "অবস্থান নেওয়া হচ্ছে…")
                    !ui.hasLocation -> tr("Showing $district shelters. Tap the map to set your location, or use GPS.",
                        "$district-এর আশ্রয় দেখানো হচ্ছে। আপনার অবস্থান দিতে ম্যাপে চাপ দিন, বা জিপিএস ব্যবহার করুন।")
                    ui.detailed -> tr("You are in $district.", "আপনি $district-এ আছেন।") +
                        (if (ui.manualPin) tr(" Pinned location.", " চিহ্নিত অবস্থান।")
                        else if (ui.usingGps) tr(" GPS location.", " জিপিএস অবস্থান।") else "")
                    else -> tr("You are in $district. Nearest shelters shown — tap one to see its distance.",
                        "আপনি $district-এ আছেন। নিকটতম আশ্রয় দেখানো হচ্ছে — দূরত্ব দেখতে একটিতে চাপ দিন।")
                },
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(10.dp))
            LocationCard(
                granted = locationPerm.status.isGranted,
                locating = ui.locating,
                hasLocation = ui.hasLocation,
                manualPin = ui.manualPin,
                gpsFailed = ui.gpsFailed,
                onEnable = { locationPerm.launchPermissionRequest() },
                onLocate = { viewModel.findNearestShelter() },
            )

            // ---- Map Assistant: ask in natural language; Gemma picks a tool, code runs it ----
            Spacer(Modifier.height(12.dp))
            MapAssistantCard(ui, onAsk = { viewModel.ask(it) })

            if (ui.detailed) {
                Spacer(Modifier.height(12.dp))
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Icon(FeatherIcons.AlertTriangle, null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            tr("The flood zone below is a sample scenario for practicing routes — " +
                                "not live flood data. The route avoids that sample zone, not confirmed " +
                                "real floodwater. Check local conditions before you travel.",
                                "নিচের বন্যা এলাকা পথ অনুশীলনের জন্য একটি নমুনা দৃশ্য — লাইভ বন্যা তথ্য নয়। " +
                                    "পথটি সেই নমুনা এলাকা এড়ায়, প্রকৃত বন্যার পানি নিশ্চিত নয়। যাত্রার আগে স্থানীয় " +
                                    "পরিস্থিতি যাচাই করুন।"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            // ---- area picker (over the map) ---- //
            // Only three districts ship with detailed road and flood data, so the picker can
            // only ever name one of those three. Standing in Khulna it would have sat there
            // saying "Chattogram" — which reads as the map being wrong about where you are.
            // Once we know you are outside the detailed packs, say what coverage you have
            // instead of offering a choice that does not describe you.
            Spacer(Modifier.height(12.dp))
            if (ui.hasLocation && !ui.detailed) {
                Text(
                    tr(
                        "Detailed routing covers three districts so far. Here you get the nearest " +
                            "known shelters and straight-line distances.",
                        "বিস্তারিত পথনির্দেশ এখন পর্যন্ত তিনটি জেলায় আছে। এখানে নিকটতম পরিচিত আশ্রয় ও " +
                            "সরলরেখার দূরত্ব দেখানো হচ্ছে।",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                AreaPicker(
                    regions = viewModel.regions(),
                    currentId = ui.overviewRegion,
                    onPick = { viewModel.setRegion(it) },
                )
            }

            // ---- mini map — tap to set your location ---- //
            Spacer(Modifier.height(8.dp))
            Card(Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth().height(260.dp).clipToBounds()) {
                    Canvas(
                        Modifier.fillMaxSize().pointerInput(bbox) {
                            detectTapGestures { off ->
                                val w = size.width.toFloat(); val h = size.height.toFloat(); val p = 14f
                                val dLat = (bbox[1] - bbox[0]).let { if (it == 0.0) 1e-6 else it }
                                val dLon = (bbox[3] - bbox[2]).let { if (it == 0.0) 1e-6 else it }
                                val lon = bbox[2] + ((off.x - p) / (w - 2 * p)) * dLon
                                val lat = bbox[1] - ((off.y - p) / (h - 2 * p)) * dLat
                                viewModel.setManualLocation(
                                    lat.coerceIn(bbox[0], bbox[1]), lon.coerceIn(bbox[2], bbox[3]))
                            }
                        },
                    ) { drawShelterMap(ui, bbox) }
                    if (!ui.hasLocation) {
                        Row(
                            Modifier.align(Alignment.BottomCenter).padding(8.dp)
                                .clip(ShapeSm).background(Color(0xCC1B1030))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(FeatherIcons.MapPin, null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(5.dp))
                            Text(tr("Tap the map to set your location", "অবস্থান দিতে ম্যাপে চাপ দিন"),
                                color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            MapLegend(ui.detailed)
            if (ui.detailed) {
                Text(
                    tr("Roads: © OpenStreetMap contributors.", "রাস্তা: © OpenStreetMap contributors।"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp))
            }

            // ---- results ---- //
            // Detailed mode: flood-avoiding walking route card.
            ui.route?.let { r ->
                Spacer(Modifier.height(12.dp))
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        val top = ui.ranked.firstOrNull { it.shelterId == ui.selectedShelterId }
                            ?: ui.ranked.firstOrNull()
                        Text("→ ${top?.name ?: tr("Shelter", "আশ্রয়")}",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${tr("Walking route", "হাঁটার পথ")}: ${fmtDist(r.distM)}",
                            style = MaterialTheme.typography.bodyMedium)
                        Text(
                            if (r.crossesFlood)
                                tr("⚠ No route avoiding the sample flood zone — this path crosses it.",
                                    "⚠ নমুনা বন্যা এলাকা এড়ানো কোনো পথ নেই — এই পথ সেটি পার হয়।")
                            else tr("✓ Route avoids the sample flood zone", "✓ পথটি নমুনা বন্যা এলাকা এড়ায়") +
                                if (ui.naiveCrossesFlood)
                                    tr(" (the direct line would have crossed it).",
                                        " (সরাসরি পথ সেটি পার হতো)।") else ".",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (r.crossesFlood) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Nationwide fallback: safe-direction guidance (no detailed pack for this district).
            if (ui.computed && !ui.detailed) {
                Spacer(Modifier.height(12.dp))
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(tr("Move to safety", "নিরাপদে সরে যান"),
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            tr("Head to the nearest high ground or a strong multi-storey building — a school, " +
                                "college or Union Parishad often serves as a flood shelter. Do not cross " +
                                "fast-moving water; even knee-deep flow can sweep you away.",
                                "নিকটতম উঁচু জায়গা বা মজবুত বহুতল ভবনে যান — স্কুল, কলেজ বা ইউনিয়ন পরিষদ প্রায়ই " +
                                    "বন্যা আশ্রয় হিসেবে ব্যবহৃত হয়। দ্রুত বয়ে চলা পানি পার হবেন না; হাঁটু-সমান স্রোতও " +
                                    "আপনাকে ভাসিয়ে নিতে পারে।"),
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (ui.detailed && ui.ranked.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(tr("Ranked shelters", "সাজানো আশ্রয়কেন্দ্র"),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                ui.ranked.forEach { s ->
                    ShelterRow(s, selected = s.shelterId == ui.selectedShelterId,
                        onClick = { viewModel.selectShelter(s) })
                }
            } else if (!ui.detailed && ui.nearbyPublic.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(tr("Nearest shelters (schools & colleges)", "নিকটতম আশ্রয় (স্কুল ও কলেজ)"),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(tr("Public schools and colleges — commonly used as flood shelters.",
                    "সরকারি স্কুল ও কলেজ — সাধারণত বন্যা আশ্রয় হিসেবে ব্যবহৃত হয়।"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                ui.nearbyPublic.forEachIndexed { i, h ->
                    PublicShelterRow(h, selected = i == ui.selectedPublicIdx,
                        best = i == 0, onClick = { viewModel.selectPublicShelter(i) })
                }
            } else if (!ui.computed) {
                Spacer(Modifier.height(12.dp))
                Text(tr("Tap “Find nearest safe shelter”. It uses your GPS and works anywhere in Bangladesh.",
                    "“নিকটতম নিরাপদ আশ্রয় খুঁজুন”-এ চাপ দিন। এটি আপনার জিপিএস ব্যবহার করে, বাংলাদেশের যেকোনো জায়গায় কাজ করে।"),
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MapAssistantCard(ui: GisUiState, onAsk: (String) -> Unit) {
    var q by remember { mutableStateOf("") }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(tr("Ask about your area", "আপনার এলাকা নিয়ে জিজ্ঞেস করুন"),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                tr(
                    "Gemma answers from the offline map and from what your neighbours have " +
                        "reported on the board — all on your phone.",
                    "Gemma অফলাইন ম্যাপ ও প্রতিবেশীরা বোর্ডে যা জানিয়েছেন তা থেকে উত্তর দেয় — সবই আপনার ফোনে।",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = q, onValueChange = { q = it }, modifier = Modifier.weight(1f),
                    placeholder = { Text(tr("Ask in Bangla or English…", "বাংলা বা ইংরেজিতে জিজ্ঞেস করুন…")) },
                    singleLine = true, enabled = !ui.assistantBusy,
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = { onAsk(q) }, enabled = !ui.assistantBusy && q.isNotBlank()) {
                    Text(tr("Ask", "জিজ্ঞেস"))
                }
            }
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistExample(tr("Nearest hospital", "নিকটতম হাসপাতাল")) { q = it; onAsk(it) }
                AssistExample(tr("Safe route to shelter", "আশ্রয়ে নিরাপদ পথ")) { q = it; onAsk(it) }
                AssistExample(
                    tr("What are neighbours reporting?", "প্রতিবেশীরা কী জানাচ্ছেন?"),
                ) { q = it; onAsk(it) }
            }

            if (ui.assistantBusy) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(tr("Gemma is working…", "Gemma কাজ করছে…"),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            ui.assistantAnswer?.let { ans ->
                Spacer(Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Gemma", style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        Text(ans, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
}

@Composable
private fun AssistExample(label: String, onClick: (String) -> Unit) {
    Text(label, style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clip(ShapeSm)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
            .clickable { onClick(label) }.padding(horizontal = 10.dp, vertical = 6.dp))
}

@Composable
private fun AreaPicker(
    regions: List<org.nongor.app.data.RegionPack>,
    currentId: String,
    onPick: (String) -> Unit,
) {
    val bangla = LocalBangla.current
    var open by remember { mutableStateOf(false) }
    val current = regions.firstOrNull { it.id == currentId }
    val currentName = current?.let { if (bangla) it.nameBn else it.nameEn }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(tr("Area:", "এলাকা:"), style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Box {
            Row(
                Modifier.clip(ShapeSm)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                    .clickable { open = true }.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(currentName ?: tr("Choose area", "এলাকা বাছুন"),
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(6.dp))
                Icon(FeatherIcons.ChevronDown, null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp))
            }
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                regions.forEach { r ->
                    DropdownMenuItem(
                        text = { Text(if (bangla) r.nameBn else r.nameEn) },
                        onClick = { open = false; onPick(r.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ShelterRow(s: Gis.RankedShelter, selected: Boolean, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick)
            .let {
                if (selected) it.border(2.dp, MaterialTheme.colorScheme.primary, ShapeSm)
                else it
            },
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(s.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false))
                if (s.onHighGround) {
                    Spacer(Modifier.width(8.dp))
                    HighGroundBadge()
                }
            }
            Text("${fmtDist(s.distM)} · ${s.capacityLeft} ${tr("spaces free", "জায়গা খালি")} · " +
                "${tr("score", "স্কোর")} ${"%.3f".format(s.score)}",
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun HighGroundBadge() {
    Text(
        tr("HIGH GROUND", "উঁচু জায়গা"),
        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary, maxLines = 1, softWrap = false,
        modifier = Modifier
            .clip(ShapeSm)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
private fun PublicShelterRow(
    h: PublicShelterHit, selected: Boolean, best: Boolean, onClick: () -> Unit,
) {
    val typeLabel = when (h.shelter.type) {
        "c" -> tr("College", "কলেজ")
        "u" -> tr("University", "বিশ্ববিদ্যালয়")
        else -> tr("School", "স্কুল")
    }
    val place = listOf(h.shelter.upazila, h.shelter.district)
        .filter { it.isNotBlank() }.joinToString(", ")
    Card(
        Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick)
            .let {
                if (selected) it.border(2.dp, MaterialTheme.colorScheme.primary, ShapeSm)
                else it
            },
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(h.shelter.name, style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false))
                if (best) {
                    Spacer(Modifier.width(8.dp))
                    Text(tr("AI PICK", "এআই পছন্দ"), style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clip(ShapeSm)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            Text("$typeLabel · ${fmtDist(h.distM)}" + if (place.isNotBlank()) " · $place" else "",
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun fmtDist(m: Int): String = if (m >= 1000) "%.1f km".format(m / 1000.0) else "$m m"

@Composable
private fun MapLegend(detailed: Boolean) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LegendDot(USER, ring = true)
            Spacer(Modifier.width(4.dp))
            Text(tr("you", "আপনি"), style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.width(14.dp))
            LegendDot(SHELTER)
            Spacer(Modifier.width(4.dp))
            Text(if (detailed) tr("shelter", "আশ্রয়") else tr("nearest shelter", "নিকটতম আশ্রয়"),
                style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (detailed) {
                LegendLine(ROUTE)
                Spacer(Modifier.width(4.dp))
                Text(tr("safe route", "নিরাপদ পথ"), style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.width(14.dp))
                LegendLine(FLOODED_ROAD, dashed = true)
                Spacer(Modifier.width(4.dp))
                Text(tr("flooded road", "বন্যা রাস্তা"), style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.width(14.dp))
                LegendSwatch(FLOOD)
                Spacer(Modifier.width(4.dp))
                Text(tr("sample flood zone", "নমুনা বন্যা এলাকা"), style = MaterialTheme.typography.labelSmall)
            } else {
                LegendLine(ROUTE, dashed = true)
                Spacer(Modifier.width(4.dp))
                Text(tr("direction", "দিক"), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, ring: Boolean = false) {
    Box(
        Modifier.size(10.dp).clip(CircleShape)
            .background(if (ring) Color.White else color)
            .let { if (ring) it.border(1.5.dp, color, CircleShape) else it },
    )
}

@Composable
private fun LegendLine(color: Color, dashed: Boolean = false) {
    if (dashed) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(6.dp).height(3.dp).background(color))
            Spacer(Modifier.width(2.dp))
            Box(Modifier.width(6.dp).height(3.dp).background(color))
        }
    } else {
        Box(Modifier.width(18.dp).height(3.dp).background(color))
    }
}

@Composable
private fun LegendSwatch(color: Color) {
    Box(Modifier.size(10.dp).background(color.copy(alpha = 0.35f)))
}

/** Renderer for the shelter mini-map preview. */
private fun DrawScope.drawShelterMap(ui: GisUiState, bbox: DoubleArray) {
    val w = size.width; val h = size.height; val p = 14f
    val minLat = bbox[0]; val maxLat = bbox[1]; val minLon = bbox[2]; val maxLon = bbox[3]
    val dLat = (maxLat - minLat).let { if (it == 0.0) 1e-6 else it }
    val dLon = (maxLon - minLon).let { if (it == 0.0) 1e-6 else it }
    fun ox(lon: Double) = (((lon - minLon) / dLon).toFloat()) * (w - 2 * p) + p
    fun oy(lat: Double) = (((maxLat - lat) / dLat).toFloat()) * (h - 2 * p) + p

    val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
    ui.floodPolys.forEach { ring ->
        val path = Path()
        ring.forEachIndexed { i, pt ->
            val x = ox(pt[0]); val y = oy(pt[1])
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path, FLOOD, alpha = 0.28f)
    }
    ui.graph?.let { g ->
        g.edges.forEach { (u, v) ->
            val a = g.nodes.getValue(u); val b = g.nodes.getValue(v)
            val flooded = Gis.segmentCrossesFlood(a, b, ui.floodPolys)
            drawLine(
                color = if (flooded) FLOODED_ROAD else ROAD.copy(alpha = 0.35f),
                start = Offset(ox(a[1]), oy(a[0])), end = Offset(ox(b[1]), oy(b[0])),
                strokeWidth = if (flooded) 2.5f else 1.6f,
                pathEffect = if (flooded) dash else null,
            )
        }
    }
    // Route (detailed) or direction line (nationwide) to the selected shelter — only when located.
    if (ui.hasLocation) {
        val poly = ui.route?.polyline
        if (poly != null) {
            for (i in 0 until poly.size - 1) {
                drawLine(ROUTE, Offset(ox(poly[i][1]), oy(poly[i][0])),
                    Offset(ox(poly[i + 1][1]), oy(poly[i + 1][0])), strokeWidth = 9f, cap = StrokeCap.Round)
            }
        } else {
            val sel = ui.selectedPublicIdx?.let { ui.nearbyPublic.getOrNull(it) }
                ?: ui.nearbyPublic.firstOrNull()
            sel?.let {
                drawLine(ROUTE, Offset(ox(ui.userLon), oy(ui.userLat)),
                    Offset(ox(it.shelter.lon), oy(it.shelter.lat)),
                    strokeWidth = 5f, pathEffect = dash, cap = StrokeCap.Round)
            }
        }
    }
    // Shelter markers (highlight the selected one).
    if (ui.detailed) {
        ui.shelters.forEach { s ->
            val c = Offset(ox(s.lon), oy(s.lat))
            if (ui.hasLocation && s.id == ui.selectedShelterId) {
                drawCircle(Color.White, 15f, c); drawCircle(ROUTE, 11f, c)
            } else {
                drawCircle(Color.White, 12f, c); drawCircle(SHELTER, 8.5f, c)
            }
        }
    } else {
        ui.nearbyPublic.forEachIndexed { i, hit ->
            val c = Offset(ox(hit.shelter.lon), oy(hit.shelter.lat))
            if (i == ui.selectedPublicIdx) {
                drawCircle(Color.White, 15f, c); drawCircle(ROUTE, 11f, c)
            } else {
                drawCircle(Color.White, 12f, c); drawCircle(SHELTER, 8.5f, c)
            }
        }
    }
    // "You" marker only when we actually have a location.
    if (ui.hasLocation) {
        val uc = Offset(ox(ui.userLon), oy(ui.userLat))
        drawCircle(Color.White, 14f, uc); drawCircle(USER, 10f, uc)
    }
}

/**
 * Where the map thinks you are, and the one control that changes it.
 *
 * This replaced a bare "Find nearest safe shelter" button that failed silently: with the
 * permission denied, or a GPS that never answers indoors, nothing on screen changed and the
 * map quietly kept showing whichever district was last stored. Every one of those states now
 * says what happened and offers the next tap.
 */
@Composable
private fun LocationCard(
    granted: Boolean,
    locating: Boolean,
    hasLocation: Boolean,
    manualPin: Boolean,
    gpsFailed: Boolean,
    onEnable: () -> Unit,
    onLocate: () -> Unit,
) {
    val tint = when {
        // A pin you dropped is a real location, but it is not GPS - amber, not green, so the
        // card never claims the phone found you when in fact you told it where you were.
        manualPin -> CautionAmber
        hasLocation -> SHELTER
        !granted || gpsFailed -> ErrorRed
        else -> TileShelterFg
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(ShapeSm)
            .background(tint.copy(alpha = 0.10f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (locating) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = tint)
        } else {
            Icon(FeatherIcons.MapPin, null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                when {
                    locating -> tr("Finding you…", "আপনাকে খোঁজা হচ্ছে…")
                    manualPin -> tr("Using the pin you placed", "আপনার দেওয়া পিন ব্যবহার হচ্ছে")
                    hasLocation -> tr("Using your GPS location", "আপনার জিপিএস অবস্থান ব্যবহার হচ্ছে")
                    !granted -> tr("Location is off", "লোকেশন বন্ধ")
                    gpsFailed -> tr("No GPS fix yet", "এখনও জিপিএস পাওয়া যায়নি")
                    else -> tr("Location is on", "লোকেশন চালু")
                },
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                when {
                    manualPin -> tr("Distances are measured from that pin, not from GPS. Tap the map to move it.", "দূরত্ব সেই পিন থেকে মাপা হচ্ছে, জিপিএস থেকে নয়। সরাতে ম্যাপে চাপ দিন।")
                    hasLocation -> tr("Shelters and routes are measured from you.", "আশ্রয় ও পথ আপনার অবস্থান থেকে মাপা হচ্ছে।")
                    !granted -> tr("Turn it on and the map opens where you actually are.", "চালু করলে ম্যাপ আপনি যেখানে আছেন সেখানে খুলবে।")
                    gpsFailed -> tr("Step outside, or tap the map to place yourself.", "বাইরে যান, অথবা ম্যাপে চাপ দিয়ে নিজের জায়গা দিন।")
                    else -> tr("Tap to point the map at you.", "ম্যাপ আপনার দিকে আনতে চাপ দিন।")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(10.dp))
        Button(
            onClick = { if (granted) onLocate() else onEnable() },
            enabled = !locating,
            shape = ShapeSm,
        ) {
            Text(
                when {
                    !granted -> tr("Turn on", "চালু করুন")
                    manualPin -> tr("Use GPS", "জিপিএস দিন")
                    hasLocation -> tr("Update", "হালনাগাদ")
                    else -> tr("Locate me", "আমাকে খুঁজুন")
                },
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
