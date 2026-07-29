package org.nongor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.nongor.app.core.Triage
import org.nongor.app.data.RegionAssets
import org.nongor.app.data.SosEntry
import org.nongor.app.data.download.HfDownloadRepository
import org.nongor.app.ui.i18n.LocalBangla
import org.nongor.app.ui.community.CommunityScreen
import org.nongor.app.ui.community.CommunityViewModel
import org.nongor.app.ui.emergency.EmergencyScreen
import org.nongor.app.ui.family.FamilyScreen
import org.nongor.app.ui.family.FamilyViewModel
import org.nongor.app.ui.firstaid.FirstAidScreen
import org.nongor.app.ui.firstaid.FirstAidViewModel
import org.nongor.app.ui.gis.GisScreen
import org.nongor.app.ui.gis.GisViewModel
import org.nongor.app.ui.demo.Actions
import org.nongor.app.ui.guide.GuideScreen
import org.nongor.app.ui.mesh.MeshScreen
import org.nongor.app.ui.mesh.MeshViewModel
import org.nongor.app.ui.onboarding.OnboardingScreen
import org.nongor.app.ui.onboarding.OnboardingViewModel
import org.nongor.app.ui.settings.SettingsScreen
import org.nongor.app.ui.settings.SettingsViewModel
import org.nongor.app.ui.splash.BrandSplashScreen
import org.nongor.app.ui.summary.SummaryScreen
import org.nongor.app.ui.summary.SummaryViewModel
import org.nongor.app.ui.theme.NongorTheme
import org.nongor.app.ui.translate.TranslateScreen
import org.nongor.app.ui.translate.TranslateViewModel
import org.nongor.app.ui.triage.TriageScreen
import org.nongor.app.ui.triage.TriageViewModel
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import org.nongor.app.ui.shell.NongorShell
import org.nongor.app.ui.shell.Tab
import org.nongor.app.ui.shell.HelpTab
import org.nongor.app.ui.shell.MoveTab
import org.nongor.app.ui.shell.AreaTab

private object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val TRIAGE = "triage"
    const val FIRSTAID = "firstaid"
    const val GIS = "gis"
    const val SUMMARY = "summary"
    const val MESH = "mesh"
    const val GUIDE = "guide"
    const val SETTINGS = "settings"
    const val EMERGENCY = "emergency"
    const val COMMUNITY = "community"
    const val FAMILY = "family"
    const val TRANSLATE = "translate"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NongorTheme {
                NongorNavHost()
            }
        }
    }
}

