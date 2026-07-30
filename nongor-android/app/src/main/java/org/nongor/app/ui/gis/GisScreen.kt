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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import compose.icons.feathericons.Crosshair
import compose.icons.feathericons.Check
import org.nongor.app.ui.theme.ShapePill
import org.nongor.app.ui.theme.ShapeMd
import org.nongor.app.ui.theme.TextPrimary
import org.nongor.app.ui.theme.TextSecondary

private val FLOOD = Color(0xFF2196F3)
private val ROAD = Color(0xFF9E9E9E)
private val FLOODED_ROAD = ErrorRed
private val ROUTE = Color(0xFFFF9800)
private val SHELTER = Color(0xFF43A047)
private val USER = Color(0xFF1B1030)
// The map surface. Warmer than the page so the canvas reads as a distinct object.
private val MapPaper = Color(0xFFEDEAE2)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class, ExperimentalLayoutApi::class)
@Composable
fun GisScreen(viewModel: GisViewModel, onBack: (() -> Unit)? = null) {
    val ui by viewModel.ui.collectAsState()
    val locationPerm = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION) {
        viewModel.findNearestShelter()
    }
    // Canvas cannot reach a Composable scope, so the text engine and the one translated string
    // the drawing needs are hoisted out here and handed down.
    val measurer = rememberTextMeasurer()
    val youLabel = tr("You", "আপনি")

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
                title = { Text(tr("Safe Shelter", "নিরাপদ আশ্রয়")) },
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
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 28.dp),
        ) {
            val district = if (LocalBangla.current) ui.districtBn else ui.districtEn

            // ---- The map, first and full width ----
            // It used to sit a full screen down, under a banner, a status line, a location
            // card, the assistant and two disclaimers. On a map screen the map is the answer;
            // everything else is a caption to it, so everything else now comes after.
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(330.dp)
                    .background(MapPaper)
                    .clipToBounds(),
            ) {
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
                ) { drawShelterMap(ui, bbox, measurer, youLabel) }

                // Where you are, floated over the map rather than stacked above it.
                Row(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(ShapePill)
                        .background(Color.White.copy(alpha = 0.94f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (ui.locating) {
                        CircularProgressIndicator(
                            Modifier.size(13.dp), strokeWidth = 2.dp, color = TileShelterFg,
                        )
                    } else {
                        Icon(
                            FeatherIcons.MapPin, null,
                            tint = if (ui.hasLocation) TileShelterFg else CautionAmber,
                            modifier = Modifier.size(13.dp),
                        )
                    }
                    Spacer(Modifier.width(7.dp))
                    Text(
                        when {
                            ui.locating -> tr("Finding you\u2026", "\u0986\u09aa\u09a8\u09be\u0995\u09c7 \u0996\u09cb\u0981\u099c\u09be \u09b9\u099a\u09cd\u099b\u09c7\u2026")
                            ui.hasLocation -> district
                            else -> tr("$district \u00b7 not your location", "$district \u00b7 \u0986\u09aa\u09a8\u09be\u09b0 \u0985\u09ac\u09b8\u09cd\u09a5\u09be\u09a8 \u09a8\u09df")
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                    )
                }

                // Recentre on yourself, where a map app puts it.
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(enabled = !ui.locating) {
                            if (locationPerm.status.isGranted) viewModel.findNearestShelter()
                            else locationPerm.launchPermissionRequest()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        FeatherIcons.Crosshair,
                        contentDescription = tr("Find me", "\u0986\u09ae\u09be\u0995\u09c7 \u0996\u09c1\u0981\u099c\u09c1\u09a8"),
                        tint = if (ui.hasLocation) TileShelterFg else CautionAmber,
                        modifier = Modifier.size(21.dp),
                    )
                }

                if (!ui.hasLocation) {
                    Row(
                        Modifier.align(Alignment.BottomStart).padding(12.dp)
                            .clip(ShapePill).background(Color(0xCC1B2A25))
                            .padding(horizontal = 11.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(FeatherIcons.MapPin, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(tr("Tap the map to place yourself", "\u09a8\u09bf\u099c\u09c7\u09b0 \u099c\u09be\u09df\u0997\u09be \u09a6\u09bf\u09a4\u09c7 \u09ae\u09cd\u09af\u09be\u09aa\u09c7 \u099a\u09be\u09aa \u09a6\u09bf\u09a8"),
                            color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Column(Modifier.padding(horizontal = 16.dp)) {
                // ---- Where you are, and the one control that changes it ----
                // First thing under the map: everything below is measured from this point, so
                // whether it is GPS, a pin you dropped, or nothing at all decides how much of
                // the rest you should trust.
                Spacer(Modifier.height(12.dp))
                LocationCard(
                    granted = locationPerm.status.isGranted,
                    locating = ui.locating,
                    hasLocation = ui.hasLocation,
                    manualPin = ui.manualPin,
                    gpsFailed = ui.gpsFailed,
                    onEnable = { locationPerm.launchPermissionRequest() },
                    onLocate = { viewModel.findNearestShelter() },
                )

                Spacer(Modifier.height(12.dp))
                MapLegend(ui.detailed)

                // ---- The answer ----
                ui.route?.let { r ->
                    Spacer(Modifier.height(14.dp))
                    val top = ui.ranked.firstOrNull { it.shelterId == ui.selectedShelterId }
                        ?: ui.ranked.firstOrNull()
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(ShapeMd)
                            .background(TileShelterFg.copy(alpha = 0.10f))
                            .padding(15.dp),
                    ) {
                        Text(
                            tr("Go here", "\u098f\u0996\u09be\u09a8\u09c7 \u09af\u09be\u09a8"),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TileShelterFg,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            top?.name ?: tr("Shelter", "\u0986\u09b6\u09cd\u09b0\u09df"),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            tr("Walking", "\u09b9\u09be\u0981\u099f\u09be\u09b0 \u09aa\u09a5") + " \u00b7 " + fmtDist(r.distM),
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary,
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (r.crossesFlood) FeatherIcons.AlertTriangle else FeatherIcons.Check,
                                null,
                                tint = if (r.crossesFlood) ErrorRed else TileShelterFg,
                                modifier = Modifier.size(15.dp),
                            )
                            Spacer(Modifier.width(7.dp))
                            Text(
                                if (r.crossesFlood) {
                                    tr("No route avoids the sample flood zone \u2014 this one crosses it.",
                                        "\u09a8\u09ae\u09c1\u09a8\u09be \u09ac\u09a8\u09cd\u09af\u09be \u098f\u09b2\u09be\u0995\u09be \u098f\u09dc\u09be\u09a8\u09cb \u0995\u09cb\u09a8\u09cb \u09aa\u09a5 \u09a8\u09c7\u0987 \u2014 \u098f\u0987 \u09aa\u09a5 \u09b8\u09c7\u099f\u09bf \u09aa\u09be\u09b0 \u09b9\u09df\u0964")
                                } else if (ui.naiveCrossesFlood) {
                                    tr("Avoids the sample flood zone \u2014 the direct line would have crossed it.",
                                        "\u09a8\u09ae\u09c1\u09a8\u09be \u09ac\u09a8\u09cd\u09af\u09be \u098f\u09b2\u09be\u0995\u09be \u098f\u09dc\u09be\u09df \u2014 \u09b8\u09b0\u09be\u09b8\u09b0\u09bf \u09aa\u09a5 \u09b8\u09c7\u099f\u09bf \u09aa\u09be\u09b0 \u09b9\u09a4\u09cb\u0964")
                                } else {
                                    tr("Avoids the sample flood zone.", "\u09a8\u09ae\u09c1\u09a8\u09be \u09ac\u09a8\u09cd\u09af\u09be \u098f\u09b2\u09be\u0995\u09be \u098f\u09dc\u09be\u09df\u0964")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (r.crossesFlood) ErrorRed else TextSecondary,
                            )
                        }
                    }
                }

                if (ui.computed && !ui.detailed) {
                    Spacer(Modifier.height(14.dp))
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(ShapeMd)
                            .background(TileShelterFg.copy(alpha = 0.10f))
                            .padding(15.dp),
                    ) {
                        Text(tr("Move to safety", "\u09a8\u09bf\u09b0\u09be\u09aa\u09a6\u09c7 \u09b8\u09b0\u09c7 \u09af\u09be\u09a8"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        Spacer(Modifier.height(5.dp))
                        Text(
                            tr("Head to the nearest high ground or a strong multi-storey building \u2014 a school, " +
                                "college or Union Parishad often serves as a flood shelter. Do not cross " +
                                "fast-moving water; even knee-deep flow can sweep you away.",
                                "\u09a8\u09bf\u0995\u099f\u09a4\u09ae \u0989\u0981\u099a\u09c1 \u099c\u09be\u09df\u0997\u09be \u09ac\u09be \u09ae\u099c\u09ac\u09c1\u09a4 \u09ac\u09b9\u09c1\u09a4\u09b2 \u09ad\u09ac\u09a8\u09c7 \u09af\u09be\u09a8 \u2014 \u09b8\u09cd\u0995\u09c1\u09b2, \u0995\u09b2\u09c7\u099c \u09ac\u09be \u0987\u0989\u09a8\u09bf\u09df\u09a8 \u09aa\u09b0\u09bf\u09b7\u09a6 \u09aa\u09cd\u09b0\u09be\u09df\u0987 " +
                                    "\u09ac\u09a8\u09cd\u09af\u09be \u0986\u09b6\u09cd\u09b0\u09df \u09b9\u09bf\u09b8\u09c7\u09ac\u09c7 \u09ac\u09cd\u09af\u09ac\u09b9\u09c3\u09a4 \u09b9\u09df\u0964 \u09a6\u09cd\u09b0\u09c1\u09a4 \u09ac\u09df\u09c7 \u099a\u09b2\u09be \u09aa\u09be\u09a8\u09bf \u09aa\u09be\u09b0 \u09b9\u09ac\u09c7\u09a8 \u09a8\u09be; \u09b9\u09be\u0981\u099f\u09c1-\u09b8\u09ae\u09be\u09a8 \u09b8\u09cd\u09b0\u09cb\u09a4\u0993 " +
                                    "\u0986\u09aa\u09a8\u09be\u0995\u09c7 \u09ad\u09be\u09b8\u09bf\u09df\u09c7 \u09a8\u09bf\u09a4\u09c7 \u09aa\u09be\u09b0\u09c7\u0964"),
                            style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    }
                }

                // ---- Every option, ranked ----
                if (ui.detailed && ui.ranked.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text(tr("Other shelters", "\u0985\u09a8\u09cd\u09af \u0986\u09b6\u09cd\u09b0\u09df"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    ui.ranked.forEach { sh ->
                        ShelterRow(sh, selected = sh.shelterId == ui.selectedShelterId,
                            onClick = { viewModel.selectShelter(sh) })
                    }
                } else if (!ui.detailed && ui.nearbyPublic.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text(tr("Nearest shelters", "\u09a8\u09bf\u0995\u099f\u09a4\u09ae \u0986\u09b6\u09cd\u09b0\u09df"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Text(tr("Public schools and colleges \u2014 commonly used as flood shelters.",
                        "\u09b8\u09b0\u0995\u09be\u09b0\u09bf \u09b8\u09cd\u0995\u09c1\u09b2 \u0993 \u0995\u09b2\u09c7\u099c \u2014 \u09b8\u09be\u09a7\u09be\u09b0\u09a3\u09a4 \u09ac\u09a8\u09cd\u09af\u09be \u0986\u09b6\u09cd\u09b0\u09df \u09b9\u09bf\u09b8\u09c7\u09ac\u09c7 \u09ac\u09cd\u09af\u09ac\u09b9\u09c3\u09a4 \u09b9\u09df\u0964"),
                        style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    ui.nearbyPublic.forEachIndexed { i, h ->
                        PublicShelterRow(h, selected = i == ui.selectedPublicIdx,
                            best = i == 0, onClick = { viewModel.selectPublicShelter(i) })
                    }
                }

                // ---- Ask about it ----
                Spacer(Modifier.height(18.dp))
                MapAssistantCard(ui, onAsk = { viewModel.ask(it) })

                // ---- The small print, at the bottom where small print belongs ----
                // Only three districts ship with detailed road and flood data, so the picker
                // can only ever name one of those three. Standing in Khulna it would have sat
                // there saying "Chattogram", which reads as the map being wrong about where
                // you are - so outside those packs we state the coverage instead.
                Spacer(Modifier.height(14.dp))
                if (ui.hasLocation && !ui.detailed) {
                    Text(
                        tr(
                            "Detailed routing covers three districts so far. Here you get the nearest " +
                                "known shelters and straight-line distances.",
                            "\u09ac\u09bf\u09b8\u09cd\u09a4\u09be\u09b0\u09bf\u09a4 \u09aa\u09a5\u09a8\u09bf\u09b0\u09cd\u09a6\u09c7\u09b6 \u098f\u0996\u09a8 \u09aa\u09b0\u09cd\u09af\u09a8\u09cd\u09a4 \u09a4\u09bf\u09a8\u099f\u09bf \u099c\u09c7\u09b2\u09be\u09df \u0986\u099b\u09c7\u0964 \u098f\u0996\u09be\u09a8\u09c7 \u09a8\u09bf\u0995\u099f\u09a4\u09ae \u09aa\u09b0\u09bf\u099a\u09bf\u09a4 \u0986\u09b6\u09cd\u09b0\u09df \u0993 " +
                                "\u09b8\u09b0\u09b2\u09b0\u09c7\u0996\u09be\u09b0 \u09a6\u09c2\u09b0\u09a4\u09cd\u09ac \u09a6\u09c7\u0996\u09be\u09a8\u09cb \u09b9\u099a\u09cd\u099b\u09c7\u0964",
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                } else {
                    AreaPicker(
                        regions = viewModel.regions(),
                        currentId = ui.overviewRegion,
                        onPick = { viewModel.setRegion(it) },
                    )
                }

                if (ui.detailed) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(ShapeSm)
                            .background(ErrorRed.copy(alpha = 0.08f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(FeatherIcons.AlertTriangle, null, tint = ErrorRed,
                            modifier = Modifier.size(15.dp).padding(top = 1.dp))
                        Spacer(Modifier.width(9.dp))
                        Text(
                            tr("The flood zone drawn here is a sample scenario for practising routes \u2014 " +
                                "not live flood data. Check local conditions before you travel.",
                                "\u098f\u0996\u09be\u09a8\u09c7 \u0986\u0981\u0995\u09be \u09ac\u09a8\u09cd\u09af\u09be \u098f\u09b2\u09be\u0995\u09be \u09aa\u09a5 \u0985\u09a8\u09c1\u09b6\u09c0\u09b2\u09a8\u09c7\u09b0 \u099c\u09a8\u09cd\u09af \u098f\u0995\u099f\u09bf \u09a8\u09ae\u09c1\u09a8\u09be \u2014 \u09b2\u09be\u0987\u09ad \u09ac\u09a8\u09cd\u09af\u09be \u09a4\u09a5\u09cd\u09af \u09a8\u09df\u0964 " +
                                    "\u09af\u09be\u09a4\u09cd\u09b0\u09be\u09b0 \u0986\u0997\u09c7 \u09b8\u09cd\u09a5\u09be\u09a8\u09c0\u09df \u09aa\u09b0\u09bf\u09b8\u09cd\u09a5\u09bf\u09a4\u09bf \u09af\u09be\u099a\u09be\u0987 \u0995\u09b0\u09c1\u09a8\u0964"),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        tr("Roads: \u00a9 OpenStreetMap contributors.", "\u09b0\u09be\u09b8\u09cd\u09a4\u09be: \u00a9 OpenStreetMap contributors\u0964"),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                }
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
    // The map draws every marker as a solid dot inside a white halo. The legend used to draw
    // the "you" swatch the other way round — white centre, coloured ring — so the key did not
    // match the thing it was a key for.
    if (ring) {
        Box(
            Modifier.size(13.dp).clip(CircleShape).background(Color.White).padding(1.5.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.fillMaxSize().clip(CircleShape).background(color))
        }
    } else {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
    }
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
private fun DrawScope.drawShelterMap(
    ui: GisUiState,
    bbox: DoubleArray,
    measurer: TextMeasurer,
    youLabel: String,
) {
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
    // Shelter markers (highlight the selected one), each captioned like a real map. Without a
    // name a dot only says "something is here"; the whole point of the map is knowing which
    // shelter you would be walking to before you commit to walking there.
    val labels = ArrayList<Pair<Offset, String>>()
    if (ui.detailed) {
        ui.shelters.forEach { s ->
            val c = Offset(ox(s.lon), oy(s.lat))
            if (ui.hasLocation && s.id == ui.selectedShelterId) {
                drawCircle(Color.White, 15f, c); drawCircle(ROUTE, 11f, c)
            } else {
                drawCircle(Color.White, 12f, c); drawCircle(SHELTER, 8.5f, c)
            }
            labels += c to shortLabel(s.name)
        }
    } else {
        ui.nearbyPublic.forEachIndexed { i, hit ->
            val c = Offset(ox(hit.shelter.lon), oy(hit.shelter.lat))
            if (i == ui.selectedPublicIdx) {
                drawCircle(Color.White, 15f, c); drawCircle(ROUTE, 11f, c)
            } else {
                drawCircle(Color.White, 12f, c); drawCircle(SHELTER, 8.5f, c)
            }
            labels += c to shortLabel(hit.shelter.name)
        }
    }
    // "You" marker only when we actually have a location.
    if (ui.hasLocation) {
        val uc = Offset(ox(ui.userLon), oy(ui.userLat))
        drawCircle(Color.White, 14f, uc); drawCircle(USER, 10f, uc)
        labels += uc to youLabel
    }

    // Captions last, so a name is never painted over by a marker drawn after it. Skipped
    // entirely once the map is crowded — twenty overlapping names is less readable than none.
    if (labels.size <= MAX_LABELS) {
        labels.forEach { (c, text) -> drawMapLabel(measurer, c, text) }
    }
}

/**
 * A caption sitting just above a marker, on its own pale plate.
 *
 * The plate matters: road lines and the flood polygon run underneath, and dark text straight
 * onto them is unreadable exactly where the map is busiest.
 */
private fun DrawScope.drawMapLabel(measurer: TextMeasurer, at: Offset, text: String) {
    if (text.isBlank()) return
    val laid = measurer.measure(
        AnnotatedString(text),
        style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold),
    )
    val w = laid.size.width.toFloat()
    val h = laid.size.height.toFloat()
    val x = (at.x - w / 2f).coerceIn(2f, (size.width - w - 2f).coerceAtLeast(2f))
    val y = (at.y - 18f - h).coerceAtLeast(2f)
    drawRoundRect(
        color = Color.White.copy(alpha = 0.82f),
        topLeft = Offset(x - 3f, y - 1f),
        size = androidx.compose.ui.geometry.Size(w + 6f, h + 2f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
    )
    drawText(laid, color = LABEL_INK, topLeft = Offset(x, y))
}

/**
 * A shelter name cut down to something that fits over a dot.
 *
 * Names in the packs run long — "Chattogram Govt. Muslim High School" — and the marker is
 * eight pixels across. The full name is one tap away in the ranked list below the map, so the
 * caption only has to be enough to tell two nearby dots apart.
 */
private fun shortLabel(name: String): String {
    val cleaned = name.substringBefore(",").trim()
    return if (cleaned.length <= LABEL_CHARS) cleaned
    else cleaned.take(LABEL_CHARS).trimEnd() + "…"
}

private const val LABEL_CHARS = 14
private const val MAX_LABELS = 14
private val LABEL_INK = Color(0xFF1B2A25)

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
