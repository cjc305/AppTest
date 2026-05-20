package com.apptest.feature.appdetail.nav

import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.apptest.core.navigation.AppDeepLink
import com.apptest.core.navigation.AppDestination
import com.apptest.feature.appdetail.ui.AppDetailRoute

/**
 * AppDetail subgraph contribution. Owns deep-link bindings (custom scheme + App Links)
 * so caller doesn't need to know about them.
 */
fun NavGraphBuilder.appDetailGraph(
    onNavigateUp: () -> Unit,
    deepLinks: List<NavDeepLink> = listOf(
        navDeepLink { uriPattern = AppDeepLink.PATTERN_APP_DETAIL_CUSTOM },
        navDeepLink { uriPattern = AppDeepLink.PATTERN_APP_DETAIL_WEB },
    ),
) {
    composable<AppDestination.AppDetail>(deepLinks = deepLinks) {
        AppDetailRoute(onNavigateUp = onNavigateUp)
    }
}
