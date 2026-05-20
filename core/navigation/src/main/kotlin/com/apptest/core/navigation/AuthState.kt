package com.apptest.core.navigation

import com.apptest.core.common.AuthState

/**
 * Pure function mapping auth state → NavHost start destination. Caller side:
 * `val start = remember(authState) { startDestinationFor(authState) }`.
 */
fun startDestinationFor(state: AuthState): AppDestination = when (state) {
    AuthState.SignedOut -> AppDestination.AuthRoot
    AuthState.NeedsOnboarding -> AppDestination.OnboardingRoot
    AuthState.Ready -> AppDestination.MainRoot
}
