package org.nongor.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nongor.app.R
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.BrandTeal
import org.nongor.app.ui.theme.BgCard
import org.nongor.app.ui.theme.BgDark
import org.nongor.app.ui.theme.Divider
import org.nongor.app.ui.theme.ErrorRed
import org.nongor.app.ui.theme.GlassBorder
import org.nongor.app.ui.theme.TextMuted
import org.nongor.app.ui.theme.TextPrimary
import org.nongor.app.ui.theme.TextSecondary
import org.nongor.app.ui.theme.ShapeMd
import org.nongor.app.ui.theme.ShapePill
import org.nongor.app.ui.theme.ShapeSm
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinished: () -> Unit,
    onSkip: () -> Unit,
) {
    val ui by viewModel.ui.collectAsState()
    var consentAccepted by rememberSaveable { mutableStateOf(false) }
    var showLegalInfo by rememberSaveable { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(ui.completed) { if (ui.completed) onFinished() }

    if (showLegalInfo) {
        AlertDialog(
            onDismissRequest = { showLegalInfo = false },
            confirmButton = {
                TextButton(onClick = { showLegalInfo = false }) {
                    Text("Close")
                }
            },
            title = {
                Text(
                    stringResource(R.string.legal_title),
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        stringResource(R.string.legal_intro),
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    ConsentLine(stringResource(R.string.legal_privacy_blurb))
                    ConsentLine(stringResource(R.string.legal_download_blurb))
                    LegalLinkRow(
                        label = stringResource(R.string.about_creator_title),
                        description = stringResource(R.string.about_creator_description),
                        action = stringResource(R.string.legal_open_x),
                    ) { uriHandler.openUri("https://x.com/1littlecoder") }
                    LegalLinkRow(
                        label = stringResource(R.string.about_litert_title),
                        description = stringResource(R.string.about_litert_description),
                        action = stringResource(R.string.legal_open_hf),
                    ) { uriHandler.openUri("https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm") }
                    LegalLinkRow(
                        label = stringResource(R.string.about_gemma_title),
                        description = stringResource(R.string.about_gemma_description),
                        action = stringResource(R.string.legal_open_deepmind),
                    ) { uriHandler.openUri("https://deepmind.google/models/gemma/gemma-4/") }
                    LegalLinkRow(
                        label = stringResource(R.string.about_runtime_title),
                        description = stringResource(R.string.about_runtime_description),
                        action = stringResource(R.string.legal_open_litert),
                    ) { uriHandler.openUri("https://github.com/google-ai-edge/LiteRT-LM/blob/main/docs/api/kotlin/getting_started.md") }
                }
            },
            containerColor = BgCard,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
        )
    }

    // One page, no scrolling. A first-run screen that has to be scrolled hides its own Skip
    // button, and the whole point of this screen is that skipping is the expected path — every
    // feature except the AI already works. Weighted spacers absorb the difference between a
    // tall phone and a short one rather than letting the content run off the bottom.
    Column(
        Modifier
            .fillMaxSize()
            .background(BgDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(0.6f))

        // Flat brand mark. The old screen stacked two radial gradients and a glow orb behind
        // this; on a cheap phone that is three overdrawn layers for decoration.
        Box(
            Modifier.size(76.dp).clip(CircleShape).background(BrandTeal),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painterResource(R.drawable.nongor_mark),
                contentDescription = null,
                modifier = Modifier.size(44.dp),
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(
            tr("Nongor is ready", "নোঙর প্রস্তুত"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            tr(
                "Emergency calls, translation, mesh SOS, shelters, routes and first aid all work " +
                    "right now — no internet, no download.",
                "জরুরি কল, অনুবাদ, মেশ এসওএস, আশ্রয়, পথ ও প্রাথমিক চিকিৎসা এখনই চলে — ইন্টারনেট বা " +
                    "ডাউনলোড ছাড়াই।",
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.weight(0.8f))

        // ---- The optional model ----
        Column(
            Modifier
                .fillMaxWidth()
                .clip(ShapeMd)
                .background(BrandTeal.copy(alpha = 0.08f))
                .padding(16.dp),
        ) {
            Text(
                stringResource(R.string.consent_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(10.dp))
            FactRow(stringResource(R.string.consent_download_size))
            FactRow(stringResource(R.string.consent_ram))
            FactRow(stringResource(R.string.consent_intensive))
            FactRow(stringResource(R.string.consent_offline))

            Spacer(Modifier.height(6.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(ShapeSm)
                    .clickable(enabled = !ui.downloading) { consentAccepted = !consentAccepted },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = consentAccepted,
                    onCheckedChange = if (ui.downloading) null else { c -> consentAccepted = c },
                    enabled = !ui.downloading,
                )
                Text(
                    stringResource(R.string.consent_checkbox),
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        if (ui.lowMemoryWarning) {
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.low_memory_warning),
                color = ErrorRed,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }

        ui.error?.let { err ->
            Spacer(Modifier.height(10.dp))
            Text(
                err,
                color = ErrorRed,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }

        if (ui.downloading) {
            Spacer(Modifier.height(14.dp))
            val cur = ui.progress.first
            val max = ui.progress.second
            val fraction = if (max > 0) cur.toFloat() / max else 0f
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(ShapePill),
                color = BrandTeal,
                trackColor = Divider,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (max > 0) {
                    "${cur / (1024 * 1024)} / ${max / (1024 * 1024)} MB · ${(fraction * 100).toInt()}%"
                } else {
                    stringResource(R.string.downloading_model)
                },
                color = TextSecondary,
                style = MaterialTheme.typography.labelMedium,
            )
        }

        Spacer(Modifier.weight(1f))

        // ---- Actions ----
        // Skip is a real button of equal weight, not a grey link under the download CTA:
        // it is the expected path, not a consolation for people who gave up.
        val canDownload = !ui.downloading && consentAccepted
        Row(
            Modifier
                .fillMaxWidth()
                .clip(ShapePill)
                .background(if (canDownload) BrandTeal else BrandTeal.copy(alpha = 0.30f))
                .clickable(enabled = canDownload) { viewModel.startDownload() }
                .padding(vertical = 15.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (ui.downloading) {
                CircularProgressIndicator(
                    Modifier.size(17.dp), strokeWidth = 2.dp, color = Color.White,
                )
                Spacer(Modifier.size(9.dp))
            }
            Text(
                stringResource(R.string.download_and_continue),
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
            )
        }

        Spacer(Modifier.height(9.dp))
        OutlinedButton(
            onClick = onSkip,
            enabled = !ui.downloading,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = ShapePill,
        ) {
            Text(
                tr("Skip — start using Nongor now", "বাদ দিন — এখনই নোঙর ব্যবহার করুন"),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            )
        }

        Spacer(Modifier.height(2.dp))
        TextButton(onClick = { showLegalInfo = true }, enabled = !ui.downloading) {
            Text(
                stringResource(R.string.review_terms_credits_privacy),
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
            )
        }

        Spacer(Modifier.weight(0.3f))
    }
}

/** One fact about the download: a teal dot and a line, nothing more. */
@Composable
private fun FactRow(text: String) {
    Row(Modifier.padding(bottom = 7.dp), verticalAlignment = Alignment.Top) {
        Box(
            Modifier.padding(top = 6.dp).size(5.dp).clip(CircleShape).background(BrandTeal),
        )
        Text(
            text,
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 10.dp),
        )
    }
}

@Composable
private fun ConsentLine(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(BrandTeal),
        )
        Text(
            text = text,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 10.dp),
        )
    }
}

@Composable
private fun LegalLinkRow(
    label: String,
    description: String,
    action: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeSm)
            .border(1.dp, GlassBorder.copy(alpha = 0.8f), ShapeSm)
            .background(BgDark.copy(alpha = 0.32f))
            .clickable { onClick() }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            label,
            color = TextPrimary,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            description,
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            action,
            color = BrandTeal,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
