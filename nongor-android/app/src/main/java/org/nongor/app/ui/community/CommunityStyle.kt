package org.nongor.app.ui.community

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FeatherIcons
import compose.icons.feathericons.AlertTriangle
import compose.icons.feathericons.CheckCircle
import compose.icons.feathericons.Droplet
import compose.icons.feathericons.Home
import compose.icons.feathericons.LifeBuoy
import compose.icons.feathericons.Package
import compose.icons.feathericons.Slash
import compose.icons.feathericons.ShoppingBag
import org.nongor.app.data.CommunityKinds

/**
 * How a report looks on the board.
 *
 * The board is scanned, not read: someone standing in the rain wants to know in one glance
 * whether the news on this card is *a warning* or *an offer of help*. So the whole card is
 * tinted by that one distinction — warm for "careful", cool for "this is available" — and the
 * icon only has to separate items inside each group.
 */
data class KindStyle(val icon: ImageVector, val bg: Color, val fg: Color)

private val WarnBg = Color(0xFFFBEADF)
private val WarnFg = Color(0xFFB4530F)
private val DangerBg = Color(0xFFFBE9E7)
private val DangerFg = Color(0xFFC62828)
private val InfoBg = Color(0xFFE4EEF5)
private val InfoFg = Color(0xFF1F6D82)
private val GoodBg = Color(0xFFE3F1EA)
private val GoodFg = Color(0xFF11704E)

fun kindStyle(kindId: String): KindStyle {
    val kind = CommunityKinds.byId(kindId)
    return when (kindId) {
        "road_flooded" -> KindStyle(FeatherIcons.Droplet, WarnBg, WarnFg)
        "bridge_down" -> KindStyle(FeatherIcons.Slash, WarnBg, WarnFg)
        "shelter_full" -> KindStyle(FeatherIcons.Home, WarnBg, WarnFg)
        // The one that means "do not come here". It is the only card allowed to be red.
        "danger" -> KindStyle(FeatherIcons.AlertTriangle, DangerBg, DangerFg)
        "supplies" -> KindStyle(FeatherIcons.Package, GoodBg, GoodFg)
        "pharmacy_open" -> KindStyle(FeatherIcons.ShoppingBag, InfoBg, InfoFg)
        "safe_route" -> KindStyle(FeatherIcons.CheckCircle, GoodBg, GoodFg)
        "rescue_here" -> KindStyle(FeatherIcons.LifeBuoy, InfoBg, InfoFg)
        else -> if (kind.danger) {
            KindStyle(FeatherIcons.AlertTriangle, WarnBg, WarnFg)
        } else {
            KindStyle(FeatherIcons.Package, InfoBg, InfoFg)
        }
    }
}
