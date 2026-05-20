package com.apptest.feature.home.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.apptest.core.navigation.AppDestination
import com.apptest.feature.home.ui.HomeRoute

/**
 * Home subgraph contribution. Called from `:app/nav/AppNavHost` inside the top-level `NavHost`.
 *
 * Public API per `_specs/feature_modules.md §2`: returns the `composable<AppDestination.Home>`
 * entry; everything else (Screen / ViewModel / Repo / Models) stays `internal`.
 */
fun NavGraphBuilder.homeGraph(
    onAppClick: (String) -> Unit,
) {
    composable<AppDestination.Home> {
        HomeRoute(onAppClick = onAppClick)
    }
}
