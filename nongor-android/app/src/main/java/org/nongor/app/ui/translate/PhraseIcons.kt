package org.nongor.app.ui.translate

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.ChildFriendly
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PregnantWoman
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Signpost
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.Wc
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The pictogram for each phrase.
 *
 * This is not decoration. When there is no translation for someone's language — which is
 * the normal case for Marma, Kokborok, Santali and Garo — the pictogram plus the tap-reply
 * is the entire conversation, so every phrase must have one that reads at arm's length.
 */
fun phraseIcon(name: String): ImageVector = when (name) {
    "help", "hand" -> Icons.Filled.PanTool
    "shield" -> Icons.Filled.Shield
    "badge" -> Icons.Filled.Badge
    "hearing" -> Icons.Filled.Hearing
    "translate" -> Icons.Filled.Translate
    "healing" -> Icons.Filled.Healing
    "person_pin" -> Icons.Filled.PersonPin
    "blood" -> Icons.Filled.Bloodtype
    "air" -> Icons.Filled.Air
    "accessible" -> Icons.Filled.Accessible
    "mood" -> Icons.Filled.Mood
    "scale" -> Icons.Filled.Favorite
    "pregnant" -> Icons.Filled.PregnantWoman
    "medication" -> Icons.Filled.Medication
    "pets" -> Icons.Filled.Pets
    "groups" -> Icons.Filled.Groups
    "trapped" -> Icons.Filled.Construction
    "walk", "rescue" -> Icons.AutoMirrored.Filled.DirectionsWalk
    "family" -> Icons.Filled.FamilyRestroom
    "arrow" -> Icons.Filled.ArrowForward
    "timer" -> Icons.Filled.Timer
    "water" -> Icons.Filled.WaterDrop
    "food" -> Icons.Filled.Restaurant
    "boil" -> Icons.Filled.LocalFireDepartment
    "clock" -> Icons.Filled.AccessTime
    "baby" -> Icons.Filled.ChildFriendly
    "home" -> Icons.Filled.Home
    "house" -> Icons.Filled.House
    "night" -> Icons.Filled.NightsStay
    "clothes" -> Icons.Filled.Checkroom
    "toilet" -> Icons.Filled.Wc
    "search" -> Icons.Filled.Search
    "person" -> Icons.Filled.Person
    "cake" -> Icons.Filled.Cake
    "eye" -> Icons.Filled.Visibility
    "child" -> Icons.Filled.ChildCare
    "wave" -> Icons.Filled.Waves
    "bolt" -> Icons.Filled.Bolt
    "building" -> Icons.Filled.Apartment
    "hill" -> Icons.Filled.Terrain
    "quiet" -> Icons.Filled.VolumeOff
    "place" -> Icons.Filled.Place
    "signpost" -> Icons.Filled.Signpost
    "ruler" -> Icons.Filled.Straighten
    "boat" -> Icons.Filled.Sailing
    "block" -> Icons.Filled.Block
    "danger" -> Icons.Filled.Warning
    "yes" -> Icons.Filled.Favorite
    "no" -> Icons.Filled.Close
    else -> Icons.AutoMirrored.Filled.HelpOutline
}
