package org.nongor.app.ui.settings

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.Check
import compose.icons.feathericons.Cpu
import compose.icons.feathericons.Download
import compose.icons.feathericons.ExternalLink
import compose.icons.feathericons.Globe
import compose.icons.feathericons.Info
import compose.icons.feathericons.MapPin
import compose.icons.feathericons.MessageSquare
import compose.icons.feathericons.Shield
import compose.icons.feathericons.Trash2
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import org.nongor.app.data.RegionAssets
import org.nongor.app.data.Regions
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.AccentPurple
import org.nongor.app.ui.theme.BgCard
import org.nongor.app.ui.theme.BgDark
import org.nongor.app.ui.theme.ErrorRed
import org.nongor.app.ui.theme.GlassBorder
import org.nongor.app.ui.theme.TextMuted
import org.nongor.app.ui.theme.TextPrimary
import org.nongor.app.ui.theme.TextSecondary
import org.nongor.app.ui.theme.TileShelterBg
import org.nongor.app.ui.theme.TileShelterFg
import org.nongor.app.ui.theme.TileSummaryBg
import org.nongor.app.ui.theme.TileSummaryFg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    var cleared by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val appVersion = remember { appVersionName(context) }
    val language by viewModel.language.collectAsState()
    val activeRegion by viewModel.activeRegion.collectAsState()
    val installed by viewModel.installedRegions.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .background(BgDark)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // Top bar
        Row(
            Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(42.dp).clip(CircleShape).background(BgCard)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(FeatherIcons.ArrowLeft, contentDescription = "Back", tint = TextPrimary,
                    modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(tr("Settings", "সেটিংস"), style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary, fontWeight = FontWeight.Bold)
        }

        Column(
            Modifier.padding(horizontal = 16.dp).verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            // ---- Language ----
            SectionLabel(tr("Language", "ভাষা"))
            SettingsCard {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconBubble(FeatherIcons.Globe, TileSummaryBg, TileSummaryFg)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(tr("App language", "অ্যাপের ভাষা"), color = TextPrimary,
                            fontWeight = FontWeight.Medium)
                        Text(tr("Interface and Gemma replies", "ইন্টারফেস ও Gemma-র উত্তর"),
                            color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Row(Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) {
                    LangChip("English", language == "en", Modifier.weight(1f)) {
                        viewModel.setLanguage("en")
                    }
                    Spacer(Modifier.width(10.dp))
                    LangChip("বাংলা", language == "bn", Modifier.weight(1f)) {
                        viewModel.setLanguage("bn")
                    }
                }
            }

            // ---- Region packs ----
            Spacer(Modifier.height(20.dp))
            SectionLabel(tr("Offline region packs", "অফলাইন এলাকা প্যাক"))
            Text(
                tr(
                    "Shelter finder and district detection work anywhere in Bangladesh (all 64 districts). " +
                        "These areas add a detailed offline map with flood-avoiding routes.",
                    "আশ্রয় খোঁজা ও জেলা শনাক্তকরণ বাংলাদেশের যেকোনো জায়গায় কাজ করে (৬৪টি জেলা)। " +
                        "এই এলাকাগুলোতে বন্যা এড়ানো পথসহ বিস্তারিত অফলাইন মানচিত্র রয়েছে।",
                ),
                color = TextSecondary, style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            SettingsCard {
                Regions.ALL.forEachIndexed { i, region ->
                    if (i > 0) Divider()
                    val isActive = region.id == activeRegion
                    val isInstalled = region.id in installed
                    val name = if (language == "bn") region.nameBn else region.nameEn
                    val desc = if (language == "bn") region.descBn else region.descEn
                    val counts = remember(region.id) { regionCounts(context, region.id) }
                    Row(
                        Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconBubble(FeatherIcons.MapPin, TileShelterBg, TileShelterFg)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(name, color = TextPrimary, fontWeight = FontWeight.Medium)
                            Text(desc, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            if (isInstalled) {
                                Text(
                                    "${counts.first} " + tr("shelters", "আশ্রয়") +
                                        " · ${counts.second} " + tr("facilities", "স্থাপনা"),
                                    color = TextMuted, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        when {
                            isActive -> Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(FeatherIcons.Check, null, tint = TileShelterFg,
                                    modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(tr("Active", "সক্রিয়"), color = TileShelterFg,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium)
                            }
                            isInstalled -> TextButton(onClick = { viewModel.activateRegion(region.id) }) {
                                Text(tr("Set active", "সক্রিয় করুন"))
                            }
                            else -> TextButton(onClick = { viewModel.installRegion(region.id) }) {
                                Icon(FeatherIcons.Download, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(tr("Add", "যোগ করুন"))
                            }
                        }
                    }
                }
            }

            // ---- Model ----
            Spacer(Modifier.height(20.dp))
            SectionLabel(tr("Model", "মডেল"))
            SettingsCard {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconBubble(FeatherIcons.Cpu, TileSummaryBg, TileSummaryFg)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Gemma 4 E2B", color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text(tr("LiteRT-LM · on-device inference", "LiteRT-LM · ডিভাইসেই চলে"),
                            color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // ---- Data ----
            Spacer(Modifier.height(20.dp))
            SectionLabel(tr("Data", "ডেটা"))
            SettingsCard {
                ActionRow(
                    FeatherIcons.MessageSquare, TextPrimary,
                    tr("Clear chats", "চ্যাট মুছুন"),
                    tr("Remove all conversation history", "সব কথোপকথন মুছে ফেলুন"),
                ) { viewModel.clearChatHistory(); cleared = true }
                if (cleared) {
                    Text(tr("Chats cleared", "চ্যাট মোছা হয়েছে"), color = AccentPurple,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(start = 58.dp, bottom = 6.dp))
                }
                Divider()
                ActionRow(
                    FeatherIcons.Trash2, ErrorRed,
                    tr("Delete model", "মডেল মুছুন"),
                    tr("Re-download needed to chat again", "আবার চ্যাট করতে পুনরায় ডাউনলোড লাগবে"),
                ) { viewModel.deleteModel() }
            }

            // ---- About ----
            Spacer(Modifier.height(20.dp))
            SectionLabel(tr("About", "সম্পর্কে"))
            SettingsCard {
                InfoRow(
                    FeatherIcons.Info,
                    tr("Nongor (নোঙর)", "নোঙর"),
                    tr(
                        "Offline AI disaster response for floods in Bangladesh, powered by Gemma 4.",
                        "বাংলাদেশের বন্যায় Gemma 4 চালিত অফলাইন এআই দুর্যোগ সহায়তা।",
                    ),
                    "v$appVersion",
                )
                Divider()
                LinkRow(
                    FeatherIcons.Cpu, "Gemma 4 E2B",
                    tr("On Hugging Face (LiteRT-LM)", "Hugging Face-এ (LiteRT-LM)"),
                ) { uriHandler.openUri("https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm") }
                Divider()
                InfoRow(
                    FeatherIcons.Shield,
                    tr("Fully on-device & private", "সম্পূর্ণ ডিভাইসে ও ব্যক্তিগত"),
                    tr(
                        "Reports, photos and chats never leave this phone.",
                        "রিপোর্ট, ছবি ও চ্যাট কখনো এই ফোন ছেড়ে যায় না।",
                    ),
                )
                Divider()
                LinkRow(
                    FeatherIcons.MapPin,
                    tr("Map & shelter data", "মানচিত্র ও আশ্রয় তথ্য"),
                    tr("Roads & shelters © OpenStreetMap contributors (ODbL)",
                        "রাস্তা ও আশ্রয় © OpenStreetMap contributors (ODbL)"),
                ) { uriHandler.openUri("https://www.openstreetmap.org/copyright") }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp, top = 4.dp))
}

@Composable
private fun SettingsCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp)).background(BgCard),
        content = content,
    )
}

@Composable
private fun IconBubble(icon: ImageVector, bg: Color, fg: Color) {
    Box(Modifier.size(40.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun LangChip(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.clip(RoundedCornerShape(12.dp))
            .background(if (selected) AccentPurple else BgDark)
            .border(1.dp, if (selected) AccentPurple else GlassBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (selected) Color.White else TextPrimary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().padding(start = 58.dp, end = 14.dp).height(1.dp)
        .background(GlassBorder))
}

@Composable
private fun ActionRow(icon: ImageVector, tint: Color, label: String, desc: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.padding(start = 14.dp)) {
            Text(label, color = tint, fontWeight = FontWeight.Medium)
            Text(desc, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, desc: String, supporting: String? = null) {
    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.Top) {
        IconBubble(icon, TileSummaryBg, TileSummaryFg)
        Column(Modifier.padding(start = 14.dp).weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, color = TextPrimary, fontWeight = FontWeight.Medium)
            Text(desc, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            supporting?.let { Text(it, color = TextMuted, style = MaterialTheme.typography.labelSmall) }
        }
    }
}

@Composable
private fun LinkRow(icon: ImageVector, label: String, desc: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBubble(icon, TileSummaryBg, TileSummaryFg)
        Column(Modifier.padding(start = 14.dp).weight(1f)) {
            Text(label, color = TextPrimary, fontWeight = FontWeight.Medium)
            Text(desc, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
        Icon(FeatherIcons.ExternalLink, null, tint = TextMuted, modifier = Modifier.size(18.dp))
    }
}

private fun regionCounts(context: Context, id: String): Pair<Int, Int> = runCatching {
    RegionAssets.loadShelters(context, id).size to RegionAssets.loadFacilities(context, id).size
}.getOrDefault(0 to 0)

private fun appVersionName(context: Context): String =
    runCatching {
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        info.versionName ?: info.longVersionCode.toString()
    }.getOrDefault("1.0")
