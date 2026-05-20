package com.apptest.app.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.apptest.core.navigation.AppDestination

/**
 * The 4 bottom-bar tabs that hang off [AppDestination.MainRoot].
 *
 * Order here = visual order in [com.apptest.core.ui.components.AppBottomBar].
 * `route` is the type-safe [AppDestination] target navigated to when the tab is selected.
 * `routeMatches` lets us highlight the tab when the user lands on the destination via deep-link
 * or restoreState (so the bottom-bar selection stays in sync with the back-stack).
 */
internal enum class MainTopLevelDestination(
    val id: String,
    val labelKey: TopLevelLabelKey,
    val icon: ImageVector,
    val route: AppDestination,
    private val routeClass: Class<out AppDestination>,
) {
    Home(
        id = "tab_home",
        labelKey = TopLevelLabelKey.Home,
        icon = Icons.Filled.Home,
        route = AppDestination.Home,
        routeClass = AppDestination.Home::class.java,
    ),
    MyApps(
        id = "tab_myapps",
        labelKey = TopLevelLabelKey.MyApps,
        icon = Icons.Filled.Star,
        route = AppDestination.MyApps,
        routeClass = AppDestination.MyApps::class.java,
    ),
    Testing(
        id = "tab_testing",
        labelKey = TopLevelLabelKey.Testing,
        icon = Icons.Filled.PlayArrow,
        route = AppDestination.Testing,
        routeClass = AppDestination.Testing::class.java,
    ),
    Profile(
        id = "tab_profile",
        labelKey = TopLevelLabelKey.Profile,
        icon = Icons.Filled.Person,
        route = AppDestination.Profile,
        routeClass = AppDestination.Profile::class.java,
    );

    @Suppress("UNCHECKED_CAST")
    fun matches(destination: NavDestination?): Boolean =
        destination?.hasRoute(routeClass.kotlin) == true

    companion object {
        fun fromDestination(destination: NavDestination?): MainTopLevelDestination? =
            entries.firstOrNull { it.matches(destination) }
    }
}

/**
 * Label key, resolved against the active [com.apptest.core.designsystem.theme.AppL10n] catalog.
 */
internal enum class TopLevelLabelKey { Home, MyApps, Testing, Profile }

@Composable
internal fun tabLabelFor(key: TopLevelLabelKey): String {
    val l = com.apptest.core.designsystem.theme.AppL10n.current
    return when (key) {
        TopLevelLabelKey.Home    -> l.nav_tab_home
        TopLevelLabelKey.MyApps  -> l.nav_tab_my_apps
        TopLevelLabelKey.Testing -> l.nav_tab_testing
        TopLevelLabelKey.Profile -> l.nav_tab_profile
    }
}
