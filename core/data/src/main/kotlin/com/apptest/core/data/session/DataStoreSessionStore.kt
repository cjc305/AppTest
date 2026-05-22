package com.apptest.core.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.apptest.core.data.di.ApplicationScope
import com.apptest.core.domain.auth.TokenProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Preferences-DataStore-backed implementation. File name `auth_session.preferences_pb`
 * is created by `SessionModule` via `preferencesDataStoreFile()`.
 *
 * Implements both [SessionStore] (for writers) and [TokenProvider] (for `:core:network`
 * interceptor) — same singleton instance is double-bound in Hilt.
 *
 * Token freshness: [token] returns `null` when the persisted session is expired so the
 * interceptor doesn't attach a stale JWT. Refresh-flow (mint new JWT from refresh token)
 * lives in `:feature:auth/RealAuthRepository` which writes the new session back here.
 */
@Singleton
class DataStoreSessionStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationScope scope: CoroutineScope,
) : SessionStore, TokenProvider {

    override val session: Flow<AuthSession?> = dataStore.data.map { prefs ->
        val jwt = prefs[KEY_JWT] ?: return@map null
        val refresh = prefs[KEY_REFRESH] ?: return@map null
        val exp = prefs[KEY_EXP] ?: return@map null
        AuthSession(jwt = jwt, refreshToken = refresh, expiresAtEpochMs = exp)
    }

    /**
     * In-memory token mirror updated reactively from [session]. Lets the OkHttp
     * [com.apptest.core.network.interceptor.AuthInterceptor] read the current token
     * synchronously without serializing every request through a `runBlocking` DataStore read
     * (which otherwise queues requests behind DataStore's internal Mutex).
     */
    @Volatile private var cachedToken: String? = null

    init {
        scope.launch {
            session.collect { s ->
                cachedToken = s?.takeIf { !it.isExpired() }?.jwt
            }
        }
    }

    override suspend fun save(session: AuthSession) {
        dataStore.edit { prefs ->
            prefs[KEY_JWT] = session.jwt
            prefs[KEY_REFRESH] = session.refreshToken
            prefs[KEY_EXP] = session.expiresAtEpochMs
        }
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    override suspend fun token(): String? = cachedToken
        ?: session.firstOrNull()?.takeIf { !it.isExpired() }?.jwt

    /** Synchronous accessor used by OkHttp interceptors (no coroutine context available). */
    override fun tokenBlocking(): String? = cachedToken

    private companion object {
        val KEY_JWT = stringPreferencesKey("auth_jwt")
        val KEY_REFRESH = stringPreferencesKey("auth_refresh_token")
        val KEY_EXP = longPreferencesKey("auth_expires_at_epoch_ms")
    }
}
