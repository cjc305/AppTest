package com.apptest.app.nav

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.apptest.core.designsystem.components.AppText
import com.apptest.core.designsystem.components.AppVSpacer
import com.apptest.core.designsystem.spacing.AppSpacing
import com.apptest.core.navigation.AppDestination
import com.apptest.core.ui.components.AppBottomBar
import com.apptest.core.ui.components.AppBottomDest
import com.apptest.core.ui.components.AppButton
import com.apptest.core.ui.templates.ScreenScaffold
import com.apptest.feature.appdetail.nav.appDetailGraph
import com.apptest.feature.auth.nav.authGraph
import com.apptest.feature.home.nav.homeGraph
import com.apptest.feature.inbox.nav.inboxGraph
import com.apptest.feature.myapps.nav.myAppsGraph
import com.apptest.feature.onboarding.nav.onboardingGraph
import com.apptest.feature.profile.nav.profileGraph
import com.apptest.feature.testing.nav.testingGraph

/**
 * Top-level NavHost wiring all 13 [AppDestination] entries.
 *
 * Layout pattern (per `_specs/navigation.md` §6 + NowInAndroid):
 * - Wrap the [NavHost] in a Material3 [Scaffold] whose `bottomBar` slot conditionally renders
 *   [AppBottomBar] when the current back-stack entry matches one of the 4
 *   [MainTopLevelDestination] tabs. Outer scaffold consumes zero window insets — feature
 *   [ScreenScaffold] instances inside each Route handle status / IME insets themselves.
 * - [AppDestination.MainRoot] is a redirect-only composable that pops itself and lands on
 *   [AppDestination.Home] (the default tab) so back-press on Home exits the app cleanly.
 *
 * Outstanding stubs:
 * - [AppDestination.Settings] — minimal screen with locale + sign-out actions wired
 *   inline (no `:feature:settings` module yet).
 */
@Composable
@Suppress("UNUSED_PARAMETER")
fun AppNavHost(
    startDestination: AppDestination,
    windowSizeClass: WindowSizeClass,
    onShareInvite: (String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTopLevel = MainTopLevelDestination.fromDestination(backStackEntry?.destination)
    // Resolve tab labels at composable scope (tabLabelFor is @Composable)
    val tabHome    = tabLabelFor(TopLevelLabelKey.Home)
    val tabMyApps  = tabLabelFor(TopLevelLabelKey.MyApps)
    val tabTesting = tabLabelFor(TopLevelLabelKey.Testing)
    val tabProfile = tabLabelFor(TopLevelLabelKey.Profile)

    // Outer Scaffold owns only the conditional bottom bar; leave system-bar insets to the
    // feature ScreenScaffolds inside (avoids double-padding on edge-to-edge layouts).
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (currentTopLevel != null) {
                val tabLabelMap = mapOf(
                    TopLevelLabelKey.Home to tabHome,
                    TopLevelLabelKey.MyApps to tabMyApps,
                    TopLevelLabelKey.Testing to tabTesting,
                    TopLevelLabelKey.Profile to tabProfile,
                )
                val destinations = MainTopLevelDestination.entries.map { tab ->
                    AppBottomDest(
                        id = tab.id,
                        label = tabLabelMap[tab.labelKey] ?: tab.labelKey.name,
                        icon = tab.icon,
                    )
                }
                AppBottomBar(
                    destinations = destinations,
                    current = destinations.first { it.id == currentTopLevel.id },
                    onSelect = { selected ->
                        val target = MainTopLevelDestination.entries.first { it.id == selected.id }
                        navController.navigate(target.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<AppDestination.AuthRoot> { SignInRedirect(navController) }
            composable<AppDestination.MainRoot> { MainRootRedirect(navController) }

            authGraph()
            onboardingGraph()

            homeGraph(
                onAppClick = { appId -> navController.navigate(AppDestination.AppDetail(appId)) },
            )
            myAppsGraph(
                onNavigateToEditor = { appId -> navController.navigate(AppDestination.AppEditor(appId)) },
                onNavigateUp = { navController.popBackStack() },
            )
            appDetailGraph(
                onNavigateUp = { navController.popBackStack() },
            )
            testingGraph(
                onTestClick = { appId -> navController.navigate(AppDestination.AppDetail(appId)) },
                onProofClick = { proofId -> navController.navigate(AppDestination.ProofViewer(proofId)) },
            )
            profileGraph(
                onSettingsClick = { navController.navigate(AppDestination.Settings) },
                onInboxClick = { navController.navigate(AppDestination.Inbox) },
                onProofClick = { proofId -> navController.navigate(AppDestination.ProofViewer(proofId)) },
                onInviteClick = {
                    val uri = com.apptest.core.navigation.AppDeepLink.inviteWithRef("user")
                    onShareInvite(uri)
                },
            )
            inboxGraph(
                onNavigateUp = { navController.popBackStack() },
                onItemDeepLink = { uriString ->
                    val parsed = runCatching {
                        com.apptest.core.navigation.AppDeepLink.parse(android.net.Uri.parse(uriString))
                    }.getOrNull()
                    parsed?.let { navController.navigate(it) }
                },
            )

            composable<AppDestination.Settings> {
                SettingsStub(
                    onSignOut = onSignOut,
                    onBack = { navController.popBackStack() },
                )
            }

            composable<AppDestination.ProofViewer> { entry ->
                val args = entry.toRoute<AppDestination.ProofViewer>()
                com.apptest.app.proof.ProofViewerScreen(
                    proofId = args.proofId,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

@Composable
private fun SignInRedirect(nav: NavHostController) {
    LaunchedEffect(Unit) {
        nav.navigate(AppDestination.SignIn) {
            popUpTo<AppDestination.AuthRoot> { inclusive = true }
        }
    }
}

@Composable
private fun MainRootRedirect(nav: NavHostController) {
    LaunchedEffect(Unit) {
        nav.navigate(AppDestination.Home) {
            popUpTo<AppDestination.MainRoot> { inclusive = true }
        }
    }
}

@Composable
private fun SettingsStub(onSignOut: () -> Unit, onBack: () -> Unit) {
    val l = com.apptest.core.designsystem.theme.AppL10n.current
    ScreenScaffold { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).padding(AppSpacing.Lg),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.Md),
            ) {
                AppText(
                    text = l.settings_title,
                    style = MaterialTheme.typography.headlineMedium,
                )
                AppText(
                    text = l.settings_locale_note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppVSpacer(AppSpacing.Md)
                AppButton(l.cta_sign_out, onSignOut)
                AppVSpacer(AppSpacing.Sm)
                AppButton(l.cta_back, onBack)
            }
        }
    }
}
