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
 * Preferences-DataStore-backed implementation. File name `auth_session.preferences_pb`.
 *
 * Implements both [SessionStore] (writers) and [TokenProvider] (reader for AuthInterceptor) —
 * same singleton instance is double-bound in Hilt.
 *
 * Token freshness: the in-memory mirror caches the **session snapshot**, NOT a derived
 * pre-validated JWT. `tokenBlocking()` re-checks expiry on every read so a JWT that just
 * crossed its TTL is treated as null immediately, without waiting for a DataStore flow
 * re-emit (which only fires on value change — wall-clock expiry is silent to Flow).
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
     * In-memory mirror of the **persisted snapshot** (not a pre-validated JWT). Updated
     * reactively from [session]. The interceptor re-evaluates [AuthSession.isExpired] on each
     * read so a JWT that just expired silently (no DataStore write occurred) won't be sent.
     *
     * HIGH-1 fix: previous code cached the JWT String directly, validated only at collect time.
     * Once a valid JWT was cached, expiry was never re-checked → stale token sent for hours
     * until the user explicitly signed out.
     */
    @Volatile private var snapshot: AuthSession? = null

    init {
        scope.launch {
            session.collect { s -> snapshot = s }
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

    override suspend fun token(): String? =
        (snapshot ?: session.firstOrNull())?.takeIf { !it.isExpired() }?.jwt

    /** Synchronous accessor used by OkHttp interceptors (no coroutine context available). */
    override fun tokenBlocking(): String? =
        snapshot?.takeIf { !it.isExpired() }?.jwt

    private companion object {
        val KEY_JWT = stringPreferencesKey("auth_jwt")
        val KEY_REFRESH = stringPreferencesKey("auth_refresh_token")
        val KEY_EXP = longPreferencesKey("auth_expires_at_epoch_ms")
    }
}
