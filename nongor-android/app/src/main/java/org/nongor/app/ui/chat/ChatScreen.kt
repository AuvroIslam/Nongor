package org.nongor.app.ui.chat

import android.Manifest
import android.graphics.Bitmap
import android.media.MediaRecorder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.onFocusChanged
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.nongor.app.R
import org.nongor.app.actions.AppActionLauncher
import org.nongor.app.actions.AssistantAction
import org.nongor.app.ui.theme.BrandBlueGlow
import org.nongor.app.ui.theme.BrandBlue
import org.nongor.app.ui.theme.BrandTeal
import org.nongor.app.ui.theme.BgCard
import org.nongor.app.ui.theme.BgDark
import org.nongor.app.ui.theme.BgMid
import org.nongor.app.ui.theme.Divider
import org.nongor.app.ui.theme.ErrorRed
import org.nongor.app.ui.theme.NongorGradientBackground
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.GeistMono
import org.nongor.app.ui.theme.GlassBg
import org.nongor.app.ui.theme.GlassBorder
import org.nongor.app.ui.theme.TextMuted
import org.nongor.app.ui.theme.TextPrimary
import org.nongor.app.ui.theme.TextSecondary
import org.nongor.app.ui.theme.UserBubble
import org.nongor.app.util.copyUriToCacheFile
import org.nongor.app.util.decodeDownscaledToCache
import org.nongor.app.util.saveBitmapToCacheFile
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import org.nongor.app.ui.theme.ShapeLg
import org.nongor.app.ui.theme.ShapeBubble
import org.nongor.app.ui.theme.ShapeMd
import org.nongor.app.ui.theme.ShapePill
import org.nongor.app.ui.theme.ShapeSm
import org.nongor.app.ui.theme.BrandTealLite
import org.nongor.app.ui.theme.BrandTealSoft

