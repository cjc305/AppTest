package com.apptest.app.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
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
 * Label key, resolved against the active [com.apptest.core.designsystem.theme.AppL10n] catalog
 * to support EN / ZH-TW per `_specs/mvp.md` §8 (real string resources land in APT-X-005).
 *
 * Note: catalog doesn't yet expose tab labels; for now we fall back to literal pairs in
 * [tabLabelFor]. Once `nav_tab_*` keys land in `AppStrings`, this enum collapses to a key lookup.
 */
internal enum class TopLevelLabelKey { Home, MyApps, Testing, Profile }

internal fun tabLabelFor(key: TopLevelLabelKey, isZh: Boolean): String = when (key) {
    TopLevelLabelKey.Home -> if (isZh) "首頁" else "Home"
    TopLevelLabelKey.MyApps -> if (isZh) "我的 Apps" else "My Apps"
    TopLevelLabelKey.Testing -> if (isZh) "測試" else "Testing"
    TopLevelLabelKey.Profile -> if (isZh) "個人" else "Profile"
}
