package com.apptest.core.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.apptest.core.common.AppError
import com.apptest.core.common.AppResult
import com.apptest.core.common.AuthState
import com.apptest.core.common.EmailValidator
import com.apptest.core.common.jwtExpiryEpochMs
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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
        if (!EmailValidator.isValid(email)) {
            return AppResult.Failure(AppError.Validation("email", "Invalid email format"))
        }
        pendingEmail = email
        return runCatching {
            authApiService.sendOtp(OtpRequest(email = email))
            AppResult.Success(Unit)
        }.getOrElse { AppResult.Failure(mapError(it)) }
    }

    // MED-011: email is now passed by the caller (ViewModel has it from SavedStateHandle, which
    // survives process death). pendingEmail is still updated for non-process-death paths but is
    // no longer the source of truth for verifyMagicLink.
    override suspend fun verifyMagicLink(email: String, token: String): AppResult<Unit> {
        if (email.isBlank()) {
            return AppResult.Failure(AppError.Validation("email", "Email must not be blank"))
        }
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
                    // MED-003: prefer JWT exp claim; fall back to response.expiresIn if missing.
                    expiresAtEpochMs = response.accessToken.jwtExpiryEpochMs()
                        ?: (System.currentTimeMillis() + response.expiresIn * 1000L),
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
        val jwt = sessionStore.session.firstOrNull()?.jwt
        if (jwt != null) {
            runCatching { authApiService.signOut(bearer = "Bearer $jwt") } // best-effort
        }
        pendingEmail = null
        sessionStore.clear()
        // HIGH-9 fix: previously only KEY_ONBOARDING was cleared, leaving user-scoped UI state
        // (Home skipped matches + Inbox read notifications) on the device → cross-account leak
        // when the next user signs in on the same device.
        dataStore.edit { prefs ->
            prefs.remove(KEY_ONBOARDING)
            prefs.remove(KEY_HOME_SKIPPED_MATCH_IDS)
            prefs.remove(KEY_INBOX_READ_NOTIFICATION_IDS)
        }
        return AppResult.Success(Unit) // AuthState → SignedOut via reactive Flow
    }

    override suspend fun signInWithGoogle(idToken: String): AppResult<Unit> {
        return runCatching {
            val response = authApiService.signInWithIdToken(
                request = com.apptest.core.network.auth.GoogleIdTokenRequest(idToken = idToken),
            )
            sessionStore.save(
                AuthSession(
                    jwt = response.accessToken,
                    refreshToken = response.refreshToken,
                    // MED-003: prefer JWT exp claim; fall back to response.expiresIn if missing.
                    expiresAtEpochMs = response.accessToken.jwtExpiryEpochMs()
                        ?: (System.currentTimeMillis() + response.expiresIn * 1000L),
                ),
            )
            // MED-1 fix: M-4 added pendingEmail clear in signOut(), but not here — stale email
            // could re-bind to a magic-link verify call after Google sign-in.
            pendingEmail = null
            AppResult.Success(Unit)
        }.getOrElse { AppResult.Failure(mapError(it)) }
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private fun mapError(t: Throwable): AppError = when (t) {
        is HttpException -> AppError.Http(t.code(), t.message())
        else -> AppError.fromThrowable(t)
    }

    private companion object {
        val KEY_ONBOARDING = booleanPreferencesKey("onboarding_complete")
        // Keys also written by other repositories; cleared here on sign-out so the next user
        // on this device doesn't inherit cross-account UI state (HIGH-9).
        val KEY_HOME_SKIPPED_MATCH_IDS = stringSetPreferencesKey("home_skipped_match_ids")
        val KEY_INBOX_READ_NOTIFICATION_IDS = stringSetPreferencesKey("inbox_read_notification_ids")
    }
}
