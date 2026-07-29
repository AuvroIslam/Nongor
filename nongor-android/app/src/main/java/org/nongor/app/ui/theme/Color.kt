package org.nongor.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Nongor's palette: deep teal on warm paper, with colour reserved for meaning.
 *
 * Two rules hold the whole thing together:
 *
 *  1. **Red means someone could die.** It appears on the 999 button and on a critical triage
 *     result, and nowhere else. If red is also the colour of a delete icon and a form error,
 *     it stops registering as an alarm.
 *  2. **Teal is structure, not decoration.** Headings, primary actions and active states.
 *     Everything that is merely present is a neutral.
 *
 * The paper-warm background rather than a cool grey is deliberate: this app is read outdoors
 * in glare, and a slightly warm off-white is easier on the eye than a blue-white that fights
 * the sunlight.
 */

// ---- Surfaces ----
val BgDark        = Color(0xFFF7F6F2)   // app background — warm paper
val BgMid         = Color(0xFFEFEDE6)   // subtle variant / pressed states
val BgCard        = Color(0xFFFFFFFF)   // cards and sheets

val GlassBg       = Color(0xFFFFFFFF)
val GlassBorder   = Color(0xFFE3E0D7)   // the hairline that replaces shadows

// ---- Brand teal ----
val BrandTeal     = Color(0xFF0B6E5F)   // primary
val BrandTealDeep = Color(0xFF064A3F)   // headings, splash, launcher tile
val BrandTealLite = Color(0xFF0E8C77)   // hover, soft fills, secondary
val BrandTealSoft = Color(0xFF9FD3C7)   // tinted backgrounds
val BrandSand     = Color(0xFFC9A227)   // warm counterpoint, used sparingly

// ---- Text ----
val TextPrimary   = Color(0xFF14201C)
val TextSecondary = Color(0xFF5C6B66)
val TextMuted     = Color(0xFF93A09B)

// ---- Semantic ----
val ErrorRed      = Color(0xFFC62828)   // life-threatening only
val CautionAmber  = Color(0xFFE08600)   // unverified, degraded, warn
val SafeGreen     = Color(0xFF1B8F62)   // confirmed, signed, delivered
// First aid. Deliberately not ErrorRed: that one is reserved for life-threatening, and a
// permanent red row on Home would spend the alarm colour on a menu item.
val AidRose       = Color(0xFFB32B62)

// ---- Aliases so every screen reaches for the same few names ----
val BrandBlue     = BrandTeal           // former primary name, kept to avoid a churn diff
val BrandBlueDeep = BrandTealDeep
val BrandBlueSoft = BrandTealSoft
val BrandBlueGlow = BrandTealLite
val BrandCoral    = Color(0xFFE0714F)
val SendGreen     = BrandTeal
val GemmaBlue     = BrandTealLite
val GemmaBlack    = TextPrimary
val GemmaGreen    = BrandTeal
val GemmaAvatarBg = BrandTeal
val Background    = BgDark
val Surface       = BgCard
val InputBar      = Color(0xFFF1EFE8)
val OnBackground  = TextPrimary
val Muted         = TextSecondary
val Divider       = GlassBorder
val UserBubble    = BrandTeal

/**
 * Feature tiles.
 *
 * Each pair is a very light wash plus a saturated foreground from the same hue, so the grid
 * reads as one family rather than a bag of sweets. Triage keeps the alarm red because that
 * tile genuinely is about life-threatening cases.
 */
val TileTriageBg    = Color(0xFFFBEAE7); val TileTriageFg    = ErrorRed
val TileAidBg       = Color(0xFFE3F3EA); val TileAidFg       = SafeGreen
val TileShelterBg   = Color(0xFFE4EFF2); val TileShelterFg   = Color(0xFF1F6D82)
val TileSummaryBg   = Color(0xFFE6F0EC); val TileSummaryFg   = BrandTealDeep
// Mesh SOS is the calling-for-help tool, so it carries the alarm red rather than a warm
// accent. Triage is red too, but that one is about *reading* danger; this is about
// signalling it. Nothing else in the grid is allowed near this hue.
val TileMeshBg      = Color(0xFFFBE9E7); val TileMeshFg      = ErrorRed
val TileChatBg      = Color(0xFFE1F0EC); val TileChatFg      = BrandTeal
val TileCommunityBg = Color(0xFFE0F1EE); val TileCommunityFg = Color(0xFF0F8377)
val TileFamilyBg    = Color(0xFFEAEFE6); val TileFamilyFg    = Color(0xFF4B6B49)
