package org.nongor.app.ui.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nongor.app.R
import org.nongor.app.ui.theme.AccentPurple

/**
 * Brand splash shown for a beat after the system splash: the Nongor owl with the name "নোঙর" beneath
 * it. Compose shapes the Bangla correctly, which a baked splash image can't guarantee. The owl
 * matches the system splash icon, so the two read as one continuous opening moment.
 */
@Composable
fun BrandSplashScreen() {
    val shown = remember { mutableStateOf(false) }
    val fade by animateFloatAsState(
        targetValue = if (shown.value) 1f else 0f,
        animationSpec = tween(durationMillis = 450),
        label = "splashFade",
    )
    // Trigger the fade-in on first composition.
    androidx.compose.runtime.LaunchedEffect(Unit) { shown.value = true }

    Column(
        Modifier.fillMaxSize().background(AccentPurple),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painterResource(R.drawable.nongor_logo), contentDescription = null,
            modifier = Modifier.size(200.dp).alpha(fade),
        )
        Spacer(Modifier.height(12.dp))
        androidx.compose.material3.Text(
            "নোঙর", color = Color.White, fontWeight = FontWeight.ExtraBold,
            fontSize = 52.sp, lineHeight = 56.sp, modifier = Modifier.alpha(fade),
        )
        androidx.compose.material3.Text(
            "NONGOR", color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.Bold,
            fontSize = 15.sp, letterSpacing = 6.sp, modifier = Modifier.alpha(fade),
        )
    }
}
