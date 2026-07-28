package org.nongor.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Semantic colours, named by what they *mean* rather than what they look like.
 *
 * Screens should reach for these instead of raw hex so that meaning stays consistent across
 * the app: danger is always the same red, "this is safe / confirmed" is always the same
 * green, and "careful, this is not certain" is always the same amber. On a phone held at
 * arm's length in bad light, that consistency is doing real work.
 */
object NongorColors {
    /** Structure and headings — deep water blue. */
    val Deep = Color(0xFF0C3B5E)
    val DeepDark = Color(0xFF072941)

    /** Secondary structure — shallower water. */
    val Surf = Color(0xFF1B6E9C)

    /** "Act now", and anything not yet verified. */
    val Amber = Color(0xFFF5A524)
    val Caution = Color(0xFFE07B00)

    /** Life-threatening. Used sparingly so it never stops meaning what it means. */
    val Danger = Color(0xFFD62828)
    val DangerDark = Color(0xFF8E1616)

    /** Confirmed, safe, delivered, verified. */
    val Safe = Color(0xFF1B9C6B)

    val Ink = Color(0xFF101820)
    val Muted = Color(0xFF5A6672)
}
