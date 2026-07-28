package org.nongor.app.ui.theme

/**
 * Semantic colours, named by what they *mean* rather than what they look like.
 *
 * Screens reach for these instead of raw hex so meaning stays consistent app-wide: danger is
 * always the same red, "confirmed" is always the same green, "not certain" is always the same
 * amber. On a phone held at arm's length in bad light, that consistency is doing real work.
 *
 * These are aliases of the brand ramp in `Color.kt`, not a second set of values — one source
 * of truth, so retheming the app cannot leave half the screens behind.
 */
object NongorColors {
    /** Structure and headings. */
    val Deep = BrandTealDeep
    val DeepDark = BrandTealDeep

    /** Secondary structure. */
    val Surf = BrandTealLite

    /** "Act now", and anything not yet verified. */
    val Amber = BrandSand
    val Caution = CautionAmber

    /** Life-threatening. Used sparingly so it never stops meaning what it means. */
    val Danger = ErrorRed
    val DangerDark = ErrorRed

    /** Confirmed, safe, delivered, verified. */
    val Safe = SafeGreen

    val Ink = TextPrimary
    val Muted = TextSecondary
}
