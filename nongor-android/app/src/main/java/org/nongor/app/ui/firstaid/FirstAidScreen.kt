package org.nongor.app.ui.firstaid

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.FeatherIcons
import compose.icons.feathericons.AlertOctagon
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.Camera
import compose.icons.feathericons.CheckCircle
import compose.icons.feathericons.PhoneCall
import compose.icons.feathericons.X
import org.nongor.app.R
import org.nongor.app.ui.emergency.dialNumber
import org.nongor.app.ui.i18n.LocalBangla
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.AidRose
import org.nongor.app.ui.theme.BgCard
import org.nongor.app.ui.theme.ErrorRed
import org.nongor.app.ui.theme.ShapeMd
import org.nongor.app.ui.theme.ShapePill
import org.nongor.app.ui.theme.ShapeSm
import org.nongor.app.ui.theme.TextPrimary
import org.nongor.app.ui.theme.TextSecondary

private val EXAMPLES_EN = listOf(
    "Someone is bleeding heavily from a deep cut on the leg.",
    "We pulled someone from the floodwater and they are not breathing.",
    "A snake bit my brother's foot near the water.",
    "A child swallowed floodwater and is vomiting.",
)

private val EXAMPLES_BN = listOf(
    "পায়ে গভীর কাটা থেকে প্রচুর রক্তক্ষরণ হচ্ছে।",
    "বন্যার পানি থেকে তুলে আনা একজন শ্বাস নিচ্ছে না।",
    "পানির কাছে আমার ভাইয়ের পায়ে সাপে কামড়েছে।",
    "একটি শিশু বন্যার পানি গিলে বমি করছে।",
)

/** The short label a chip carries, so the tap target is not a wrapped paragraph. */
private val CHIPS_EN = listOf("Heavy bleeding", "Not breathing", "Snake bite", "Swallowed water")
private val CHIPS_BN = listOf("প্রচুর রক্তক্ষরণ", "শ্বাস নিচ্ছে না", "সাপের কামড়", "পানি গিলেছে")

