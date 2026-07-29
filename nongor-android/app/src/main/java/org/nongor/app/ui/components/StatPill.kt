package org.nongor.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.nongor.app.ui.theme.GlassBorder
import org.nongor.app.ui.theme.ShapePill
import org.nongor.app.ui.theme.TextSecondary

/**
 * One count in a row of counts.
 *
 * Every pill in a row is built by this, so they share a height, a corner radius and a text
 * size. The previous version mixed a Material `AssistChip` with hand-rolled pills, which
 * differ in all three — and a row of counts that do not line up reads as broken long before
 * anyone works out why.
 */
@Composable
fun StatPill(
    label: String,
    value: Int,
    tint: Color? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    val content = tint ?: TextSecondary
    Row(
        Modifier
            .defaultMinSize(minHeight = 34.dp)
            .clip(ShapePill)
            .background(tint?.copy(alpha = 0.13f) ?: Color.Transparent)
            .then(
                if (tint == null) Modifier.border(1.dp, GlassBorder, ShapePill) else Modifier,
            )
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(7.dp))
        }
        Text(
            "$label $value",
            style = MaterialTheme.typography.labelLarge,
            color = content,
            fontWeight = FontWeight.Bold,
        )
    }
}
