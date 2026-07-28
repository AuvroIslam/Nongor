package org.nongor.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Nongor's palette: deep water blue for structure, amber for "act now", one red for danger.
 *
 * The light, low-saturation background is deliberate — a dark theme photographs better but a
 * bright screen is what is actually readable outdoors at midday, which is when an evacuation
 * happens. Semantic meanings live in [NongorColors]; this file is the raw brand ramp.
 */

// ---- Surfaces ----
val BgDark        = Color(0xFFF4F7F9)   // app background (cool off-white)
val BgMid         = Color(0xFFE6EEF3)   // subtle variant
val BgCard        = Color(0xFFFFFFFF)   // cards / surface

val GlassBg       = Color(0xFFFFFFFF)
val GlassBorder   = Color(0xFFDCE6EC)

// ---- Brand blues ----
val BrandBlue     = Color(0xFF0E5A85)   // primary accent
val BrandBlueDeep = Color(0xFF0C3B5E)   // headings, launcher tile, splash
val BrandTeal     = Color(0xFF0E7C86)   // secondary accent (translation)
val BrandBlueSoft = Color(0xFF8FC3DE)
val BrandBlueGlow = Color(0xFF3F8DB8)
val BrandCoral    = Color(0xFFEC6A5E)

// ---- Text ----
val TextPrimary   = Color(0xFF10202B)
val TextSecondary = Color(0xFF4E6472)
val TextMuted     = Color(0xFF8FA0AC)

// ---- Functional ----
val SendGreen     = BrandBlue
val ErrorRed      = Color(0xFFD62828)

// ---- Aliases kept so every screen reaches for the same few names ----
val GemmaBlue     = BrandTeal
val GemmaBlack    = TextPrimary
val GemmaGreen    = BrandBlue
val GemmaAvatarBg = BrandBlue
val Background    = BgDark
val Surface       = BgCard
val InputBar      = Color(0xFFEAF1F5)
val OnBackground  = TextPrimary
val Muted         = TextSecondary
val Divider       = Color(0xFFDCE6EC)
val UserBubble    = BrandBlue

// ---- Feature tiles ----
val TileTriageBg  = Color(0xFFFDECEA); val TileTriageFg  = Color(0xFFD62828)
val TileAidBg     = Color(0xFFE4F6EC); val TileAidFg     = Color(0xFF1B9C6B)
val TileShelterBg = Color(0xFFE7F0FE); val TileShelterFg = Color(0xFF2E7DF5)
val TileSummaryBg = Color(0xFFE3EEF5); val TileSummaryFg = Color(0xFF0E5A85)
val TileMeshBg    = Color(0xFFFFF0E1); val TileMeshFg    = Color(0xFFE07B00)
val TileChatBg    = Color(0xFFDFF1F3); val TileChatFg    = Color(0xFF0E7C86)
val TileCommunityBg = Color(0xFFDFF5F1); val TileCommunityFg = Color(0xFF12A594)
val TileFamilyBg  = Color(0xFFE4EBF2); val TileFamilyFg  = Color(0xFF3C5A78)
