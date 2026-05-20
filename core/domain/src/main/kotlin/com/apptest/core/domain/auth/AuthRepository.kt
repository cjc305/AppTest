package com.apptest.core.domain.auth

import com.apptest.core.common.AppResult
import com.apptest.core.common.AuthState
import com.apptest.core.domain.Repository
import kotlinx.coroutines.flow.StateFlow

/**
 * Cross-feature auth contract. Lives in `:core:domain` so `:feature:auth` (writer) and
 * `:feature:onboarding` / `:app` (readers) can share without inter-feature dep.
 *
 * Real impl: `:feature:auth/FakeAuthRepository` for V1; Supabase-backed `RealAuthRepository`
 * lands with APT-V1-R-041 / R-043.
 *
 * State machine (per `_specs/navigation.md §5`):
 *   SignedOut → NeedsOnboarding (sign-in success, new profile)
 *   SignedOut → Ready (sign-in success, existing profile + onboarded)
 *   NeedsOnboarding → Ready (markOnboardingComplete)
 *   any → SignedOut (signOut)
 */
interface AuthRepository : Repository {

    /** Observable for `:app/MainActivity` to drive NavHost startDestination. */
    val state: StateFlow<AuthState>

    /** Mock or real Google Sign-In. V1 fake emits NeedsOnboarding for first-time users. */
    suspend fun signInWithGoogle(): AppResult<Unit>

    /** Triggers email magic link send. */
    suspend fun requestMagicLink(email: String): AppResult<Unit>

    /** Verifies magic link token from deep-link arrival → emits NeedsOnboarding / Ready. */
    suspend fun verifyMagicLink(token: String): AppResult<Unit>

    /** Called by `:feature:onboarding` when 3-step wizard completes. */
    suspend fun markOnboardingComplete(): AppResult<Unit>

    /** Sign out → emit SignedOut + clear tokens. */
    suspend fun signOut(): AppResult<Unit>
}
