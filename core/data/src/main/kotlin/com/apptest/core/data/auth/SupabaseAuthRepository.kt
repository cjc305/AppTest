package com.apptest.core.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.apptest.core.common.AppError
import com.apptest.core.common.AppResult
import com.apptest.core.common.AuthState
import com.apptest.core.data.di.ApplicationScope
import com.apptest.core.data.session.AuthSession
import com.apptest.core.data.session.SessionStore
import com.apptest.core.domain.auth.AuthRepository
import com.apptest.core.network.auth.OtpRequest
import com.apptest.core.network.auth.SupabaseAuthApiService
import com.apptest.core.network.auth.VerifyOtpRequest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.HttpException

/**
 * Real Supabase-backed auth. Replaces [com.apptest.feature.auth.data.FakeAuthRepository].
 *
 * State machine driven reactively: combines [SessionStore.session] + onboarding flag in
 * DataStore → [AuthState]. No explicit state mutation except via side-effect of saving/clearing
 * the session or toggling the onboarding flag.
 *
 * Magic-link flow (V1 entry path):
 *   1. [requestMagicLink] → POST /auth/v1/otp  (sends 6-digit code + link to inbox)
 *   2. [verifyMagicLink]  → POST /auth/v1/verify (user enters code → issues JWT)
 *   3. [markOnboardingComplete] → flips DataStore flag → state becomes [AuthState.Ready]
 */
@Singleton
class SupabaseAuthRepository @Inject constructor(
    private val authApiService: SupabaseAuthApiService,
    private val sessionStore: SessionStore,
    private val dataStore: DataStore<Preferences>,
    @ApplicationScope private val scope: CoroutineScope,
) : AuthRepository {

    private val _state = MutableStateFlow<AuthState>(AuthState.SignedOut)
    override val state: StateFlow<AuthState> = _state.asStateFlow()

    /** Temporarily stores email between [requestMagicLink] and [verifyMagicLink]. */
    @Volatile private var pendingEmail: String? = null

    init {
        // Reactively derive AuthState from persisted session + onboarding flag.
        scope.launch {
            val onboardingFlow = dataStore.data.map { it[KEY_ONBOARDING] ?: false }
            sessionStore.session.combine(onboardingFlow) { session, onboarded ->
                when {
                    session == null || session.isExpired() -> AuthState.SignedOut
                    onboarded -> AuthState.Ready
                    else -> AuthState.NeedsOnboarding
                }
            }.collect { _state.value = it }
        }
    }

    override suspend fun requestMagicLink(email: String): AppResult<Unit> {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return AppResult.Failure(AppError.Validation("email", "Invalid email format"))
        }
        pendingEmail = email
        return runCatching {
            authApiService.sendOtp(OtpRequest(email = email)).close()
            AppResult.Success(Unit)
        }.getOrElse { AppResult.Failure(mapError(it)) }
    }

    override suspend fun verifyMagicLink(token: String): AppResult<Unit> {
        val email = pendingEmail
            ?: return AppResult.Failure(AppError.Validation("email", "No pending sign-in — call requestMagicLink first"))
        if (token.isBlank()) {
            return AppResult.Failure(AppError.Validation("token", "Token must not be blank"))
        }
        return runCatching {
            val response = authApiService.verifyOtp(
                VerifyOtpRequest(type = "email", token = token, email = email),
            )
            sessionStore.save(
                AuthSession(
                    jwt = response.accessToken,
                    refreshToken = response.refreshToken,
                    expiresAtEpochMs = System.currentTimeMillis() + response.expiresIn * 1000L,
                ),
            )
            pendingEmail = null
            AppResult.Success(Unit) // AuthState updated by reactive Flow
        }.getOrElse { AppResult.Failure(mapError(it)) }
    }

    override suspend fun markOnboardingComplete(): AppResult<Unit> {
        dataStore.edit { it[KEY_ONBOARDING] = true }
        return AppResult.Success(Unit) // AuthState updated by reactive Flow
    }

    override suspend fun signOut(): AppResult<Unit> {
        runCatching { authApiService.signOut().close() } // best-effort, ignore errors
        sessionStore.clear()
        dataStore.edit { it.remove(KEY_ONBOARDING) }
        return AppResult.Success(Unit) // AuthState → SignedOut via reactive Flow
    }

    /** Google Sign-In deferred to V2. */
    override suspend fun signInWithGoogle(): AppResult<Unit> =
        AppResult.Failure(AppError.Auth(AppError.AuthReason.SignInCancelled, "Google Sign-In arrives in V2"))

    // ─── helpers ─────────────────────────────────────────────────────────────

    private fun mapError(t: Throwable): AppError = when (t) {
        is HttpException -> AppError.Http(t.code(), t.message())
        else -> AppError.fromThrowable(t)
    }

    private companion object {
        val KEY_ONBOARDING = booleanPreferencesKey("onboarding_complete")
    }
}
