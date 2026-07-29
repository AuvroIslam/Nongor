package org.nongor.app.ui.shell

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Activity
import compose.icons.feathericons.ChevronRight
import compose.icons.feathericons.Heart
import compose.icons.feathericons.MapPin
import compose.icons.feathericons.MessageSquare
import compose.icons.feathericons.PhoneCall
import compose.icons.feathericons.Settings
import compose.icons.feathericons.Users
import compose.icons.feathericons.WifiOff
import org.nongor.app.R
import org.nongor.app.ui.emergency.dialNumber
import org.nongor.app.ui.i18n.LocalBangla
import org.nongor.app.ui.i18n.localiseDigits
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.BgCard
import org.nongor.app.ui.theme.BrandTeal
import org.nongor.app.ui.theme.BrandTealDeep
import org.nongor.app.ui.theme.CautionAmber
import org.nongor.app.ui.theme.ErrorRed
import org.nongor.app.ui.theme.SafeGreen
import org.nongor.app.ui.theme.ShapeMd
import org.nongor.app.ui.theme.ShapePill
import org.nongor.app.ui.theme.ShapeSm
import org.nongor.app.ui.theme.TextPrimary
import org.nongor.app.ui.theme.TextSecondary
import compose.icons.feathericons.HelpCircle

/** One update on the home feed. Built from real mesh and board activity, never invented. */
data class HomeUpdate(
    val title: String,
    val detail: String,
    val minutesAgo: Long,
    val urgent: Boolean,
    /** The icon the source already uses, so the feed matches the screen it came from. */
    val icon: ImageVector,
)

/**
 * Home.
 *
 * Everything a person needs in the first ten seconds, in the order they need it: are we
 * working, what can I tap, how do I call, what is happening near me. Nothing here is more
 * than one tap from the top of the app, which is the whole point — a screen you have to
 * *navigate* is a screen that fails the person using it in the dark, in the rain, in a hurry.
 */
