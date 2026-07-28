package org.nongor.app.ui.translate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.ChevronRight
import compose.icons.feathericons.PlayCircle
import compose.icons.feathericons.RotateCcw
import compose.icons.feathericons.Search
import compose.icons.feathericons.Send
import org.nongor.app.core.LangInfo
import org.nongor.app.core.Phrase
import org.nongor.app.core.Priority
import org.nongor.app.ui.i18n.LocalBangla
import org.nongor.app.ui.i18n.localiseDigits
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.AccentPurple
import org.nongor.app.ui.theme.BgCard
import org.nongor.app.ui.theme.TextPrimary
import org.nongor.app.ui.theme.TextSecondary
import org.nongor.app.ui.theme.NongorColors

/**
 * Emergency translation.
 *
 * The volunteer speaks Bangla; the person in front of them may speak Chakma, Marma,
 * Rohingya, Kokborok, Santali or Garo, or may be Deaf. No offline translator covers those
 * languages, so this screen does not pretend to be one. It gives the volunteer a fixed set
 * of the questions that actually matter in a rescue, shows each one as a pictogram the other
 * person can answer by tapping, and adds a written line in their language wherever the
 * community phrasebook has one — clearly marked when it has not been verified.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslateScreen(
    viewModel: TranslateViewModel,
    onBack: () -> Unit,
    onSendAsSos: (String) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val bangla = LocalBangla.current
    val book = viewModel.book
    var open by remember { mutableStateOf<Phrase?>(null) }

    val guided = state.guidedStep
    val guidedPhrase = viewModel.guidedPhrase()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Emergency Translation", "জরুরি অনুবাদ")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(FeatherIcons.ArrowLeft, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.replies.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearConversation() }) {
                            Icon(
                                FeatherIcons.RotateCcw,
                                contentDescription = tr("Start over", "নতুন করে শুরু"),
                            )
                        }
                    }
                },
            )
        },
    ) { pad ->
        LazyColumn(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 18.dp, end = 18.dp, top = 4.dp, bottom = 28.dp,
            ),
        ) {
            item {
                Text(
                    tr(
                        "Pick the language they speak. If it has no written phrases yet, the " +
                            "pictures and the yes/no buttons still work.",
                        "তিনি যে ভাষায় কথা বলেন সেটি বেছে নিন। লেখা অনুবাদ না থাকলেও ছবি ও " +
                            "হ্যাঁ/না বোতাম কাজ করবে।",
                    ),
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(10.dp))
                LanguageRow(
                    languages = book.targetLanguages(),
                    selected = state.targetLang,
                    coverage = { book.coverage(it) },
                    bangla = bangla,
                    onSelect = { viewModel.setTargetLanguage(it) },
                )
                Spacer(Modifier.height(14.dp))
            }

            item {
                GuidedCard(
                    active = guided != null,
                    answered = state.replies.size,
                    total = book.triageFlow.size,
                    bangla = bangla,
                    onStart = {
                        viewModel.startGuided()
                        open = viewModel.guidedPhrase()
                    },
                    onResume = { open = guidedPhrase },
                )
                Spacer(Modifier.height(14.dp))
            }

            if (state.replies.isNotEmpty()) {
                item {
                    HandoverCard(
                        note = viewModel.handoverNote(bangla),
                        priority = viewModel.assessment().priority,
                        bangla = bangla,
                        onSend = { onSendAsSos(viewModel.handoverNote(bangla)) },
                    )
                    Spacer(Modifier.height(14.dp))
                }
            }

            item {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = { viewModel.setQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(FeatherIcons.Search, contentDescription = null) },
                    placeholder = {
                        Text(tr("Search — bleeding, water, child…", "খুঁজুন — রক্ত, পানি, শিশু…"))
                    },
                )
                Spacer(Modifier.height(12.dp))
            }

            if (state.query.isNotBlank()) {
                if (state.results.isEmpty()) {
                    item {
                        Text(
                            tr("No phrase matches that.", "এমন কোনো বাক্য নেই।"),
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                } else {
                    items(state.results) { phrase ->
                        PhraseRow(phrase, bangla, state.replies[phrase.id] != null) { open = phrase }
                    }
                }
            } else {
                book.categories.forEach { category ->
                    val phrases = book.inCategory(category.id)
                    if (phrases.isEmpty()) return@forEach
                    item(key = "cat_${category.id}") {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (bangla) category.bn else category.en,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = 6.dp),
                        )
                    }
                    items(phrases, key = { it.id }) { phrase ->
                        PhraseRow(phrase, bangla, state.replies[phrase.id] != null) { open = phrase }
                    }
                }
            }
        }
    }

    val showing = open
    if (showing != null) {
        Dialog(
            onDismissRequest = { open = null; viewModel.stopGuided() },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            val step = state.guidedStep
            PhraseCard(
                book = book,
                phrase = showing,
                target = viewModel.targetLanguage(),
                recorded = state.replies[showing.id],
                speaker = viewModel.speaker,
                onReply = { viewModel.record(it) },
                onClose = { open = null; viewModel.stopGuided() },
                guidedProgress = step?.let { it + 1 to book.triageFlow.size },
                onNext = if (step != null) {
                    {
                        viewModel.advanceGuided()
                        open = viewModel.guidedPhrase()
                    }
                } else {
                    null
                },
                onPrevious = if (step != null && step > 0) {
                    {
                        viewModel.backGuided()
                        open = viewModel.guidedPhrase()
                    }
                } else {
                    null
                },
            )
        }
    }
}

@Composable
private fun LanguageRow(
    languages: List<LangInfo>,
    selected: String?,
    coverage: (String) -> Int,
    bangla: Boolean,
    onSelect: (String?) -> Unit,
) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        languages.forEach { lang ->
            val isSelected = lang.code == selected
            val written = coverage(lang.code)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) AccentPurple else BgCard,
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.clickable { onSelect(if (isSelected) null else lang.code) },
            ) {
                Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        lang.native,
                        color = if (isSelected) Color.White else TextPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        when {
                            lang.isGesture -> if (bangla) "ইশারা ও ছবি" else "Gestures & pictures"
                            written == 0 -> if (bangla) "ছবি দিয়ে" else "Pictures only"
                            else -> localiseDigits(
                                if (bangla) "$written শব্দ লেখা আছে" else "$written written",
                                bangla,
                            )
                        },
                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else TextSecondary,
                        fontSize = 11.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun GuidedCard(
    active: Boolean,
    answered: Int,
    total: Int,
    bangla: Boolean,
    onStart: () -> Unit,
    onResume: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AccentPurple.copy(alpha = 0.10f))
            .clickable { if (active) onResume() else onStart() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(36.dp).clip(CircleShape).background(AccentPurple),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                FeatherIcons.PlayCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                tr("Guided rescue questions", "ধাপে ধাপে উদ্ধার প্রশ্ন"),
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                localiseDigits(
                    tr(
                        "$total questions that build a handover note — $answered answered",
                        "$total টি প্রশ্ন, শেষে হস্তান্তর নোট — $answered টির উত্তর হয়েছে",
                    ),
                    bangla,
                ),
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Icon(FeatherIcons.ChevronRight, contentDescription = null, tint = TextSecondary)
    }
}

/**
 * The note a responder actually carries away.
 *
 * Every line here is a question the person answered by tapping — nothing is inferred or
 * reworded, so a medic reading it downstream is reading the person's own answers.
 */
