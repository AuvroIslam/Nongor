package org.nongor.app.ui.translate

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SignLanguage
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nongor.app.core.LangInfo
import org.nongor.app.core.Phrase
import org.nongor.app.core.PhrasebookData
import org.nongor.app.core.Reply
import org.nongor.app.core.ReplyKind
import org.nongor.app.core.Translation
import org.nongor.app.ui.i18n.LocalBangla
import org.nongor.app.ui.i18n.t
import org.nongor.app.ui.theme.NongorColors
import org.nongor.app.ui.theme.ShapeMd
import org.nongor.app.ui.theme.ShapePill

/**
 * The hand-over card.
 *
 * The screen is split in two and the upper half is drawn upside down, because the way this
 * actually gets used is: the volunteer holds the phone out flat and the other person reads
 * their half across the table. Nobody has to snatch the phone back and forth, and both
 * people can see what was asked and what was answered at the same time.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhraseCard(
    book: PhrasebookData,
    phrase: Phrase,
    target: LangInfo?,
    recorded: Reply?,
    speaker: Speaker,
    onReply: (Reply) -> Unit,
    onClose: () -> Unit,
    guidedProgress: Pair<Int, Int>? = null,
    onNext: (() -> Unit)? = null,
    onPrevious: (() -> Unit)? = null,
) {
    val bangla = LocalBangla.current
    val translation = target?.let { phrase.translations[it.code] }
    val options = remember(phrase.id) { replyOptions(book, phrase) }
    var typed by remember(phrase.id) { mutableStateOf(recorded?.en.orEmpty()) }
    var showGesture by remember(phrase.id) { mutableStateOf(target?.isGesture == true) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // ── Their half, drawn upside down so it faces the person opposite ──────────
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .rotate(180f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    phraseIcon(phrase.icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(64.dp),
                )
                Spacer(Modifier.height(10.dp))

                TheirLine(phrase = phrase, translation = translation, target = target)

                if (options.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        options.forEach { option ->
                            val selected = recorded?.code == option.code
                            ReplyButton(
                                label = if (bangla) option.bn else option.en,
                                selected = selected,
                                onClick = {
                                    onReply(
                                        Reply(
                                            phraseId = phrase.id,
                                            kind = phrase.replyKind,
                                            code = option.code,
                                            en = option.en,
                                            bn = option.bn,
                                        ),
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }

        // ── The seam ──────────────────────────────────────────────────────────────
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Filled.SwapVert,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.size(6.dp))
            Text(
                t("Lay the phone flat between you", "ফোনটি দুজনের মাঝে সমতলে রাখুন"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // ── Your half ─────────────────────────────────────────────────────────────
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    if (guidedProgress != null) {
                        Text(
                            t(
                                "Question ${guidedProgress.first} of ${guidedProgress.second}",
                                "প্রশ্ন ${guidedProgress.first} / ${guidedProgress.second}",
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        if (bangla) phrase.bn else phrase.en,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
                if (speaker.canSpeak(bangla)) {
                    IconButton(onClick = { speaker.speak(if (bangla) phrase.bn else phrase.en, bangla) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = t("Read aloud", "পড়ে শোনান"),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = t("Close", "বন্ধ"))
                }
            }

            Spacer(Modifier.height(8.dp))

            if (recorded != null) {
                Surface(
                    shape = ShapeMd,
                    color = NongorColors.Safe.copy(alpha = 0.14f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        t(
                            "Answered: ${recorded.en}",
                            "উত্তর: ${recorded.bn}",
                        ),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = NongorColors.Safe,
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            if (phrase.replyKind == ReplyKind.TEXT) {
                OutlinedTextField(
                    value = typed,
                    onValueChange = { value ->
                        typed = value
                        onReply(
                            Reply(
                                phraseId = phrase.id,
                                kind = ReplyKind.TEXT,
                                code = "text",
                                en = value,
                                bn = value,
                            ),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(t("Write down the answer", "উত্তর লিখে রাখুন")) },
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
            }

            if (phrase.signBn != null) {
                Surface(
                    shape = ShapeMd,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showGesture = !showGesture },
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.SignLanguage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(
                                t("Gesture to use (BdSL-style)", "যে ইশারা করবেন (বিডিএসএল ধাঁচে)"),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        if (showGesture) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                phrase.signBn,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (onNext != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onPrevious != null) {
                        OutlinedButton(onClick = onPrevious, modifier = Modifier.weight(1f)) {
                            Text(t("Back", "পিছনে"))
                        }
                    }
                    Button(onClick = onNext, modifier = Modifier.weight(1.4f)) {
                        Text(t("Next question", "পরের প্রশ্ন"))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

/** What the other person reads — their language when we have it, Bangla when we do not. */
@Composable
private fun TheirLine(phrase: Phrase, translation: Translation?, target: LangInfo?) {
    val line = translation?.beng?.takeIf { it.isNotBlank() }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (line != null) {
            Text(
                line,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            translation.latn?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                )
            }
            if (!translation.isVerified) {
                Spacer(Modifier.height(6.dp))
                UnverifiedBand()
            }
            Spacer(Modifier.height(8.dp))
        }
        // Bangla always shows: it is the one line we know is right, and in most of
        // Bangladesh at least one person in the group reads it.
        Text(
            phrase.bn,
            style = if (line == null) {
                MaterialTheme.typography.headlineMedium
            } else {
                MaterialTheme.typography.titleMedium
            },
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                alpha = if (line == null) 1f else 0.7f,
            ),
        )
        if (line == null && target != null && !target.isGesture) {
            Spacer(Modifier.height(8.dp))
            Text(
                "${target.native} — অনুবাদ নেই, ছবি দেখে বুঝুন",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun UnverifiedBand() {
    Surface(shape = ShapePill, color = NongorColors.Caution.copy(alpha = 0.20f)) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = NongorColors.Caution,
                modifier = Modifier.size(13.dp),
            )
            Spacer(Modifier.size(5.dp))
            Text(
                "যাচাই হয়নি · unverified",
                style = MaterialTheme.typography.labelLarge,
                color = NongorColors.Caution,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun ReplyButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = ShapeMd,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) NongorColors.Safe else MaterialTheme.colorScheme.surface,
            contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
        ),
        modifier = Modifier.border(
            width = 1.5.dp,
            color = if (selected) NongorColors.Safe else MaterialTheme.colorScheme.outline,
            shape = ShapeMd,
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 18.dp,
            vertical = 12.dp,
        ),
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}
