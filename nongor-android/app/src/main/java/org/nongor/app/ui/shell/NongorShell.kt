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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Bell
import compose.icons.feathericons.Grid
import compose.icons.feathericons.Home
import compose.icons.feathericons.Map
import org.nongor.app.R
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.BgCard
import org.nongor.app.ui.theme.BrandTeal
import org.nongor.app.ui.theme.ErrorRed
import org.nongor.app.ui.theme.GlassBorder
import org.nongor.app.ui.theme.ShapeLg
import org.nongor.app.ui.theme.TextSecondary
import androidx.compose.foundation.layout.offset
import compose.icons.feathericons.Radio
import org.nongor.app.ui.theme.ShapeMd

/**
 * Nongor's four resting places, plus the one thing that is never more than a thumb away.
 *
 * The bar carries Home, Map, Alerts and More. The SOS button sits in the middle, raised out
 * of the bar, and is reachable from **every** screen in the app — because the moment you
 * need it is not a moment to be navigating, and because a person who has never opened this
 * app before will still understand a big red circle in the middle of the screen.
 */
enum class Tab {
    HOME,
    MAP,
    ALERTS,
    VOLUNTEER,
    ;

    @Composable
    fun label(): String = when (this) {
        HOME -> tr("Home", "হোম")
        MAP -> tr("Map", "মানচিত্র")
        ALERTS -> tr("Alerts", "খবর")
        VOLUNTEER -> tr("Volunteer", "স্বয়ংসেবক")
    }
}

@Composable
fun NongorShell(
    current: Tab,
    onSelect: (Tab) -> Unit,
    onSos: () -> Unit,
    sosActive: Boolean,
    content: @Composable () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Content runs under the bar; screens add their own bottom padding.
        Box(Modifier.fillMaxSize().padding(bottom = 96.dp)) { content() }

        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .clip(ShapeLg)
                    .background(BgCard)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BarItem(Tab.HOME, current, onSelect, Modifier.weight(1f))
                BarItem(Tab.MAP, current, onSelect, Modifier.weight(1f))
                // Space the raised SOS button sits over.
                Spacer(Modifier.weight(1f))
                BarItem(Tab.ALERTS, current, onSelect, Modifier.weight(1f))
                BarItem(Tab.VOLUNTEER, current, onSelect, Modifier.weight(1f))
            }

            // The anchor, raised. Red while an SOS is going out, so the app never looks calm
            // while it is shouting for help.
            // Raised clear of the bar, like a call button on a dashboard. White ring so it
            // reads as a separate object rather than part of the bar behind it.
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-16).dp)
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(BgCard)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(ErrorRed)
                    .clickable(onClick = onSos),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        FeatherIcons.Radio,
                        contentDescription = tr("Send an SOS", "এসওএস পাঠান"),
                        tint = Color.White,
                        modifier = Modifier.size(if (sosActive) 15.dp else 18.dp),
                    )
                    Text(
                        "SOS",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        lineHeight = 13.sp,
                    )
                    if (sosActive) {
                        Text(
                            tr("sending", "যাচ্ছে"),
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 7.sp,
                            lineHeight = 8.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BarItem(
    tab: Tab,
    current: Tab,
    onSelect: (Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected = tab == current
    val tint = if (selected) BrandTeal else TextSecondary
    Column(
        modifier
            .fillMaxSize()
            .padding(vertical = 6.dp, horizontal = 3.dp)
            .clip(ShapeMd)
            .background(if (selected) BrandTeal.copy(alpha = 0.12f) else Color.Transparent)
            .clickable { onSelect(tab) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        when (tab) {
            Tab.HOME -> Icon(FeatherIcons.Home, null, tint = tint, modifier = Modifier.size(19.dp))
            Tab.MAP -> Icon(FeatherIcons.Map, null, tint = tint, modifier = Modifier.size(19.dp))
            Tab.ALERTS -> Icon(FeatherIcons.Bell, null, tint = tint, modifier = Modifier.size(19.dp))
            // A raised hand: the icon for offering help, not for a menu.
            Tab.VOLUNTEER -> Icon(
                painterResource(R.drawable.ic_volunteer),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(19.dp),
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            tab.label(),
            color = tint,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
            fontSize = 10.sp,
        )
    }
}
