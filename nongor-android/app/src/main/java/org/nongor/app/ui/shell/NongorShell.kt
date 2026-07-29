package org.nongor.app.ui.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.LifeBuoy
import compose.icons.feathericons.MapPin
import compose.icons.feathericons.MessageSquare
import compose.icons.feathericons.Users
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.BgCard
import org.nongor.app.ui.theme.BrandTeal
import org.nongor.app.ui.theme.TextSecondary

/**
 * Nongor's four places.
 *
 * The app used to open onto a grid of ten tiles — every tool given equal weight, and the user
 * asked to pick one before anything happened. That is a fine shape for a toolbox and a poor
 * one for an emergency, where the answer to "what do I do" should already be on screen.
 *
 * So the app is now organised by the four things a person is actually trying to do, and it
 * opens on [Tab.TALK], because the gap Nongor exists to close is that the person in front of
 * you cannot tell you what is wrong.
 */
enum class Tab(
    val route: String,
    val icon: ImageVector,
) {
    /** Understand each other. The reason this app exists. */
    TALK("tab_talk", FeatherIcons.MessageSquare),

    /** Get help, or give it: SOS, the siren, 999, first aid. */
    HELP("tab_help", FeatherIcons.LifeBuoy),

    /** Get somewhere: shelters, routes, family. */
    MOVE("tab_move", FeatherIcons.MapPin),

    /** What everyone else is seeing: the board, the queue, the briefing. */
    AREA("tab_area", FeatherIcons.Users),
    ;

    @Composable
    fun label(): String = when (this) {
        TALK -> tr("Talk", "কথা")
        HELP -> tr("Help", "সাহায্য")
        MOVE -> tr("Move", "চলুন")
        AREA -> tr("Area", "এলাকা")
    }
}

@Composable
fun NongorShell(
    current: Tab,
    onSelect: (Tab) -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = BgCard, tonalElevation = 0.dp) {
                Tab.entries.forEach { tab ->
                    val selected = tab == current
                    NavigationBarItem(
                        selected = selected,
                        onClick = { onSelect(tab) },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = {
                            Text(
                                tab.label(),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandTeal,
                            selectedTextColor = BrandTeal,
                            indicatorColor = BrandTeal.copy(alpha = 0.12f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                        ),
                    )
                }
            }
        },
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(bottom = pad.calculateBottomPadding())) {
            content(Modifier)
        }
    }
}
