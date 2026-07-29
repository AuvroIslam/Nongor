package org.nongor.app.ui.triage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.FeatherIcons
import compose.icons.feathericons.AlertTriangle
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.Camera
import compose.icons.feathericons.CheckCircle
import compose.icons.feathericons.Inbox
import compose.icons.feathericons.Navigation
import compose.icons.feathericons.X
import org.nongor.app.ui.i18n.LocalBangla
import org.nongor.app.ui.i18n.localiseDigits
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.BgCard
import org.nongor.app.ui.theme.BrandTeal
import org.nongor.app.ui.theme.CautionAmber
import org.nongor.app.ui.theme.ErrorRed
import org.nongor.app.ui.theme.SafeGreen
import org.nongor.app.ui.theme.ShapeMd
import org.nongor.app.ui.theme.ShapePill
import org.nongor.app.ui.theme.ShapeSm
import org.nongor.app.ui.theme.TextPrimary
import org.nongor.app.ui.theme.TextSecondary

private val EXAMPLES_EN = listOf(
    "Pregnant woman trapped on the rooftop, water is still rising fast, no food since morning.",
    "My father is not breathing after we pulled him out of the floodwater.",
    "Elderly man has heavy bleeding from a deep leg cut, blood soaking the cloth.",
    "We are four people safe on the second floor but out of drinking water.",
)

private val EXAMPLES_BN = listOf(
    "গর্ভবতী নারী ছাদে আটকা, পানি দ্রুত বাড়ছে, সকাল থেকে খাবার নেই।",
    "বন্যার পানি থেকে তোলার পর আমার বাবা শ্বাস নিচ্ছে না।",
    "বয়স্ক একজনের পায়ে গভীর কাটা থেকে প্রচুর রক্তক্ষরণ, কাপড় ভিজে যাচ্ছে।",
    "আমরা চারজন দোতলায় নিরাপদ, কিন্তু খাওয়ার পানি নেই।",
)

private val CHIPS_EN = listOf("Trapped on roof", "Not breathing", "Heavy bleeding", "No drinking water")
private val CHIPS_BN = listOf("ছাদে আটকা", "শ্বাস নিচ্ছে না", "প্রচুর রক্তক্ষরণ", "খাওয়ার পানি নেই")