@Composable
fun HomeTab(
    modelReady: Boolean,
    peers: Int,
    updates: List<HomeUpdate>,
    onTranslate: () -> Unit,
    onShelter: () -> Unit,
    onFirstAid: () -> Unit,
    onFamily: () -> Unit,
    onBoard: () -> Unit,
    onEmergency: () -> Unit,
    onSettings: () -> Unit,
    onGuide: () -> Unit,
) {
    val context = LocalContext.current
    val bangla = LocalBangla.current

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
    ) {
        // ---- Greeting over the scene ----
        Box(Modifier.fillMaxWidth()) {
            Image(
                painterResource(R.drawable.home_scene),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(260.dp),
                contentScale = ContentScale.Crop,
                alignment = Alignment.BottomEnd,
            )
            // The illustration fades into the page so the cards below do not sit on a seam.
            Box(
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0.55f to Color.Transparent,
                            1f to MaterialTheme.colorScheme.background,
                        ),
                    ),
            )

            Column(
                Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp)
                    .padding(top = 8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painterResource(R.drawable.nongor_app_icon),
                        contentDescription = null,
                        modifier = Modifier.size(42.dp).clip(CircleShape),
                    )
                    Spacer(Modifier.width(9.dp))
                    Column {
                        Text(
                            "নোঙর",
                            color = BrandTealDeep,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 21.sp,
                            lineHeight = 22.sp,
                        )
                        Text(
                            "NONGOR",
                            color = BrandTeal,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.5.sp,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    RoundIcon(FeatherIcons.HelpCircle, onGuide)
                    Spacer(Modifier.width(8.dp))
                    RoundIcon(FeatherIcons.Settings, onSettings)
                }

                Spacer(Modifier.height(22.dp))
                // Held to the left two-thirds so it never runs across the house.
                Text(
                    tr("Assalamu Alaikum", "আসসালামু আলাইকুম"),
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    modifier = Modifier.fillMaxWidth(0.68f),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    tr(
                        "You are prepared.\nWe are here for you, always.",
                        "আপনি প্রস্তুত।\nআমরা সবসময় আপনার পাশে আছি।",
                    ),
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(0.72f),
                )
                Spacer(Modifier.height(18.dp))
            }
        }

        Column(Modifier.padding(horizontal = 18.dp)) {
            // ---- Status: the honest one-liner ----
            StatusCard(modelReady = modelReady, peers = peers, bangla = bangla)

            // ---- Quick actions ----
            Spacer(Modifier.height(18.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                QuickAction(FeatherIcons.MessageSquare, BrandTeal, tr("Translate", "অনুবাদ"), onTranslate)
                QuickAction(FeatherIcons.MapPin, Color(0xFF1F6D82), tr("Shelter", "আশ্রয়"), onShelter)
                QuickAction(FeatherIcons.Activity, SafeGreen, tr("First aid", "চিকিৎসা"), onFirstAid)
                QuickAction(FeatherIcons.Heart, Color(0xFF3C5A78), tr("Family", "পরিবার"), onFamily)
                QuickAction(FeatherIcons.Users, CautionAmber, tr("Board", "বোর্ড"), onBoard)
            }

            // ---- Emergency call ----
            Spacer(Modifier.height(18.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(ShapeMd)
                    .background(ErrorRed)
                    .clickable(onClick = onEmergency)
                    .padding(15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(FeatherIcons.PhoneCall, null, tint = Color.White, modifier = Modifier.size(21.dp))
                }
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        tr("Emergency call", "জরুরি কল"),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        tr("Police · Fire · Ambulance", "পুলিশ · ফায়ার · অ্যাম্বুলেন্স"),
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Row(
                    Modifier
                        .clip(ShapePill)
                        .background(Color.White)
                        .clickable { dialNumber(context, "999") }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(FeatherIcons.PhoneCall, null, tint = ErrorRed, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        localiseDigits(tr("Call 999", "৯৯৯"), bangla),
                        color = ErrorRed,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                    )
                }
            }

            // ---- What is happening ----
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    tr("Recent updates", "সাম্প্রতিক খবর"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Spacer(Modifier.weight(1f))
                Row(
                    Modifier.clip(ShapeSm).clickable(onClick = onBoard).padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        tr("See all", "সব দেখুন"),
                        color = BrandTeal,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(FeatherIcons.ChevronRight, null, tint = BrandTeal, modifier = Modifier.size(15.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            if (updates.isEmpty()) {
                // No invented activity. Silence is the honest state before anything happens.
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(ShapeMd)
                        .background(BgCard)
                        .padding(16.dp),
                ) {
                    Text(
                        tr("Nothing yet — and that is good news", "এখনো কিছু নেই — এটাই ভালো খবর"),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        tr(
                            "SOS messages and neighbours' reports appear here as soon as any " +
                                "phone near you sends one.",
                            "কাছের কোনো ফোন এসওএস বা রিপোর্ট পাঠালেই তা এখানে দেখা যাবে।",
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            } else {
                updates.take(4).forEach { UpdateRow(it, bangla) }
            }
        }
    }
}

@Composable
private fun RoundIcon(icon: ImageVector, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(BgCard)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
    }
}

/**
 * The status line.
 *
 * "Offline" is stated as a *capability*, not an error, because for Nongor it is the normal
 * operating mode — and the second line says exactly which parts are ready rather than
 * claiming everything is.
 */
@Composable
private fun StatusCard(modelReady: Boolean, peers: Int, bangla: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(ShapeMd)
            .background(BgCard)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(SafeGreen.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(FeatherIcons.WifiOff, null, tint = SafeGreen, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    tr("You are ", "আপনি এখন "),
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    tr("OFFLINE", "অফলাইনে"),
                    color = SafeGreen,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(Modifier.width(6.dp))
                Box(Modifier.size(7.dp).clip(CircleShape).background(SafeGreen))
            }
            Text(
                buildString {
                    append(
                        if (modelReady) {
                            tr("Every feature and the AI are ready on this phone", "সব সুবিধা ও এআই এই ফোনেই প্রস্তুত")
                        } else {
                            tr("Every feature is ready on this phone", "সব সুবিধা এই ফোনেই প্রস্তুত")
                        },
                    )
                    if (peers > 0) {
                        append(
                            localiseDigits(
                                tr(" · $peers phone(s) in range", " · কাছে $peers টি ফোন"),
                                bangla,
                            ),
                        )
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun QuickAction(icon: ImageVector, tint: Color, label: String, onClick: () -> Unit) {
    Column(
        Modifier.clip(ShapeSm).clickable(onClick = onClick).padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(54.dp).clip(CircleShape).background(tint),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(23.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun UpdateRow(update: HomeUpdate, bangla: Boolean) {
    val tint = if (update.urgent) ErrorRed else BrandTeal
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(ShapeMd)
            .background(BgCard)
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(36.dp).clip(CircleShape).background(tint.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(update.icon, null, tint = tint, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                update.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
            )
            Text(update.detail, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Spacer(Modifier.width(8.dp))
        Text(
            localiseDigits(
                when {
                    update.minutesAgo < 1 -> tr("now", "এখন")
                    update.minutesAgo < 60 -> tr("${update.minutesAgo}m", "${update.minutesAgo}মি")
                    else -> tr("${update.minutesAgo / 60}h", "${update.minutesAgo / 60}ঘ")
                },
                bangla,
            ),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
    }
}
