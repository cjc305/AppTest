package com.apptest.feature.onboarding.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.apptest.core.navigation.AppDestination
import com.apptest.feature.onboarding.ui.OnboardingRoute

/**
 * Onboarding subgraph contribution. Single composable since the 3-step wizard is one Screen
 * with internal step state (simpler than nested NavHost for V1).
 *
 * No nav callbacks — onboarding completes via `AuthRepository.markOnboardingComplete()` which
 * flips AuthState to Ready, triggering MainActivity to re-key NavHost to MainRoot.
 */
fun NavGraphBuilder.onboardingGraph() {
    composable<AppDestination.OnboardingRoot> { OnboardingRoute() }
}
