package com.apptest.core.data.session

import kotlinx.coroutines.flow.Flow

/**
 * Read + write contract for the persisted [AuthSession]. Consumed by `:feature:auth` (real
 * Supabase impl writes here on sign-in / refresh) and by `:app/MainActivity` (read-only
 * observation to map session presence to [com.apptest.core.common.AuthState]).
 *
 * `:core:network` does **not** depend on this — it uses [com.apptest.core.domain.auth.TokenProvider]
 * which the production [DataStoreSessionStore] also implements.
 */
interface SessionStore {

    /** Cold flow emitting current session, or `null` when no session is stored. */
    val session: Flow<AuthSession?>

    /** Persist [session] atomically. Overwrites any existing entry. */
    suspend fun save(session: AuthSession)

    /** Clear persisted session (sign-out). Idempotent. */
    suspend fun clear()
}
