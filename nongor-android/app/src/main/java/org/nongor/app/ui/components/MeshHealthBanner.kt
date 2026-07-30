package org.nongor.app.ui.components

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import compose.icons.FeatherIcons
import compose.icons.feathericons.AlertTriangle
import compose.icons.feathericons.Bluetooth
import compose.icons.feathericons.ChevronRight
import compose.icons.feathericons.MapPin
import compose.icons.feathericons.Wifi
import org.nongor.app.mesh.MeshBlocker
import org.nongor.app.mesh.MeshReadiness
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.CautionAmber
import org.nongor.app.ui.theme.ErrorRed
import org.nongor.app.ui.theme.ShapeSm
import org.nongor.app.ui.theme.TextPrimary
import org.nongor.app.ui.theme.TextSecondary

/**
 * Why phone-to-phone is not working, said out loud.
 *
 * Without this the app advertised into a switched-off radio and reported "0 phones in range",
 * which reads as "nobody is nearby" when it actually means "this phone cannot hear anybody".
 * In a flood those are opposite conclusions: the first tells you to wait, the second tells you
 * to fix something. Tapping the banner opens the exact settings panel that fixes it.
 *
 * Re-checked on every resume, so returning from Settings with Bluetooth switched on makes the
 * banner disappear without the user having to work out that they should reopen the screen.
 */
@Composable
fun MeshHealthBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var blockers by remember { mutableStateOf(MeshReadiness.blockers(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) blockers = MeshReadiness.blockers(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // The first blocker is the one to fix: without permission nothing else can even be read,
    // and without Bluetooth nothing can be discovered whatever else is on.
    val worst = blockers.firstOrNull() ?: return
    val fatal = worst != MeshBlocker.WIFI
    val tint = if (fatal) ErrorRed else CautionAmber

    Row(
        modifier
            .fillMaxWidth()
            .clip(ShapeSm)
            .background(tint.copy(alpha = 0.10f))
            .clickable { openFixFor(context, worst) }
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(30.dp).clip(CircleShape).background(tint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                when (worst) {
                    MeshBlocker.BLUETOOTH -> FeatherIcons.Bluetooth
                    MeshBlocker.WIFI -> FeatherIcons.Wifi
                    MeshBlocker.LOCATION_SERVICES -> FeatherIcons.MapPin
                    MeshBlocker.PERMISSIONS -> FeatherIcons.AlertTriangle
                },
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title(worst),
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
            )
            Text(
                detail(worst),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(FeatherIcons.ChevronRight, null, tint = tint, modifier = Modifier.size(18.dp))
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun title(b: MeshBlocker): String = when (b) {
    MeshBlocker.BLUETOOTH -> tr("Bluetooth is off", "ব্লুটুথ বন্ধ")
    MeshBlocker.WIFI -> tr("Wi-Fi is off", "ওয়াই-ফাই বন্ধ")
    MeshBlocker.LOCATION_SERVICES -> tr("Location is off", "লোকেশন বন্ধ")
    MeshBlocker.PERMISSIONS -> tr("Permissions needed", "অনুমতি দরকার")
}

@Composable
private fun detail(b: MeshBlocker): String = when (b) {
    MeshBlocker.BLUETOOTH -> tr(
        "Phone-to-phone cannot find anyone without it. Tap to turn it on.",
        "এটি ছাড়া ফোন থেকে ফোনে কাউকে খুঁজে পাওয়া যায় না। চালু করতে চাপ দিন।",
    )
    MeshBlocker.WIFI -> tr(
        "Messages will still find people, but much more slowly. Tap to turn it on.",
        "বার্তা তবু পৌঁছাবে, তবে অনেক ধীরে। চালু করতে চাপ দিন।",
    )
    MeshBlocker.LOCATION_SERVICES -> tr(
        "Android needs it switched on to scan for nearby phones. Tap to turn it on.",
        "কাছের ফোন খুঁজতে অ্যান্ড্রয়েডের এটি চালু থাকা লাগে। চালু করতে চাপ দিন।",
    )
    MeshBlocker.PERMISSIONS -> tr(
        "Nongor needs nearby-devices and location permission to reach other phones.",
        "অন্য ফোনে পৌঁছাতে নোঙরের কাছের-ডিভাইস ও লোকেশন অনুমতি লাগে।",
    )
}

/** Send the user straight to the panel that fixes this, not to the settings root. */
private fun openFixFor(context: Context, b: MeshBlocker) {
    val action = when (b) {
        MeshBlocker.BLUETOOTH -> Settings.ACTION_BLUETOOTH_SETTINGS
        MeshBlocker.WIFI -> Settings.ACTION_WIFI_SETTINGS
        MeshBlocker.LOCATION_SERVICES -> Settings.ACTION_LOCATION_SOURCE_SETTINGS
        MeshBlocker.PERMISSIONS -> Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    }
    val intent = Intent(action).apply {
        if (b == MeshBlocker.PERMISSIONS) {
            data = android.net.Uri.fromParts("package", context.packageName, null)
        }
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
        .onFailure {
            runCatching {
                context.startActivity(
                    Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
        }
}
