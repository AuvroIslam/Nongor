package org.nongor.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.nongor.app.R

/**
 * The Nongor mark, exactly as it appears on the launcher.
 *
 * This replaces the three illustrated mascots the app used to carry. They were friendly, but
 * they were an owl in an app called *anchor* — the one thing a brand mark has to do is agree
 * with the name.
 */
@Composable
fun AnchorBadge(size: Dp = 120.dp, modifier: Modifier = Modifier) {
    // The launcher mark itself, not a paler variant of it. Whatever the user tapped on their
    // home screen is what should greet them inside the app — a second, softer version of the
    // logo reads as a different product.
    Image(
        painter = painterResource(R.drawable.nongor_app_icon),
        contentDescription = null,
        modifier = modifier
            .size(size)
            .clip(CircleShape),
    )
}
