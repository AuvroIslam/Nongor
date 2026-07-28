package org.nongor.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.nongor.app.R
import org.nongor.app.ui.theme.BrandTealSoft

/**
 * The anchor mark in a soft disc.
 *
 * This replaces the three illustrated mascots the app used to carry. They were friendly, but
 * they were an owl in an app called *anchor* — the one thing a brand mark has to do is agree
 * with the name. Being the same vector as the launcher icon also means the icon a user taps
 * on their home screen is the icon that greets them inside the app.
 */
@Composable
fun AnchorBadge(size: Dp = 120.dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(BrandTealSoft.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.nongor_mark),
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .padding(size * 0.16f),
        )
    }
}