/**
 * First Aid.
 *
 * The answer is the screen. Everything above it — the question box, the four common
 * situations, the photo — exists only to produce a list of steps someone can follow with one
 * hand while the other is holding a wound closed, so the steps are rendered as separate
 * numbered cards rather than a paragraph. In a paragraph you lose your place; on a card you
 * do not.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FirstAidScreen(viewModel: FirstAidViewModel, onBack: () -> Unit) {
    val ui by viewModel.ui.collectAsState()
    var text by remember { mutableStateOf("") }
    val bangla = LocalBangla.current
    val examples = if (bangla) EXAMPLES_BN else EXAMPLES_EN
    val chips = if (bangla) CHIPS_BN else CHIPS_EN
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.setImageFromUri(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("First Aid", "প্রাথমিক চিকিৎসা")) },
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
            // ---- Ask ----
            // The composer is one rose block: the question, the shortcuts, the photo and the
            // button all inside it, so it reads as a single thing you fill in and send.
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(ShapeMd)
                    .background(AidRose.copy(alpha = 0.08f))
                    .padding(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(38.dp).clip(CircleShape).background(AidRose.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_firstaid),
                            contentDescription = null,
                            tint = AidRose,
                            modifier = Modifier.size(21.dp),
                        )
                    }
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            tr("What happened?", "কী হয়েছে?"),
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                        )
                        Text(
                            statusLine(ui.engineLoading, ui.engineReady),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
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
                            tr("Describe the injury in your own words", "নিজের ভাষায় আঘাতের কথা লিখুন"),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BgCard,
                        unfocusedContainerColor = BgCard,
                        focusedIndicatorColor = AidRose,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = AidRose,
                    ),
                )

                // Four taps that cover most of what a flood produces. These were a vertical
                // stack of chips each holding a full truncated sentence, which read as a list
                // of things gone wrong rather than a set of shortcuts.
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
                            color = AidRose,
                            modifier = Modifier
                                .clip(ShapePill)
                                .background(BgCard)
                                .clickable { text = examples[i] }
                                .padding(horizontal = 13.dp, vertical = 8.dp),
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                            Icon(FeatherIcons.Camera, null, tint = AidRose, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(7.dp))
                            Text(
                                tr("Add a photo", "ছবি দিন"),
                                style = MaterialTheme.typography.labelLarge,
                                color = AidRose,
                            )
                        }
                    } else {
                        Row(
                            Modifier
                                .clip(ShapePill)
                                .background(AidRose.copy(alpha = 0.16f))
                                .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                FeatherIcons.CheckCircle, null,
                                tint = AidRose, modifier = Modifier.size(15.dp),
                            )
                            Spacer(Modifier.width(7.dp))
                            Text(
                                tr("Photo attached", "ছবি যুক্ত"),
                                style = MaterialTheme.typography.labelLarge,
                                color = AidRose,
                            )
                            IconButton(
                                onClick = { viewModel.clearImage() },
                                modifier = Modifier.size(26.dp),
                            ) {
                                Icon(FeatherIcons.X, null, tint = AidRose, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                val canAsk = !ui.busy && (text.isNotBlank() || ui.imagePath != null)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(ShapePill)
                        .background(if (canAsk) AidRose else AidRose.copy(alpha = 0.35f))
                        .clickable(enabled = canAsk) { viewModel.ask(text) }
                        .padding(vertical = 13.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (ui.busy) {
                        CircularProgressIndicator(
                            Modifier.size(17.dp), strokeWidth = 2.dp, color = Color.White,
                        )
                        Spacer(Modifier.width(9.dp))
                    }
                    Text(
                        if (ui.busy) tr("Working on this phone…", "এই ফোনেই কাজ হচ্ছে…")
                        else tr("Show me the steps", "ধাপগুলো দেখান"),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
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

            if (ui.redFlag) {
                Spacer(Modifier.height(14.dp))
                LifeThreatBanner()
            }

            // ---- Answer ----
            ui.answer?.let { ans ->
                val parsed = remember(ans) { parseAnswer(ans) }
                Spacer(Modifier.height(18.dp))
                Text(
                    tr("Do this, in order", "এই ক্রমে করুন"),
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(10.dp))
                parsed.steps.forEachIndexed { i, step ->
                    StepCard(i + 1, step)
                    Spacer(Modifier.height(8.dp))
                }
                parsed.closing?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }

                if (ui.citations.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        tr("Where this comes from", "এটি কোথা থেকে এসেছে"),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.height(6.dp))
                    ui.citations.forEach { c ->
                        Row(
                            Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "${c.n}",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.labelSmall,
                                color = AidRose,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(AidRose.copy(alpha = 0.14f))
                                    .padding(horizontal = 7.dp, vertical = 3.dp),
                            )
                            Spacer(Modifier.width(9.dp))
                            Text(
                                "${c.source} · ${c.pack}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun statusLine(loading: Boolean, ready: Boolean): String = when {
    loading -> tr("Loading the on-device model…", "ডিভাইসের মডেল লোড হচ্ছে…")
    ready -> tr("Answers from WHO and IFRC guidance, offline", "WHO ও IFRC নির্দেশনা থেকে, অফলাইনে")
    else -> tr("Showing the source passages directly", "সরাসরি উৎস অনুচ্ছেদ দেখানো হচ্ছে")
}

/** One step, big enough to read at arm's length with the phone on the floor. */
@Composable
private fun StepCard(number: Int, text: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(ShapeMd)
            .background(BgCard)
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            Modifier.size(28.dp).clip(CircleShape).background(AidRose),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "$number",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

/** The model's answer, split into the steps it was asked to produce plus the closing caveat. */
private data class ParsedAnswer(val steps: List<String>, val closing: String?)

/**
 * Turn the model's prose into discrete steps.
 *
 * The prompt asks for ordered, individually cited steps, and the model complies in one of a
 * few shapes: "1. …", "- …", or plain lines. All three are handled, and anything that does
 * not look like a step at all falls back to a single card — a wrong-looking list is worse
 * than one honest block of text.
 */
private fun parseAnswer(answer: String): ParsedAnswer {
    val lines = answer.trim().lines().map { it.trim() }.filter { it.isNotEmpty() }
    if (lines.isEmpty()) return ParsedAnswer(listOf(answer.trim()), null)

    // The mandated closing sentence is guidance about the guidance, not a step.
    val closingIdx = lines.indexOfLast { it.contains("not a substitute", ignoreCase = true) }
    val closing = closingIdx.takeIf { it >= 0 }?.let { lines[it] }
    val body = if (closingIdx >= 0) lines.filterIndexed { i, _ -> i != closingIdx } else lines

    val steps = body
        .map { it.removePrefix("*").removePrefix("-").removePrefix("•").trim() }
        .map { it.replace(STEP_NUMBER, "") }
        .filter { it.isNotEmpty() }

    return if (steps.size <= 1) ParsedAnswer(listOf(body.joinToString(" ")), closing)
    else ParsedAnswer(steps, closing)
}

private val STEP_NUMBER = Regex("^\\d+[.)]\\s*")

/**
 * Shown when the question contains a red flag — not breathing, heavy bleeding, unresponsive.
 *
 * Two deliberate choices. It uses a drawn octagon rather than a siren emoji, because the
 * emoji renders as a grey box on some of the cheap handsets this app is aimed at, and a
 * missing glyph on the life-threat warning is the worst possible place for one.
 *
 * And it carries the call button itself. Telling someone "seek emergency help now" and then
 * making them find their way back to a different screen to do it is the sort of thing that
 * reads fine in a design review and costs minutes in a real one.
 */
@Composable
private fun LifeThreatBanner() {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxWidth()
            .clip(ShapeMd)
            .background(ErrorRed)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    FeatherIcons.AlertOctagon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    tr("Life-threatening", "জীবন-সংকটজনক"),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    tr("Get emergency help now.", "এখনই জরুরি সাহায্য নিন।"),
                    color = Color.White.copy(alpha = 0.92f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(ShapeSm)
                .background(Color.White)
                .clickable { dialNumber(context, "999") }
                .padding(vertical = 11.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                FeatherIcons.PhoneCall,
                contentDescription = null,
                tint = ErrorRed,
                modifier = Modifier.size(17.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                tr("Call 999", "৯৯৯-এ কল করুন"),
                color = ErrorRed,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}
