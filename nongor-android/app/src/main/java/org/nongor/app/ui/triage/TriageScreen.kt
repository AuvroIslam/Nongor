package org.nongor.app.ui.triage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.Camera
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.nongor.app.R
import org.nongor.app.ui.components.HeroBanner
import org.nongor.app.ui.i18n.LocalBangla
import org.nongor.app.ui.i18n.tr
import compose.icons.feathericons.AlertTriangle
import org.nongor.app.ui.theme.TileTriageFg

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriageScreen(viewModel: TriageViewModel, onBack: () -> Unit) {
    val ui by viewModel.ui.collectAsState()
    var text by remember { mutableStateOf("") }
    val examples = if (LocalBangla.current) EXAMPLES_BN else EXAMPLES_EN
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.setImageFromUri(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Nongor · Rescue Triage", "নোঙর · উদ্ধার ট্রায়াজ")) },
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
            HeroBanner(FeatherIcons.AlertTriangle, tint = TileTriageFg,
                title = tr("Rescue Triage", "উদ্ধার ট্রায়াজ"),
                subtitle = tr("Prioritise SOS by urgency", "জরুরিতা অনুযায়ী এসওএস সাজান"))
            Spacer(Modifier.height(12.dp))
            // engine status line
            when {
                ui.engineLoading -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.height(18.dp).width(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(tr("Loading on-device Gemma 4…", "ডিভাইসে Gemma 4 লোড হচ্ছে…"),
                        style = MaterialTheme.typography.bodySmall)
                }
                ui.engineReady -> Text(
                    tr("● On-device Gemma 4 ready — triage runs offline.",
                        "● ডিভাইসে Gemma 4 প্রস্তুত — ট্রায়াজ অফলাইনে চলে।"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                else -> Text(
                    tr("Model not loaded — using rule-based fallback.",
                        "মডেল লোড হয়নি — নিয়মভিত্তিক বিকল্প ব্যবহার করা হচ্ছে।"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(tr("Describe the SOS", "এসওএস লিখুন")) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
            )

            Spacer(Modifier.height(8.dp))
            Text(tr("Common situations", "সাধারণ পরিস্থিতি"),
                style = MaterialTheme.typography.labelMedium)
            examples.forEach { ex ->
                AssistChip(
                    onClick = { text = ex },
                    label = { Text(ex.take(42) + "…", maxLines = 1) },
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Icon(FeatherIcons.Camera, null, modifier = Modifier.width(18.dp).height(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(tr("Attach photo", "ছবি যুক্ত করুন"))
                }
                if (ui.imagePath != null) {
                    Spacer(Modifier.width(8.dp))
                    Text(tr("attached ✓", "যুক্ত হয়েছে ✓"), color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { viewModel.clearImage() }) {
                        Text(tr("remove", "সরান"))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row {
                Button(
                    onClick = { viewModel.triage(text) },
                    enabled = !ui.busy && (text.isNotBlank() || ui.imagePath != null),
                ) {
                    if (ui.busy) {
                        CircularProgressIndicator(Modifier.height(18.dp).width(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(tr("Triaging on-device…", "ডিভাইসে ট্রায়াজ হচ্ছে…"))
                    } else {
                        Text(tr("Run Triage", "ট্রায়াজ করুন"))
                    }
                }
                Spacer(Modifier.width(8.dp))
                if (ui.queue.isNotEmpty()) {
                    OutlinedButton(onClick = { viewModel.clearQueue() }, enabled = !ui.busy) {
                        Text(tr("Clear", "মুছুন"))
                    }
                }
            }

            ui.error?.let {
                Spacer(Modifier.height(8.dp))
                Text("${tr("Error", "সমস্যা")}: $it", color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            Text(
                "${tr("Prioritized queue", "অগ্রাধিকার তালিকা")} (${ui.queue.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ui.queue.forEach { item -> TriageCard(item) }
            }
        }
    }
}

@Composable
private fun TriageCard(item: TriagedItem) {
    val r = item.result
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(r.color)
                Spacer(Modifier.width(8.dp))
                Text(r.priority.uppercase(), fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text("score ${"%.2f".format(r.urgencyScore)}", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (r.producedBy == "gemma") "Gemma" else "fallback",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(item.text, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text("Why: ${r.rationale}", style = MaterialTheme.typography.bodySmall)
            Text("Action: ${r.recommendedAction}", style = MaterialTheme.typography.bodySmall)
            if (r.riskSignals.isNotEmpty()) {
                Text("Signals: ${r.riskSignals.joinToString(", ")}",
                    style = MaterialTheme.typography.labelSmall)
            }
            if (r.needsHumanReview) {
                Text("⚠ Needs human review", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
