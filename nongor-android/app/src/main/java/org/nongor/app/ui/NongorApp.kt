package org.nongor.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.nongor.app.data.AppPrefs
import org.nongor.app.ui.home.HomeScreen
import org.nongor.app.ui.i18n.t
import org.nongor.app.ui.settings.SettingsScreen

@Composable
fun NongorApp(prefs: AppPrefs) {
    val nav = rememberNavController()
    val back: () -> Unit = { nav.popBackStack() }

    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigate = { nav.navigate(it) },
                onToggleLanguage = { prefs.setBangla(!prefs.bangla.value) },
            )
        }
        composable(Routes.SETTINGS) { SettingsScreen(prefs, back) }

        // Feature screens land here as the sprint progresses.
        composable(Routes.TRANSLATE) { UnderConstruction("Translate", "অনুবাদ", back) }
        composable(Routes.EMERGENCY) { UnderConstruction("Emergency call", "জরুরি কল", back) }
        composable(Routes.FIRST_AID) { UnderConstruction("First aid", "প্রাথমিক চিকিৎসা", back) }
        composable(Routes.SHELTER) { UnderConstruction("Safe shelter", "নিরাপদ আশ্রয়", back) }
        composable(Routes.MESH) { UnderConstruction("Mesh SOS", "মেশ SOS", back) }
        composable(Routes.TRIAGE) { UnderConstruction("Rescue triage", "উদ্ধার ট্রায়াজ", back) }
        composable(Routes.COMMUNITY) { UnderConstruction("Area board", "এলাকা বোর্ড", back) }
        composable(Routes.FAMILY) { UnderConstruction("Find family", "পরিবার খুঁজুন", back) }
        composable(Routes.SUMMARY) { UnderConstruction("Coordinator", "সমন্বয়", back) }
        composable(Routes.ASSISTANT) { UnderConstruction("Ask Nongor", "নোঙরকে জিজ্ঞাসা", back) }
        composable(Routes.GUIDE) { UnderConstruction("Guide & drill", "গাইড ও মহড়া", back) }
    }
}

@Composable
private fun UnderConstruction(en: String, bn: String, onBack: () -> Unit) {
    org.nongor.app.ui.components.NongorScaffold(title = t(en, bn), onBack = onBack) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                t("Being built right now.", "এখনই তৈরি হচ্ছে।"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
