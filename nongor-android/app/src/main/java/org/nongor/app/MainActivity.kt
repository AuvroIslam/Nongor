package org.nongor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.nongor.app.data.AppPrefs
import org.nongor.app.ui.NongorApp
import org.nongor.app.ui.i18n.LocalBangla
import org.nongor.app.ui.theme.NongorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = AppPrefs.get(this)
        setContent {
            val bangla by prefs.bangla.collectAsState()
            NongorTheme {
                CompositionLocalProvider(LocalBangla provides bangla) {
                    NongorApp(prefs)
                }
            }
        }
    }
}
