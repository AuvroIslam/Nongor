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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nongor.app.R
import org.nongor.app.ui.theme.AccentGlow
import org.nongor.app.ui.theme.AccentPurple
import org.nongor.app.ui.theme.AccentViolet
import org.nongor.app.ui.theme.BgCard
import org.nongor.app.ui.theme.BgDark
import org.nongor.app.ui.theme.BgMid
import org.nongor.app.ui.theme.Divider
import org.nongor.app.ui.theme.ErrorRed
import org.nongor.app.ui.theme.GlassBg
import org.nongor.app.ui.theme.GlassBorder
import org.nongor.app.ui.theme.TextMuted
import org.nongor.app.ui.theme.TextPrimary
import org.nongor.app.ui.theme.TextSecondary

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

    Box(
        Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(listOf(BgDark, BgMid, BgDark)),
            ),
    ) {
        // Top glow
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .size(360.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentPurple.copy(alpha = 0.2f),
                            AccentGlow.copy(alpha = 0.05f),
                            Color.Transparent,
                        ),
                    ),
                    shape = CircleShape,
                ),
        )

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Glow orb logo
            Box(contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(140.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(AccentPurple.copy(alpha = 0.35f), AccentGlow.copy(alpha = 0.1f), Color.Transparent),
                            ),
                            shape = CircleShape,
                        ),
                )
                Box(
                    Modifier
                        .size(88.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(AccentPurple, AccentViolet),
                                start = Offset.Zero,
                                end = Offset(88f, 88f),
                            ),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("G", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                stringResource(R.string.onboarding_title),
                style = MaterialTheme.typography.displayLarge,
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.onboarding_body),
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
                    .background(GlassBg)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    stringResource(R.string.consent_title),
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                ConsentLine(stringResource(R.string.consent_download_size))
                ConsentLine(stringResource(R.string.consent_ram))
                ConsentLine(stringResource(R.string.consent_intensive))
                ConsentLine(stringResource(R.string.consent_offline))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !ui.downloading) { consentAccepted = !consentAccepted }
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Checkbox(
                        checked = consentAccepted,
                        onCheckedChange = if (ui.downloading) null else { checked -> consentAccepted = checked },
                        enabled = !ui.downloading,
                    )
                    Text(
                        text = stringResource(R.string.consent_checkbox),
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 12.dp, start = 4.dp),
                    )
                }
            }

            if (ui.lowMemoryWarning) {
                Spacer(Modifier.height(16.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ErrorRed.copy(alpha = 0.12f))
                        .border(1.dp, ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                ) {
                    Text(
                        stringResource(R.string.low_memory_warning),
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            ui.error?.let { err ->
                Spacer(Modifier.height(16.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ErrorRed.copy(alpha = 0.12f))
                        .border(1.dp, ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                ) {
                    Text(err, color = ErrorRed, style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (ui.downloading) {
                Spacer(Modifier.height(28.dp))
                val cur = ui.progress.first
                val max = ui.progress.second
                val fraction = if (max > 0) cur.toFloat() / max else 0f
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = AccentViolet,
                    trackColor = Divider,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (max > 0) "${cur / (1024 * 1024)} / ${max / (1024 * 1024)} MB · ${(fraction * 100).toInt()}%"
                    else stringResource(R.string.downloading_model),
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            Spacer(Modifier.height(36.dp))

            Button(
                onClick = { viewModel.startDownload() },
                enabled = !ui.downloading && consentAccepted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentPurple,
                    contentColor = Color.White,
                    disabledContainerColor = BgCard,
                    disabledContentColor = TextMuted,
                ),
            ) {
                if (ui.downloading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = TextSecondary,
                    )
                } else {
                    Text(
                        stringResource(R.string.download_and_continue),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                if (!consentAccepted && !ui.downloading) {
                    stringResource(R.string.consent_required)
                } else {
                    "~2.5 GB · Wi-Fi recommended · Runs fully offline"
                },
                color = TextMuted,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            TextButton(
                onClick = { showLegalInfo = true },
                enabled = !ui.downloading,
            ) {
                Text(stringResource(R.string.review_terms_credits_privacy))
            }
            TextButton(
                onClick = onSkip,
                enabled = !ui.downloading,
            ) {
                Text("Continue without the model (mesh & maps)")
            }
        }
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
                .background(AccentViolet),
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
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, GlassBorder.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
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
            color = AccentViolet,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
