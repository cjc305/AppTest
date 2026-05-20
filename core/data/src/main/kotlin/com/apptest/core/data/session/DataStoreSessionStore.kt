package com.apptest.core.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.apptest.core.domain.auth.TokenProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

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
) : SessionStore, TokenProvider {

    override val session: Flow<AuthSession?> = dataStore.data.map { prefs ->
        val jwt = prefs[KEY_JWT] ?: return@map null
        val refresh = prefs[KEY_REFRESH] ?: return@map null
        val exp = prefs[KEY_EXP] ?: return@map null
        AuthSession(jwt = jwt, refreshToken = refresh, expiresAtEpochMs = exp)
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

    override suspend fun token(): String? =
        session.firstOrNull()?.takeIf { !it.isExpired() }?.jwt

    private companion object {
        val KEY_JWT = stringPreferencesKey("auth_jwt")
        val KEY_REFRESH = stringPreferencesKey("auth_refresh_token")
        val KEY_EXP = longPreferencesKey("auth_expires_at_epoch_ms")
    }
}
