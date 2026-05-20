package com.apptest.core.common

/**
 * Routing-level auth state per `_specs/navigation.md §5`. Lives in `:core:common` so both
 * `:core:domain` (AuthRepository contract) and `:core:navigation` (startDestinationFor)
 * can import without creating a domain↔navigation cycle.
 *
 * Producer: `:feature:auth` AuthRepository
 * Consumer: `:app` MainActivity (for startDestination); `:feature:onboarding` (marks complete)
 */
enum class AuthState {
    SignedOut,         // no session → show AppDestination.AuthRoot
    NeedsOnboarding,   // session OK but profile.onboarding_completed=false → AppDestination.OnboardingRoot
    Ready,             // session + onboarded → AppDestination.MainRoot
}
