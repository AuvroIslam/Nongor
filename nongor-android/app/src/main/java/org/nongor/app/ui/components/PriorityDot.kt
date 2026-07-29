package org.nongor.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.nongor.app.ui.theme.CautionAmber
import org.nongor.app.ui.theme.ErrorRed
import org.nongor.app.ui.theme.SafeGreen
import org.nongor.app.ui.theme.ShapePill

/**
 * Severity, as a drawn dot.
 *
 * These used to be the coloured-circle emoji. Emoji are the wrong tool for a status
 * indicator: the exact hue is decided by whichever font the phone happens to ship, they are
 * a different size from the text beside them, they cannot be tinted to match the palette,
 * and on some low-end handsets they render as an empty rectangle — which on a triage queue
 * means the one column that says who is dying quietly stops working.
 */
private fun colourFor(priority: String): Color = when (priority.lowercase()) {
    "critical" -> ErrorRed
    "high" -> CautionAmber
    "moderate" -> Color(0xFFB8860B)
    else -> SafeGreen
}

@Composable
fun PriorityDot(priority: String, size: Dp = 10.dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(colourFor(priority)),
    )
}

/** A dot and a count. Built on [StatPill] so it matches every other pill in the row. */
@Composable
fun PriorityCount(priority: String, count: Int, label: String) {
    StatPill(
        label = label,
        value = count,
        tint = colourFor(priority),
        leading = { PriorityDot(priority, 8.dp) },
    )
}

/** Exposed so screens can tint other things (a card, a rule) to match a priority. */
fun priorityColour(priority: String): Color = colourFor(priority)
