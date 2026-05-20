package com.apptest.feature.auth.data

import com.apptest.core.common.AppError
import com.apptest.core.common.AppResult
import com.apptest.core.common.AuthState
import com.apptest.core.domain.auth.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * V1 in-memory fake. Real Supabase Auth integration lands with APT-V1-R-043.
 *
 * Demo flow:
 * - signInWithGoogle → 600ms delay → NeedsOnboarding
 * - requestMagicLink → 400ms delay → Success (UI shows "sent" confirmation)
 * - verifyMagicLink → 400ms delay → NeedsOnboarding (always treats as new user for demo)
 * - markOnboardingComplete → Ready
 * - signOut → SignedOut
 */
@Singleton
class FakeAuthRepository @Inject constructor() : AuthRepository {

    private val _state = MutableStateFlow(AuthState.SignedOut)
    override val state: StateFlow<AuthState> = _state.asStateFlow()

    override suspend fun signInWithGoogle(idToken: String): AppResult<Unit> {
        delay(600)
        _state.value = AuthState.NeedsOnboarding
        return AppResult.Success(Unit)
    }

    override suspend fun requestMagicLink(email: String): AppResult<Unit> {
        delay(400)
        if (!email.contains("@") || !email.contains(".")) {
            return AppResult.Failure(AppError.Validation("email", "Invalid email format"))
        }
        // V1: pretend mail was sent
        return AppResult.Success(Unit)
    }

    override suspend fun verifyMagicLink(token: String): AppResult<Unit> {
        delay(400)
        if (token.isBlank()) {
            return AppResult.Failure(AppError.Validation("token", "Empty token"))
        }
        _state.value = AuthState.NeedsOnboarding
        return AppResult.Success(Unit)
    }

    override suspend fun markOnboardingComplete(): AppResult<Unit> {
        _state.value = AuthState.Ready
        return AppResult.Success(Unit)
    }

    override suspend fun signOut(): AppResult<Unit> {
        _state.value = AuthState.SignedOut
        return AppResult.Success(Unit)
    }
}
