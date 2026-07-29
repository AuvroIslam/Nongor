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

/**
 * Nongor's four resting places, plus the one thing that is never more than a thumb away.
 *
 * The bar carries Home, Map, Alerts and More. The SOS button sits in the middle, raised out
 * of the bar, and is reachable from **every** screen in the app — because the moment you
 * need it is not a moment to be navigating, and because a person who has never opened this
 * app before will still understand a big red circle in the middle of the screen.
 */
enum class Tab(val icon: ImageVector) {
    HOME(FeatherIcons.Home),
    MAP(FeatherIcons.Map),
    ALERTS(FeatherIcons.Bell),
    MORE(FeatherIcons.Grid),
    ;

    @Composable
    fun label(): String = when (this) {
        HOME -> tr("Home", "হোম")
        MAP -> tr("Map", "মানচিত্র")
        ALERTS -> tr("Alerts", "খবর")
        MORE -> tr("More", "আরও")
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
        Box(Modifier.fillMaxSize().padding(bottom = 74.dp)) { content() }

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
                BarItem(Tab.MORE, current, onSelect, Modifier.weight(1f))
            }

            // The anchor, raised. Red while an SOS is going out, so the app never looks calm
            // while it is shouting for help.
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(if (sosActive) ErrorRed else BrandTeal)
                    .clickable(onClick = onSos),
                contentAlignment = Alignment.Center,
            ) {
                if (sosActive) {
                    Text(
                        "SOS",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                    )
                } else {
                    Image(
                        painterResource(R.drawable.nongor_mark),
                        contentDescription = tr("Send an SOS", "এসওএস পাঠান"),
                        modifier = Modifier.size(34.dp),
                    )
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
    Column(
        modifier
            .fillMaxSize()
            .clip(ShapeLg)
            .clickable { onSelect(tab) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            tab.icon,
            contentDescription = null,
            tint = if (selected) BrandTeal else TextSecondary,
            modifier = Modifier.size(19.dp),
        )
        Spacer(Modifier.height(3.dp))
        Text(
            tab.label(),
            color = if (selected) BrandTeal else TextSecondary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 10.sp,
        )
    }
}