private data class Suggestion(
    val icon: ImageVector,
    val title: String,
    val hint: String,
    val fill: String,
    val accentColors: List<Color>,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onOpenSettings: () -> Unit,
    onNeedModel: () -> Unit,
) {
    val ui by viewModel.ui.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var input by remember { mutableStateOf("") }
    var thinking by remember { mutableStateOf(false) }
    var conciseMode by remember { mutableStateOf(true) }
    var pendingImagePath by remember { mutableStateOf<String?>(null) }
    var pendingAudioPath by remember { mutableStateOf<String?>(null) }
    var showAttachOptions by remember { mutableStateOf(false) }
    var showModelInfo by remember { mutableStateOf(false) }
    var streaming by remember { mutableStateOf("") }
    var modeHint by remember { mutableStateOf<String?>(null) }
    var tokenCount by remember { mutableStateOf(0) }
    var streamStartMs by remember { mutableStateOf(0L) }
    var lastTokPerSec by remember { mutableStateOf(0f) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                // Downscale before it reaches Gemma — a full-res gallery photo can OOM the model.
                pendingImagePath = (decodeDownscaledToCache(context, uri, prefix = "chat")
                    ?: copyUriToCacheFile(context, uri)).absolutePath
                modeHint = context.getString(R.string.attachment_ready)
            }.onFailure {
                pendingImagePath = null
                modeHint = context.getString(R.string.attachment_failed)
            }
        }
    }
    val takePicturePreview = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            runCatching {
                pendingImagePath = saveBitmapToCacheFile(context, bitmap, "jpg").absolutePath
                modeHint = context.getString(R.string.attachment_ready)
            }.onFailure {
                pendingImagePath = null
                modeHint = context.getString(R.string.attachment_failed)
            }
        }
    }

    val audioPerm = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var recordingFile by remember { mutableStateOf<File?>(null) }
    var isRecording by remember { mutableStateOf(false) }

    fun stopRecording() {
        runCatching { recorder?.stop(); recorder?.release() }
        recorder = null; isRecording = false
        recordingFile?.let { pendingAudioPath = it.absolutePath }
    }

    fun startRecording() {
        if (!audioPerm.status.isGranted) { audioPerm.launchPermissionRequest(); return }
        val out = File(context.cacheDir, "audio_${System.currentTimeMillis()}.m4a")
        recordingFile = out
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(out.absolutePath)
            prepare(); start()
        }
        isRecording = true
    }

    LaunchedEffect(messages.size, streaming) {
        val count = messages.size + if (streaming.isNotEmpty()) 1 else 0
        if (count > 0) listState.animateScrollToItem(count - 1)
    }

    LaunchedEffect(modeHint) {
        if (modeHint != null) {
            delay(1400)
            modeHint = null
        }
    }

    if (ui.sendError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearSendError() },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(ui.sendError ?: "") },
            confirmButton = { TextButton(onClick = { viewModel.clearSendError() }) { Text(tr("OK", "ঠিক আছে")) } },
            containerColor = BgCard,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
        )
    }

    if (showModelInfo) {
        AlertDialog(
            onDismissRequest = { showModelInfo = false },
            title = { Text(stringResource(R.string.model_info_title)) },
            text = { Text(stringResource(R.string.model_info_body)) },
            confirmButton = {
                TextButton(onClick = { showModelInfo = false }) {
                    Text(tr("Close", "বন্ধ করুন"))
                }
            },
            containerColor = BgCard,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
        )
    }

    if (showAttachOptions) {
        AlertDialog(
            onDismissRequest = { showAttachOptions = false },
            title = { Text(stringResource(R.string.attach_options_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            showAttachOptions = false
                            takePicturePreview.launch(null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            stringResource(R.string.attach_from_camera),
                            modifier = Modifier.fillMaxWidth(),
                            color = TextPrimary,
                        )
                    }
                    TextButton(
                        onClick = {
                            showAttachOptions = false
                            pickImage.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            stringResource(R.string.attach_from_gallery),
                            modifier = Modifier.fillMaxWidth(),
                            color = TextPrimary,
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAttachOptions = false }) {
                    Text(tr("Cancel", "বাতিল"))
                }
            },
            containerColor = BgCard,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
        )
    }

    ui.pendingAction?.let { action ->
        SuggestedActionDialog(
            action = action,
            onDismiss = { viewModel.clearPendingAction() },
            onConfirm = {
                val launched = runCatching {
                    AppActionLauncher.launch(context, action)
                }.getOrDefault(false)
                if (!launched) {
                    modeHint = "Couldn't open that app"
                }
                viewModel.clearPendingAction()
            },
        )
    }

    // ── Side drawer ───────────────────────────────────────────────────────────
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SideDrawer(
                conversations = conversations,
                onSelect = { id ->
                    viewModel.openConversation(id)
                    scope.launch { drawerState.close() }
                },
                onNewChat = {
                    viewModel.newChat(); input = ""; streaming = ""
                    pendingImagePath = null; pendingAudioPath = null
                    scope.launch { drawerState.close() }
                },
                onSettings = {
                    scope.launch { drawerState.close() }
                    onOpenSettings()
                },
            )
        },
        gesturesEnabled = true,
    ) {
        NongorGradientBackground {
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
            ) {
                // ── Top bar ───────────────────────────────────────────────────
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = TextPrimary)
                    }
                    Spacer(Modifier.weight(1f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showModelInfo = true },
                    ) {
                        PolkaDotsAccent()
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.model_name_strip),
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                        )
                        Icon(
                            Icons.Outlined.KeyboardArrowDown,
                            contentDescription = "Model details",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        PolkaDotsAccent(mirrored = true)
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            viewModel.newChat(); input = ""; streaming = ""
                            pendingImagePath = null; pendingAudioPath = null
                        },
                    ) {
                        Icon(Icons.Outlined.EditNote, contentDescription = "New chat", tint = TextPrimary)
                    }
                }

                // ── Content ───────────────────────────────────────────────────
                when {
                    ui.engineLoading -> {
                        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                GlowOrb(size = 72, animated = true)
                                Spacer(Modifier.height(16.dp))
                                Text(tr("Loading Gemma 4…", "Gemma 4 লোড হচ্ছে…"), color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(6.dp))
                                Text(tr("Warming up the local model", "স্থানীয় মডেল প্রস্তুত হচ্ছে"), color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }

                    ui.engineError != null -> {
                        Column(
                            Modifier.weight(1f).fillMaxWidth().padding(32.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(tr("Couldn't load model", "মডেল লোড করা যায়নি"), color = TextPrimary, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text(ui.engineError ?: "", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(20.dp))
                            GlassButton(tr("Retry", "আবার চেষ্টা করুন")) { viewModel.retryLoadEngine() }
                            Spacer(Modifier.height(8.dp))
                            GlassButton(tr("Re-download", "আবার ডাউনলোড করুন")) { onNeedModel() }
                        }
                    }

                    else -> {
                        val hasMessages = messages.isNotEmpty() || streaming.isNotEmpty()

                        if (!hasMessages) {
                            // ── Hero ──────────────────────────────────────────
                            Column(
                                Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                StaggeredReveal(0) { GlowOrb(size = 78, animated = true) }
                                Spacer(Modifier.height(20.dp))
                                StaggeredReveal(100) {
                                    Text(
                                        stringResource(R.string.hero_title),
                                        style = MaterialTheme.typography.displayLarge,
                                        color = TextPrimary,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                StaggeredReveal(200) {
                                    Text(
                                        stringResource(R.string.hero_subtitle),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }

                            // ── 2×2 suggestion grid ──────────────────────────
                            val suggestions = listOf(
                                Suggestion(
                                    Icons.Default.Place,
                                    stringResource(R.string.chip_plan_title),
                                    stringResource(R.string.chip_plan_hint),
                                    stringResource(R.string.chip_plan_fill),
                                    listOf(BrandTealLite, BrandTealSoft),
                                ),
                                Suggestion(
                                    Icons.Default.Image,
                                    stringResource(R.string.chip_identify_title),
                                    stringResource(R.string.chip_identify_hint),
                                    stringResource(R.string.chip_identify_fill),
                                    listOf(Color(0xFF60A5FA), Color(0xFF8B9CFF)),
                                ),
                                Suggestion(
                                    Icons.Default.AutoAwesome,
                                    stringResource(R.string.chip_boost_title),
                                    stringResource(R.string.chip_boost_hint),
                                    stringResource(R.string.chip_boost_fill),
                                    listOf(Color(0xFF6EA8FF), Color(0xFF2F6BFF)),
                                ),
                                Suggestion(
                                    Icons.Default.Edit,
                                    stringResource(R.string.chip_write_title),
                                    stringResource(R.string.chip_write_hint),
                                    stringResource(R.string.chip_write_fill),
                                    listOf(BrandTeal, BrandTealLite),
                                ),
                            )
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.height(236.dp),
                            ) {
                                itemsIndexed(suggestions) { index, s ->
                                    StaggeredReveal(300 + (index * 100)) {
                                        GlassSuggestionCard(s) { input = it }
                                    }
                                }
                            }
                        } else {
                            // ── Messages list ─────────────────────────────────
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 8.dp),
                            ) {
                                items(messages, key = { it.id }) { m ->
                                    if (m.role == "user") {
                                        UserMessage(m.text, m.imagePath)
                                    } else {
                                        AssistantMessage(m.text)
                                    }
                                }
                                if (streaming.isNotEmpty()) {
                                    item { AssistantMessage(streaming, isStreaming = true) }
                                }
                            }
                        }

                        // ── Composer ──────────────────────────────────────────
                        Composer(
                            input = input,
                            onInputChange = { input = it },
                            thinking = thinking,
                            onThinkingToggle = {
                                thinking = !thinking
                                modeHint = if (thinking) "Thinking enabled" else "Thinking disabled"
                            },
                            conciseMode = conciseMode,
                            onConciseToggle = {
                                conciseMode = !conciseMode
                                modeHint = if (conciseMode) "Short answers" else "Detailed answers"
                            },
                            onAttach = { showAttachOptions = true },
                            onMicToggle = { if (isRecording) stopRecording() else startRecording() },
                            isRecording = isRecording,
                            pendingImagePath = pendingImagePath,
                            modeHint = modeHint,
                            onPresetPick = {
                                input = it
                                modeHint = it
                            },
                            onClearImage = { pendingImagePath = null },
                            onSend = {
                                if (!ui.engineReady || ui.sending || (input.isBlank() && pendingImagePath == null)) return@Composer
                                focusManager.clearFocus(force = true)
                                keyboardController?.hide()
                                streaming = ""; tokenCount = 0; streamStartMs = System.currentTimeMillis()
                                viewModel.sendMessage(
                                    text = input,
                                    imagePath = pendingImagePath,
                                    audioPath = pendingAudioPath,
                                    thinking = thinking,
                                    concise = conciseMode,
                                    onToken = { chunk ->
                                        streaming += chunk
                                        tokenCount++
                                        val elapsed = System.currentTimeMillis() - streamStartMs
                                        if (elapsed > 0) lastTokPerSec = tokenCount * 1000f / elapsed
                                    },
                                    onComplete = {
                                        input = ""; pendingImagePath = null
                                        pendingAudioPath = null; streaming = ""
                                    },
                                )
                            },
                            sending = ui.sending,
                            tokPerSec = if (ui.sending && tokenCount > 0) lastTokPerSec else lastTokPerSec,
                            showTokPerSec = lastTokPerSec > 0,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PolkaDotsAccent(mirrored: Boolean = false) {
    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalAlignment = if (mirrored) Alignment.End else Alignment.Start,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Dot(color = BrandTeal.copy(alpha = 0.95f), size = 4.dp)
            Dot(color = BrandBlue.copy(alpha = 0.65f), size = 3.dp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Dot(color = BrandBlue.copy(alpha = 0.55f), size = 3.dp)
            Dot(color = BrandTeal.copy(alpha = 0.8f), size = 4.dp)
            Dot(color = BrandBlueGlow.copy(alpha = 0.45f), size = 2.dp)
        }
    }
}

@Composable
private fun Dot(color: Color, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(color = color, shape = CircleShape),
    )
}

@Composable
private fun SuggestedActionDialog(
    action: AssistantAction,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val message = when (action.type.lowercase()) {
        // Show the REAL destination URL, never the model-supplied label — a friendly label must
        // not be able to disguise where a link actually goes. Unopenable/non-web links say so.
        "open_url" -> {
            val safe = action.uri?.let { AppActionLauncher.safeWebUrl(it) }
            if (safe != null) tr("Open this link in your browser?\n", "এই লিঙ্কটি ব্রাউজারে খুলবেন?\n") + safe
            else tr("This link can't be opened safely and was blocked.",
                "এই লিঙ্কটি নিরাপদে খোলা যায় না, তাই বন্ধ করা হয়েছে।")
        }
        "open_app" -> {
            val target = action.app?.replaceFirstChar { it.titlecase() } ?: "external app"
            val querySuffix = action.query?.takeIf { it.isNotBlank() }?.let {
                "\n" + tr("Requested task: ", "অনুরোধকৃত কাজ: ") + it
            }.orEmpty()
            tr("Open $target from Gemma?", "Gemma থেকে $target খুলবেন?") + querySuffix
        }
        else -> tr("Open from Gemma?", "Gemma থেকে খুলবেন?")
    }
    // If an open_url link failed validation there is nothing safe to launch — hide the Open button.
    val blockedUrl = action.type.lowercase() == "open_url" &&
        action.uri?.let { AppActionLauncher.safeWebUrl(it) } == null
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Suggested action", "প্রস্তাবিত পদক্ষেপ")) },
        text = { Text(message) },
        confirmButton = {
            if (!blockedUrl) {
                TextButton(onClick = onConfirm) {
                    Text(tr("Open", "খুলুন"))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (blockedUrl) tr("Close", "বন্ধ করুন") else tr("Cancel", "বাতিল"))
            }
        },
        containerColor = BgCard,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
    )
}

// ── Side drawer content ───────────────────────────────────────────────────────
@Composable
private fun SideDrawer(
    conversations: List<org.nongor.app.data.ConversationEntity>,
    onSelect: (Long) -> Unit,
    onNewChat: () -> Unit,
    onSettings: () -> Unit,
) {
    Column(
        Modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(
                brush = Brush.verticalGradient(listOf(BgMid, BgDark)),
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp),
    ) {
        // New chat button
        Row(
            Modifier
                .fillMaxWidth()
                .clip(ShapeSm)
                .border(1.dp, GlassBorder, ShapeSm)
                .background(GlassBg)
                .clickable { onNewChat() }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.EditNote, contentDescription = null, tint = BrandTeal, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(tr("New chat", "নতুন চ্যাট"), color = TextPrimary, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(20.dp))
        Text(tr("Recent", "সাম্প্রতিক"), color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        LazyColumn(Modifier.weight(1f)) {
            items(conversations) { c ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(ShapeSm)
                        .clickable { onSelect(c.id) }
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        c.title.ifEmpty { stringResource(R.string.chat_untitled) },
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        // Settings at bottom
        Row(
            Modifier
                .fillMaxWidth()
                .clip(ShapeSm)
                .clickable { onSettings() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Settings, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(tr("Settings", "সেটিংস"), color = TextSecondary)
        }
    }
}

// ── Glow orb (logo) ──────────────────────────────────────────────────────────
@Composable
private fun StaggeredReveal(delayMs: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) + androidx.compose.animation.slideInVertically(
            initialOffsetY = { it / 5 },
            animationSpec = tween(500, easing = FastOutSlowInEasing),
        ),
    ) {
        content()
    }
}

// ── Glow orb (logo) ──────────────────────────────────────────────────────────
@Composable
private fun GlowOrb(size: Int, animated: Boolean = false) {
    val infinite = rememberInfiniteTransition(label = "glow_orb")
    val pulseScale by infinite.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_scale",
    )
    val haloAlpha by infinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "halo_alpha",
    )
    val ringScale by infinite.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ring_scale",
    )
    val ringAlpha by infinite.animateFloat(
        initialValue = 0.45f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ring_alpha",
    )
    val ringScale2 by infinite.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing, delayMillis = 350),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ring_scale2",
    )
    val ringAlpha2 by infinite.animateFloat(
        initialValue = 0.30f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing, delayMillis = 350),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ring_alpha2",
    )
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rotation",
    )

    Box(contentAlignment = Alignment.Center) {
        if (animated) {
            Box(
                Modifier
                    .size((size * 1.95f).dp)
                    .graphicsLayer {
                        scaleX = ringScale
                        scaleY = ringScale
                        alpha = ringAlpha
                    }
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                BrandTeal.copy(alpha = 0f),
                                BrandTeal.copy(alpha = 0.9f),
                                BrandBlue.copy(alpha = 0f),
                            ),
                            start = Offset.Zero,
                            end = Offset(size.toFloat() * 2f, size.toFloat() * 2f),
                        ),
                        shape = CircleShape,
                    ),
            )
            Box(
                Modifier
                    .size((size * 2.2f).dp)
                    .graphicsLayer {
                        scaleX = ringScale2
                        scaleY = ringScale2
                        alpha = ringAlpha2
                    }
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                BrandBlue.copy(alpha = 0f),
                                BrandBlueGlow.copy(alpha = 0.7f),
                                BrandTeal.copy(alpha = 0f),
                            ),
                            start = Offset.Zero,
                            end = Offset(size.toFloat() * 2.4f, size.toFloat() * 2.4f),
                        ),
                        shape = CircleShape,
                    ),
            )
        }
        // Outer glow
        Box(
            Modifier
                .size((size * 1.6).dp)
                .graphicsLayer {
                    if (animated) {
                        scaleX = pulseScale
                        scaleY = pulseScale
                        alpha = haloAlpha
                    }
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BrandBlue.copy(alpha = 0.3f),
                            BrandBlueGlow.copy(alpha = 0.1f),
                            Color.Transparent,
                        ),
                    ),
                    shape = CircleShape,
                ),
        )
        // Core
        Box(
            Modifier
                .size(size.dp)
                .graphicsLayer {
                    if (animated) {
                        rotationZ = rotation
                    }
                }
                .background(
                    brush = Brush.linearGradient(
                        colors = if (animated) {
                            listOf(BrandBlue, BrandTeal, BrandBlueGlow, BrandBlue)
                        } else {
                            listOf(BrandBlue, BrandTeal)
                        },
                        start = Offset.Zero,
                        end = Offset(size.toFloat(), size.toFloat()),
                    ),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("G", color = Color.White, fontSize = (size * 0.45).sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ── Glass suggestion card ────────────────────────────────────────────────────
@Composable
private fun GlassSuggestionCard(s: Suggestion, onPick: (String) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val lift by animateFloatAsState(
        targetValue = if (pressed) -2f else 0f,
        animationSpec = tween(180),
        label = "card_lift",
    )
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.02f else 1f,
        animationSpec = tween(180),
        label = "card_scale",
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (pressed) 0.75f else 0.55f,
        animationSpec = tween(180),
        label = "card_border",
    )
    Column(
        Modifier
            .graphicsLayer {
                translationY = lift
                scaleX = scale
                scaleY = scale
                shadowElevation = if (pressed) 16.dp.toPx() else 4.dp.toPx()
            }
            .clip(ShapeMd)
            .border(
                1.1.dp,
                Brush.linearGradient(
                    colors = listOf(
                        s.accentColors.first().copy(alpha = borderAlpha),
                        GlassBorder.copy(alpha = borderAlpha * 0.9f),
                        s.accentColors.last().copy(alpha = borderAlpha * 0.9f),
                    ),
                ),
                ShapeMd,
            )
            .background(if (pressed) GlassBg.copy(alpha = 0.96f) else GlassBg)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) { onPick(s.fill) }
            .padding(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(
                    Brush.linearGradient(s.accentColors),
                    ShapeSm,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(s.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(s.title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(s.hint, color = TextSecondary.copy(alpha = 0.85f), fontSize = 12.sp)
    }
}

// ── Glass button ─────────────────────────────────────────────────────────────
@Composable
private fun GlassButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = ShapeSm,
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandBlue,
            contentColor = Color.White,
        ),
    ) { Text(label, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun ImagePresetChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(ShapePill)
            .background(BgCard)
            .border(1.dp, Divider, ShapePill)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(
            label,
            color = TextSecondary,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun SmallToggleChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(ShapePill)
            .background(if (active) BgCard else BgDark.copy(alpha = 0.42f))
            .border(1.dp, if (active) BrandTeal.copy(alpha = 0.4f) else Divider, ShapePill)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        icon()
        Text(
            label,
            color = if (active) TextPrimary else TextSecondary,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

// ── Messages ─────────────────────────────────────────────────────────────────
@Composable
private fun UserMessage(text: String, imagePath: String? = null) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.End,
    ) {
        if (imagePath != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(File(imagePath))
                    .crossfade(true)
                    .build(),
                contentDescription = "Attached image",
                modifier = Modifier
                    .widthIn(max = 180.dp)
                    .clip(ShapeMd)
                    .border(1.dp, GlassBorder, ShapeMd),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.height(4.dp))
        }
        Box(
            Modifier
                .widthIn(max = 300.dp)
                .clip(ShapeBubble)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(UserBubble, BrandBlue.copy(alpha = 0.3f)),
                    ),
                )
                .border(1.dp, GlassBorder, ShapeBubble)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Text(text, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun AssistantMessage(text: String, isStreaming: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Mini glow orb avatar
        Box(
            Modifier
                .size(30.dp)
                .background(
                    brush = Brush.linearGradient(listOf(BrandBlue, BrandTeal)),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("G", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            MarkdownMessage(text)
            if (isStreaming) {
                Spacer(Modifier.height(4.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 1.5.dp,
                    color = BrandTeal,
                )
            }
        }
    }
}

@Composable
private fun MarkdownMessage(text: String) {
    val lines = text.replace("\r\n", "\n").split('\n')
    var inCodeBlock = false
    var codeBuffer = mutableListOf<String>()

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lines.forEach { rawLine ->
            val line = rawLine.trimEnd()

            if (line.trim().startsWith("```")) {
                if (inCodeBlock) {
                    CodeBlock(codeBuffer.joinToString("\n"))
                    codeBuffer = mutableListOf()
                }
                inCodeBlock = !inCodeBlock
                return@forEach
            }

            if (inCodeBlock) {
                codeBuffer.add(rawLine)
                return@forEach
            }

            when {
                line.isBlank() -> Spacer(Modifier.height(2.dp))
                line.startsWith("### ") -> MarkdownLine(
                    line.removePrefix("### "),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                line.startsWith("## ") -> MarkdownLine(
                    line.removePrefix("## "),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                line.startsWith("# ") -> MarkdownLine(
                    line.removePrefix("# "),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
                line.matches(Regex("^\\s*[-*]\\s+.*")) -> BulletLine(
                    line.replaceFirst(Regex("^\\s*[-*]\\s+"), ""),
                )
                line.matches(Regex("^\\s*\\d+\\.\\s+.*")) -> NumberedLine(
                    prefix = line.substringBefore('.') + ".",
                    content = line.replaceFirst(Regex("^\\s*\\d+\\.\\s+"), ""),
                )
                else -> MarkdownLine(line)
            }
        }
        if (inCodeBlock && codeBuffer.isNotEmpty()) {
            CodeBlock(codeBuffer.joinToString("\n"))
        }
    }
}

@Composable
private fun MarkdownLine(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Normal,
) {
    Text(
        text = buildInlineMarkdown(text),
        color = TextPrimary,
        fontSize = fontSize,
        fontWeight = fontWeight,
        lineHeight = (fontSize.value + 8).sp,
    )
}

@Composable
private fun BulletLine(content: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text("•", color = TextPrimary, modifier = Modifier.padding(end = 8.dp))
        Text(
            text = buildInlineMarkdown(content),
            color = TextPrimary,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun NumberedLine(prefix: String, content: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(prefix, color = TextPrimary, modifier = Modifier.padding(end = 8.dp))
        Text(
            text = buildInlineMarkdown(content),
            color = TextPrimary,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun CodeBlock(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeSm)
            .background(BgDark.copy(alpha = 0.9f))
            .border(1.dp, Divider, ShapeSm)
            .padding(12.dp),
    ) {
        Text(
            text = code,
            color = TextPrimary,
            fontFamily = GeistMono,
            fontSize = 13.sp,
            lineHeight = 18.sp,
        )
    }
}

private fun buildInlineMarkdown(text: String): AnnotatedString {
    val result = buildAnnotatedString {
        var index = 0
        while (index < text.length) {
            when {
                text.startsWith("**", index) -> {
                    val end = text.indexOf("**", index + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(index + 2, end))
                        }
                        index = end + 2
                    } else {
                        append(text[index])
                        index++
                    }
                }
                text.startsWith("`", index) -> {
                    val end = text.indexOf('`', index + 1)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = GeistMono,
                                background = BgDark.copy(alpha = 0.9f),
                            ),
                        ) {
                            append(text.substring(index + 1, end))
                        }
                        index = end + 1
                    } else {
                        append(text[index])
                        index++
                    }
                }
                else -> {
                    append(text[index])
                    index++
                }
            }
        }
    }
    return result
}

// ── Composer ──────────────────────────────────────────────────────────────────
@Composable
private fun Composer(
    input: String,
    onInputChange: (String) -> Unit,
    thinking: Boolean,
    onThinkingToggle: () -> Unit,
    conciseMode: Boolean,
    onConciseToggle: () -> Unit,
    onAttach: () -> Unit,
    onMicToggle: () -> Unit,
    isRecording: Boolean,
    pendingImagePath: String?,
    modeHint: String?,
    onPresetPick: (String) -> Unit,
    onClearImage: () -> Unit,
    onSend: () -> Unit,
    sending: Boolean,
    tokPerSec: Float = 0f,
    showTokPerSec: Boolean = false,
) {
    val canSend = (input.isNotBlank() || pendingImagePath != null) && !sending
    var isFocused by remember { mutableStateOf(false) }
    val composerBorderAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.95f else 0.38f,
        animationSpec = tween(220),
        label = "composer_border",
    )
    val composerLift by animateFloatAsState(
        targetValue = if (isFocused) -4f else 0f,
        animationSpec = tween(220),
        label = "composer_lift",
    )
    val shimmerShift by rememberInfiniteTransition(label = "composer_shimmer").animateFloat(
        initialValue = -240f,
        targetValue = 640f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "composer_shimmer_shift",
    )
    Column(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SmallToggleChip(
                label = if (conciseMode) "Short" else "Detailed",
                active = conciseMode,
                onClick = onConciseToggle,
                icon = {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (conciseMode) BrandTeal else TextMuted,
                        modifier = Modifier.size(14.dp),
                    )
                },
            )
            SmallToggleChip(
                label = "Thinking",
                active = thinking,
                onClick = onThinkingToggle,
                icon = {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = if (thinking) BrandTeal else TextMuted,
                        modifier = Modifier.size(14.dp),
                    )
                },
            )
        }

        AnimatedVisibility(visible = modeHint != null, enter = fadeIn(), exit = fadeOut()) {
            modeHint?.let { hint ->
                Box(
                    modifier = Modifier
                        .padding(bottom = 8.dp, start = 4.dp)
                        .clip(ShapePill)
                        .background(BgCard)
                        .border(1.dp, Divider, ShapePill)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        hint,
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }

        // Thumbnail preview
        AnimatedVisibility(visible = pendingImagePath != null, enter = fadeIn(), exit = fadeOut()) {
            if (pendingImagePath != null) {
                Column {
                    Box(Modifier.padding(bottom = 8.dp, start = 4.dp)) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(File(pendingImagePath))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Preview",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(ShapeSm)
                                .border(1.dp, GlassBorder, ShapeSm),
                            contentScale = ContentScale.Crop,
                        )
                        // Remove button
                        Box(
                            Modifier
                                .align(Alignment.TopEnd)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(BgDark.copy(alpha = 0.8f))
                                .clickable { onClearImage() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextPrimary, modifier = Modifier.size(12.dp))
                        }
                    }
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(start = 4.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ImagePresetChip(stringResource(R.string.preset_describe)) {
                            onPresetPick("Describe what you see in this image in detail.")
                        }
                        ImagePresetChip(stringResource(R.string.preset_read_text)) {
                            onPresetPick("Read all visible text in this image accurately.")
                        }
                        ImagePresetChip(stringResource(R.string.preset_explain)) {
                            onPresetPick("Explain this image and what is happening in it.")
                        }
                        ImagePresetChip(stringResource(R.string.preset_summarize)) {
                            onPresetPick("Summarize the key information from this image.")
                        }
                    }
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = composerLift
                    shadowElevation = if (isFocused) 24.dp.toPx() else 12.dp.toPx()
                }
                .clip(ShapeLg)
                .border(
                    1.25.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            BrandBlue.copy(alpha = composerBorderAlpha),
                            BrandTeal.copy(alpha = composerBorderAlpha),
                            Color(0xFF7DD3FC).copy(alpha = composerBorderAlpha * 0.8f),
                        ),
                        start = Offset.Zero,
                        end = Offset(520f, 140f),
                    ),
                    ShapeLg,
                )
                .background(GlassBg)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            IconButton(onClick = onAttach, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Attach", tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
            Box(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp, vertical = 8.dp),
            ) {
                if (input.isEmpty()) {
                    Text(
                        stringResource(R.string.ask_anything),
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = GeistMono),
                    )
                }
                BasicTextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
                    cursorBrush = SolidColor(BrandTeal),
                    maxLines = 6,
                )
            }
            IconButton(onClick = onMicToggle, modifier = Modifier.size(38.dp)) {
                Icon(
                    if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = "Mic",
                    tint = if (isRecording) ErrorRed else TextMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
            Box(
                Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        if (canSend) Brush.linearGradient(listOf(BrandBlue, BrandTeal))
                        else Brush.linearGradient(listOf(BgCard, BgCard)),
                    )
                    .then(if (canSend) Modifier.border(0.dp, Color.Transparent, CircleShape) else Modifier.border(1.dp, Divider, CircleShape))
                    .clickable(enabled = canSend) { onSend() },
                contentAlignment = Alignment.Center,
            ) {
                if (sending) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Send", tint = if (canSend) Color.White else TextMuted, modifier = Modifier.size(18.dp))
                }
            }
        }

        if (isFocused) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .height(2.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                BrandTeal.copy(alpha = 0.15f),
                                Color.White.copy(alpha = 0.35f),
                                BrandBlue.copy(alpha = 0.15f),
                                Color.Transparent,
                            ),
                            start = Offset(shimmerShift - 180f, 0f),
                            end = Offset(shimmerShift, 0f),
                        ),
                        ShapePill,
                    ),
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Gemma can make mistakes. Verify important info.",
                color = TextMuted.copy(alpha = 0.78f),
                style = MaterialTheme.typography.labelMedium,
                fontSize = 11.sp,
            )
            if (showTokPerSec) {
                Text(
                    " · %.1f tok/s".format(tokPerSec),
                    color = BrandTeal.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontFamily = GeistMono,
                )
            }
        }
    }
}
