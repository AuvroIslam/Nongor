package org.nongor.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * The shape and spacing system.
 *
 * Before this existed the app used ten different corner radii — 3, 6, 8, 10, 12, 14, 16, 18,
 * 24 and pill — which is the kind of thing nobody can name when they look at a screen but
 * everybody feels. Three radii and a pill is enough to express every level of hierarchy the
 * app actually has, and the constraint keeps new screens consistent for free.
 */
object Radius {
    /** Chips, icon tiles, image thumbnails — anything small sitting inside something else. */
    val sm = 10.dp

    /** The default. Cards, buttons, text fields, list rows. */
    val md = 16.dp

    /** Things that own the screen: dialogs, hero panels, bottom sheets. */
    val lg = 24.dp

    /** Fully round. Pills, badges, progress tracks, avatars. */
    val pill = 999.dp
}

val ShapeSm = RoundedCornerShape(Radius.sm)
val ShapeMd = RoundedCornerShape(Radius.md)
val ShapeLg = RoundedCornerShape(Radius.lg)
val ShapePill = RoundedCornerShape(Radius.pill)

/** Chat bubble: [Radius.md] with one square-ish corner for the tail. */
val ShapeBubble = RoundedCornerShape(
    topStart = Radius.md,
    topEnd = Radius.md,
    bottomEnd = 4.dp,
    bottomStart = Radius.md,
)

/**
 * Vertical and horizontal rhythm, on a 4dp grid.
 *
 * [screen] is the one that matters most: every screen uses the same side margin, so content
 * lines up when you move between them.
 */
object Space {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp

    /** Side margin for every screen. */
    val screen = 18.dp
}

/**
 * Surfaces are separated by a hairline, never by a shadow.
 *
 * Elevation shadows are hard to see at all on a cheap LCD in daylight, and they muddy
 * screenshots. A one-pixel border does the same job, reads identically on every device, and
 * keeps the whole app looking flat and calm instead of bubbly.
 */
object Stroke {
    val hairline = 1.dp
    val focus = 1.5.dp
}
