package com.apptest.feature.auth.nav

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.apptest.core.navigation.AppDestination
import com.apptest.feature.auth.ui.signin.SignInRoute
import com.apptest.feature.auth.ui.verify.EmailVerifyRoute

/**
 * Auth subgraph contribution. Mounts SignIn + EmailVerify entries.
 *
 * Navigation triggered by AuthState change is handled at NavHost level — when MainActivity's
 * `authRepo.state` flips to NeedsOnboarding / Ready, the NavHost re-keys startDestination
 * via `remember(authState)`, popping AuthRoot off the stack automatically.
 */
fun NavGraphBuilder.authGraph(navController: NavController) {
    composable<AppDestination.SignIn> {
        SignInRoute(
            onNavigateToVerify = { email ->
                navController.navigate(AppDestination.EmailVerify(email)) {
                    // Prevent double-destination if user double-taps before navigation lands.
                    launchSingleTop = true
                }
            },
        )
    }
    composable<AppDestination.EmailVerify> { EmailVerifyRoute() }
}