/**
 * Who needs help first.
 *
 * A volunteer with one boat and nine calls has exactly one question, and it is not "what did
 * the model say". It is "which of these do I go to now". So the queue is the screen: banded
 * by priority, worst at the top, each case wearing its colour on a spine you can find while
 * scrolling with wet hands. Adding a case is a composer that collapses out of the way once
 * there is a queue to read.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TriageScreen(viewModel: TriageViewModel, onBack: () -> Unit) {
    val ui by viewModel.ui.collectAsState()
    var text by remember { mutableStateOf("") }
    val bangla = LocalBangla.current
    val examples = if (bangla) EXAMPLES_BN else EXAMPLES_EN
    val chips = if (bangla) CHIPS_BN else CHIPS_EN
    // Once there is a queue, the queue is what you came for. The composer folds away rather
    // than pushing every case below the fold.
    var composing by remember { mutableStateOf(true) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.setImageFromUri(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Who needs help first", "আগে কার সাহায্য দরকার")) },
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
            // ---- Counts across the top: the shape of the workload in one glance ----
            if (ui.queue.isNotEmpty()) {
                val counts = ui.queue.groupingBy { it.result.priority.lowercase() }.eachCount()
                Row(Modifier.fillMaxWidth()) {
                    Tally(
                        tr("Critical", "গুরুতর"), counts["critical"] ?: 0, ErrorRed,
                        bangla, Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(9.dp))
                    Tally(
                        tr("High", "উচ্চ"), counts["high"] ?: 0, CautionAmber,
                        bangla, Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(9.dp))
                    Tally(
                        tr("Rest", "বাকি"),
                        ui.queue.size - (counts["critical"] ?: 0) - (counts["high"] ?: 0),
                        BrandTeal, bangla, Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(14.dp))
            }

            // ---- Add a case ----
            if (composing || ui.queue.isEmpty()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(ShapeMd)
                        .background(CautionAmber.copy(alpha = 0.09f))
                        .padding(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(38.dp).clip(CircleShape)
                                .background(CautionAmber.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                FeatherIcons.AlertTriangle, null,
                                tint = CautionAmber, modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(Modifier.width(11.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                tr("Add a case", "একটি কেস যোগ করুন"),
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                            )
                            Text(
                                when {
                                    ui.engineLoading -> tr("Loading the on-device model…", "ডিভাইসের মডেল লোড হচ্ছে…")
                                    ui.engineReady -> tr("Ranked on this phone, offline", "এই ফোনেই সাজানো হয়, অফলাইনে")
                                    else -> tr("Model not loaded — using the rule-based ranking", "মডেল লোড হয়নি — নিয়মভিত্তিক সাজানো")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                            )
                        }
                        if (ui.queue.isNotEmpty()) {
                            IconButton(onClick = { composing = false }) {
                                Icon(FeatherIcons.X, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = ShapeSm,
                        minLines = 2,
                        maxLines = 4,
                        placeholder = {
                            Text(
                                tr("What did they say, and where are they?", "তাঁরা কী বলেছেন, কোথায় আছেন?"),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = BgCard,
                            unfocusedContainerColor = BgCard,
                            focusedIndicatorColor = CautionAmber,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = CautionAmber,
                        ),
                    )

                    Spacer(Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        chips.forEachIndexed { i, label ->
                            Text(
                                label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = CautionAmber,
                                modifier = Modifier
                                    .clip(ShapePill)
                                    .background(BgCard)
                                    .clickable { text = examples[i] }
                                    .padding(horizontal = 13.dp, vertical = 8.dp),
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    if (ui.imagePath == null) {
                        Row(
                            Modifier
                                .clip(ShapePill)
                                .background(BgCard)
                                .clickable {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly,
                                        ),
                                    )
                                }
                                .padding(horizontal = 13.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(FeatherIcons.Camera, null, tint = CautionAmber, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(7.dp))
                            Text(
                                tr("Add a photo", "ছবি দিন"),
                                style = MaterialTheme.typography.labelLarge,
                                color = CautionAmber,
                            )
                        }
                    } else {
                        Row(
                            Modifier
                                .clip(ShapePill)
                                .background(CautionAmber.copy(alpha = 0.18f))
                                .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(FeatherIcons.CheckCircle, null, tint = CautionAmber, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(7.dp))
                            Text(
                                tr("Photo attached", "ছবি যুক্ত"),
                                style = MaterialTheme.typography.labelLarge,
                                color = CautionAmber,
                            )
                            IconButton(onClick = { viewModel.clearImage() }, modifier = Modifier.size(26.dp)) {
                                Icon(FeatherIcons.X, null, tint = CautionAmber, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    val canRun = !ui.busy && (text.isNotBlank() || ui.imagePath != null)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(ShapePill)
                            .background(if (canRun) CautionAmber else CautionAmber.copy(alpha = 0.35f))
                            .clickable(enabled = canRun) { viewModel.triage(text); text = "" }
                            .padding(vertical = 13.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (ui.busy) {
                            CircularProgressIndicator(Modifier.size(17.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(Modifier.width(9.dp))
                        }
                        Text(
                            if (ui.busy) tr("Ranking…", "সাজানো হচ্ছে…")
                            else tr("Add to the queue", "তালিকায় যোগ করুন"),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                        )
                    }
                }
            } else {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(ShapePill)
                        .background(CautionAmber.copy(alpha = 0.13f))
                        .clickable { composing = true }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(FeatherIcons.AlertTriangle, null, tint = CautionAmber, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        tr("Add another case", "আরেকটি কেস যোগ করুন"),
                        color = CautionAmber,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.labelLarge,
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

            // ---- The queue ----
            Spacer(Modifier.height(20.dp))
            if (ui.queue.isEmpty()) {
                EmptyQueue()
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        tr("Go in this order", "এই ক্রমে যান"),
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        tr("Clear all", "সব মুছুন"),
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                        modifier = Modifier
                            .clip(ShapePill)
                            .clickable(enabled = !ui.busy) { viewModel.clearQueue() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
                Spacer(Modifier.height(10.dp))
                ui.queue.forEachIndexed { i, item ->
                    CaseCard(i + 1, item, bangla)
                    Spacer(Modifier.height(9.dp))
                }
            }
        }
    }
}

/** One priority band's count, sized to be read across a room. */
@Composable
private fun Tally(label: String, n: Int, tint: Color, bangla: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(ShapeMd)
            .background(tint.copy(alpha = 0.11f))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            localiseDigits("$n", bangla),
            color = tint,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
        )
        Text(
            label,
            color = tint,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

/**
 * One case on the board.
 *
 * The spine down the left is the whole design: scanning a list of nine, you find the red ones
 * without reading a word. The queue position is on the card too, because "third" is what a
 * volunteer says out loud to the person driving the boat.
 */
@Composable
private fun CaseCard(position: Int, item: TriagedItem, bangla: Boolean) {
    val r = item.result
    val tint = priorityColour(r.priority)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(ShapeMd)
            .background(BgCard),
    ) {
        Box(Modifier.width(6.dp).fillMaxHeight().background(tint))
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(26.dp).clip(CircleShape).background(tint),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        localiseDigits("$position", bangla),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    r.priority.uppercase(),
                    color = tint,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .clip(ShapePill)
                        .background(tint.copy(alpha = 0.13f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
                Spacer(Modifier.weight(1f))
                Text(
                    if (r.producedBy == "gemma") tr("Gemma", "জেমা") else tr("rules", "নিয়ম"),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }

            Spacer(Modifier.height(9.dp))
            Text(item.text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)

            // What to actually do, given its own line and its own icon — it is the one part of
            // the card a volunteer acts on.
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    FeatherIcons.Navigation, null,
                    tint = tint, modifier = Modifier.size(15.dp).padding(top = 2.dp),
                )
                Spacer(Modifier.width(9.dp))
                Text(
                    r.recommendedAction,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            }
            Spacer(Modifier.height(5.dp))
            Text(
                r.rationale,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )

            if (r.riskSignals.isNotEmpty()) {
                Spacer(Modifier.height(9.dp))
                FlowRowSignals(r.riskSignals, tint)
            }
            if (r.needsHumanReview) {
                Spacer(Modifier.height(8.dp))
                Text(
                    tr("A person should check this one.", "একজন মানুষের এটি যাচাই করা উচিত।"),
                    style = MaterialTheme.typography.labelSmall,
                    color = ErrorRed,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowSignals(signals: List<String>, tint: Color) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        signals.forEach {
            Text(
                it,
                style = MaterialTheme.typography.labelSmall,
                color = tint,
                modifier = Modifier
                    .clip(ShapePill)
                    .background(tint.copy(alpha = 0.10f))
                    .padding(horizontal = 9.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun EmptyQueue() {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(56.dp).clip(CircleShape).background(CautionAmber.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(FeatherIcons.Inbox, null, tint = CautionAmber, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(
            tr("No cases yet", "এখনো কোনো কেস নেই"),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            tr(
                "Add what people are telling you, and they get ordered by who is worst off.",
                "মানুষ যা বলছে তা যোগ করুন, কে সবচেয়ে বিপদে আছে সেই অনুযায়ী সাজানো হবে।",
            ),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
    }
}

private fun priorityColour(priority: String): Color = when (priority.lowercase()) {
    "critical" -> ErrorRed
    "high" -> CautionAmber
    "medium" -> BrandTeal
    else -> SafeGreen
}