@Composable
private fun HandoverCard(
    note: String,
    priority: Priority,
    bangla: Boolean,
    onSend: () -> Unit,
) {
    val tint = when (priority) {
        Priority.CRITICAL -> NongorColors.Danger
        Priority.HIGH -> NongorColors.Caution
        Priority.MODERATE -> NongorColors.Surf
        Priority.LOW -> NongorColors.Safe
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = BgCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(tint.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        if (bangla) priority.labelBn() else priority.labelEn(),
                        color = tint,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    )
                }
                Spacer(Modifier.weight(1f))
                Row(
                    Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(AccentPurple)
                        .clickable(onClick = onSend)
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        FeatherIcons.Send,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        tr("Send over mesh", "মেশে পাঠান"),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(note, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun PhraseRow(phrase: Phrase, bangla: Boolean, answered: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(BgCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (answered) {
                        NongorColors.Safe.copy(alpha = 0.16f)
                    } else {
                        AccentPurple.copy(alpha = 0.10f)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                phraseIcon(phrase.icon),
                contentDescription = null,
                tint = if (answered) NongorColors.Safe else AccentPurple,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            if (bangla) phrase.bn else phrase.en,
            color = TextPrimary,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Icon(
            FeatherIcons.ChevronRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp),
        )
    }
}