@Composable
private fun NongorNavHost() {
    val context = LocalContext.current
    val app = context.applicationContext as NongorApplication
    val language by app.prefs.language.collectAsState()
    app.engineHolder.respondInBangla = language == "bn"
    // The intro is shown once. Whether the optional AI model was downloaded is irrelevant
    // here — Nongor's rescue tools do not depend on it, so declining must be a real choice
    // and not a prompt that returns on every launch.
    val introSeen by app.prefs.introSeen.collectAsState()
    val start = if (introSeen) Routes.HOME else Routes.ONBOARDING
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    // Seeds practice data for the drill: sample SOS reports plus family encounters around
    // wherever the user actually is. Both are labelled as practice and are purged the moment
    // a real report or a real encounter arrives.
    val seedDrill: () -> Unit = {
        if (app.sosRepository.entries.value.none { it.source == "drill" }) {
            RegionAssets.loadScenarios(context).forEach {
                app.sosRepository.add(SosEntry(it, Triage.fallbackTriage(it), source = "drill"))
            }
        }
        scope.launch {
            val here = runCatching { app.meshHub.myLocation() }.getOrNull()
            app.familyRepository.seedDrill(here)
        }
        Unit
    }

    fun toStartFromSplash() {
        navController.navigate(start) {
            popUpTo(Routes.SPLASH) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun appFactory() =
        androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as android.app.Application,
        )

    CompositionLocalProvider(LocalBangla provides (language == "bn")) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            BrandSplashScreen()
            androidx.compose.runtime.LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1300)
                toStartFromSplash()
            }
        }
        composable(Routes.ONBOARDING) {
            val vm: OnboardingViewModel = viewModel(factory = appFactory())
            val toHome: () -> Unit = {
                app.prefs.markIntroSeen()
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                    launchSingleTop = true
                }
            }
            OnboardingScreen(viewModel = vm, onFinished = toHome, onSkip = toHome)
        }
        composable(Routes.HOME) {
            // The tabbed shell replaces what used to be a ten-tile grid. Nongor opens on
            // Talk because the gap it exists to close is that the person in front of you
            // cannot tell you what is wrong.
            var tab by rememberSaveable { mutableStateOf(Tab.TALK) }
            val sosActive by app.sosSession.active.collectAsState()
            val sirenOn by app.sosSession.sirenOn.collectAsState()

            NongorShell(current = tab, onSelect = { tab = it }) {
                when (tab) {
                    Tab.TALK -> {
                        val vm: TranslateViewModel = viewModel(factory = appFactory())
                        TranslateScreen(
                            viewModel = vm,
                            onBack = null,
                            onOpenGuide = { navController.navigate(Routes.GUIDE) },
                            onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                            onSendAsSos = { note ->
                                app.pendingSosDraft = note
                                navController.navigate(Routes.MESH)
                            },
                        )
                    }
                    Tab.HELP -> HelpTab(
                        sosActive = sosActive,
                        sirenOn = sirenOn,
                        onStopSos = { app.sosSession.stop() },
                        onMesh = { navController.navigate(Routes.MESH) },
                        onEmergency = { navController.navigate(Routes.EMERGENCY) },
                        onFirstAid = { navController.navigate(Routes.FIRSTAID) },
                        onTriage = { navController.navigate(Routes.TRIAGE) },
                    )
                    Tab.MOVE -> MoveTab(
                        onShelter = { navController.navigate(Routes.GIS) },
                        onFamily = { navController.navigate(Routes.FAMILY) },
                    )
                    Tab.AREA -> AreaTab(
                        onBoard = { navController.navigate(Routes.COMMUNITY) },
                        onSummary = { navController.navigate(Routes.SUMMARY) },
                    )
                }
            }
        }
        composable(Routes.TRIAGE) {
            val vm: TriageViewModel = viewModel(factory = appFactory())
            TriageScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
        composable(Routes.FIRSTAID) {
            val vm: FirstAidViewModel = viewModel(factory = appFactory())
            FirstAidScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
        composable(Routes.GIS) {
            val vm: GisViewModel = viewModel(factory = appFactory())
            GisScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
        composable(Routes.SUMMARY) {
            val vm: SummaryViewModel = viewModel(factory = appFactory())
            SummaryScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
        composable(Routes.MESH) {
            val vm: MeshViewModel = viewModel(factory = appFactory())
            // A note composed on the translation screen arrives here prefilled, so the
            // volunteer never has to retype what the person just told them.
            val draft = app.pendingSosDraft
            app.pendingSosDraft = null
            MeshScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                prefill = draft.orEmpty(),
            )
        }
        composable(Routes.TRANSLATE) {
            val vm: TranslateViewModel = viewModel(factory = appFactory())
            TranslateScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onSendAsSos = { note ->
                    app.pendingSosDraft = note
                    navController.navigate(Routes.MESH)
                },
            )
        }
        composable(Routes.GUIDE) {
            GuideScreen(
                onBack = { navController.popBackStack() },
                onSeedDemo = seedDrill,
                actions = Actions(
                    triage = { navController.navigate(Routes.TRIAGE) },
                    firstAid = { navController.navigate(Routes.FIRSTAID) },
                    shelter = { navController.navigate(Routes.GIS) },
                    mesh = { navController.navigate(Routes.MESH) },
                    summary = { navController.navigate(Routes.SUMMARY) },
                    translate = { navController.navigate(Routes.TRANSLATE) },
                ),
            )
        }
        composable(Routes.EMERGENCY) {
            EmergencyScreen(
                onBack = { navController.popBackStack() },
                onMesh = { navController.navigate(Routes.MESH) },
            )
        }
        composable(Routes.COMMUNITY) {
            val vm: CommunityViewModel = viewModel(factory = appFactory())
            CommunityScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
        composable(Routes.FAMILY) {
            val vm: FamilyViewModel = viewModel(factory = appFactory())
            FamilyScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            val vm: SettingsViewModel = viewModel(factory = appFactory())
            SettingsScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
    }
    }
}
