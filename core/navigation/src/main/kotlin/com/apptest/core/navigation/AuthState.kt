package com.apptest.core.navigation

import com.apptest.core.common.AuthState

/**
 * `AuthState` enum moved to `:core:common` (2026-05-19) so `:core:domain` can reference it
 * without depending on Android library. This file now hosts the **routing** mapping only.
 *
 * Callers importing `com.apptest.core.navigation.AuthState` should update to
 * `com.apptest.core.common.AuthState`.
 */

/**
 * Pure function mapping auth state → NavHost start destination. Caller side:
 * `val start = remember(authState) { startDestinationFor(authState) }`.
 */
fun startDestinationFor(state: AuthState): AppDestination = when (state) {
    AuthState.SignedOut -> AppDestination.AuthRoot
    AuthState.NeedsOnboarding -> AppDestination.OnboardingRoot
    AuthState.Ready -> AppDestination.MainRoot
}
