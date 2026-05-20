package com.apptest.core.domain.auth

/**
 * Read-only token accessor. Consumed by `:core:network` (OkHttp interceptor) so it can attach
 * `Authorization: Bearer <jwt>` without depending on `:core:data` (avoids data ↔ network cycle).
 *
 * Production binding: `DataStoreSessionStore` in `:core:data` implements this alongside
 * [SessionStore] and is bound in `SessionModule`.
 *
 * Returning `null` means caller should treat the request as unauthenticated. Callers MUST NOT
 * cache the token — always re-fetch per request so rotations propagate.
 */
interface TokenProvider {
    /** Current valid JWT, or `null` when no session / expired. */
    suspend fun token(): String?
}
