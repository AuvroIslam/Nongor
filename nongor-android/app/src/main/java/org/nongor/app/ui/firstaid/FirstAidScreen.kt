package org.nongor.app.ui.firstaid

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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.Camera
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.nongor.app.R
import org.nongor.app.ui.components.HeroBanner
import org.nongor.app.ui.i18n.LocalBangla
import org.nongor.app.ui.i18n.tr
import compose.icons.feathericons.Activity
import org.nongor.app.ui.theme.TileAidFg
import compose.icons.feathericons.AlertOctagon
import compose.icons.feathericons.PhoneCall
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import org.nongor.app.ui.emergency.dialNumber
import org.nongor.app.ui.theme.ErrorRed
import org.nongor.app.ui.theme.ShapeMd
import org.nongor.app.ui.theme.ShapeSm
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstAidScreen(viewModel: FirstAidViewModel, onBack: () -> Unit) {
    val ui by viewModel.ui.collectAsState()
    var text by remember { mutableStateOf("") }
    val examples = if (LocalBangla.current) EXAMPLES_BN else EXAMPLES_EN
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.setImageFromUri(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Nongor · First Aid", "নোঙর · প্রাথমিক চিকিৎসা")) },
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
            HeroBanner(FeatherIcons.Activity, tint = TileAidFg,
                title = tr("First Aid", "প্রাথমিক চিকিৎসা"),
                subtitle = tr("Trusted, cited guidance", "নির্ভরযোগ্য, উৎসসহ পরামর্শ"))
            Spacer(Modifier.height(12.dp))
            when {
                ui.engineLoading -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.height(18.dp).width(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(tr("Loading on-device Gemma 4…", "ডিভাইসে Gemma 4 লোড হচ্ছে…"),
                        style = MaterialTheme.typography.bodySmall)
                }
                ui.engineReady -> Text(
                    tr("Grounded in offline first aid guidance (WHO, IFRC, Red Cross).",
                        "অফলাইন প্রাথমিক চিকিৎসা নির্দেশনার (WHO, IFRC, Red Cross) ভিত্তিতে।"),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                else -> Text(tr("Model not loaded — showing the source passages directly.",
                    "মডেল লোড হয়নি — সরাসরি উৎস অনুচ্ছেদ দেখানো হচ্ছে।"),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = text, onValueChange = { text = it },
                label = { Text(tr("Describe the injury / situation", "আঘাত / পরিস্থিতি লিখুন")) },
                modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4,
            )
            Spacer(Modifier.height(8.dp))
            Text(tr("Common situations", "সাধারণ পরিস্থিতি"),
                style = MaterialTheme.typography.labelMedium)
            examples.forEach { ex ->
                AssistChip(onClick = { text = ex },
                    label = { Text(ex.take(40) + "…", maxLines = 1) },
                    modifier = Modifier.padding(vertical = 2.dp))
            }

            Spacer(Modifier.height(8.dp))
            // Optional photo: helps Gemma see the injury; the written steps still come from the
            // grounded passages retrieved from the text description.
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
            Button(onClick = { viewModel.ask(text) },
                enabled = !ui.busy && (text.isNotBlank() || ui.imagePath != null)) {
                if (ui.busy) {
                    CircularProgressIndicator(Modifier.height(18.dp).width(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(tr("Thinking on-device…", "ডিভাইসে ভাবছে…"))
                } else {
                    Text(tr("Get First Aid Steps", "প্রাথমিক চিকিৎসার ধাপ দিন"))
                }
            }

            ui.error?.let {
                Spacer(Modifier.height(8.dp))
                Text("${tr("Error", "সমস্যা")}: $it", color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }

            if (ui.redFlag) {
                Spacer(Modifier.height(12.dp))
                LifeThreatBanner()
            }

            ui.answer?.let { ans ->
                Spacer(Modifier.height(12.dp))
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(ans, style = MaterialTheme.typography.bodyMedium)
                        if (ui.citations.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            Text(tr("Sources:", "উৎস:"), style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold)
                            ui.citations.forEach { c ->
                                Text("[${c.n}] ${c.source} · ${c.pack}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

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
