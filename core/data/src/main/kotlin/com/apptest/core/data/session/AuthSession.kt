package com.apptest.core.data.session

/**
 * In-memory representation of the persisted Supabase Auth session per `api_contracts.md` §1.
 *
 * Stored fields:
 * - [jwt] — bearer token for REST + Realtime channels (signed by Supabase)
 * - [refreshToken] — used to mint a new [jwt] when [isExpired]
 * - [expiresAtEpochMs] — server-provided expiry, used by [isExpired]
 *
 * No PII — `user_id` is derivable from the JWT claims; we don't double-store.
 */
data class AuthSession(
    val jwt: String,
    val refreshToken: String,
    val expiresAtEpochMs: Long,
) {
    fun isExpired(nowEpochMs: Long = System.currentTimeMillis()): Boolean =
        nowEpochMs >= expiresAtEpochMs
}
