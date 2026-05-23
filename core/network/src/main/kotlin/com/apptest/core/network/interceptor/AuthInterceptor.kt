package com.apptest.core.network.interceptor

import com.apptest.core.domain.auth.TokenProvider
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches `Authorization: Bearer <jwt>` per request when [TokenProvider] returns a non-null
 * valid token. Skips the header (sends unauthenticated request) when token is `null` —
 * caller-side handlers must accept that some endpoints (sign-in itself) need anonymous access.
 *
 * **HIGH-2 fix**: The Supabase JWT is a row-level-security credential — handing it to a
 * separate backend (e.g. our Ktor matching service) means a compromise of that backend yields
 * a credential that can read/write Postgres directly under the user's RLS context.
 * Therefore: attach the JWT **only** when the request host is in [allowedHosts]. Other clients
 * (Ktor, third-party APIs) get an unmodified request and must establish their own auth.
 *
 * Uses [TokenProvider.tokenBlocking] which reads the in-memory mirror from SessionStore
 * (re-checks expiry on every read — see HIGH-1).
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider,
    @Named("auth_interceptor_allowed_hosts") private val allowedHosts: Set<String>,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        // Host-scope the JWT. Any request to a host outside the allowlist proceeds
        // unauthenticated — preventing the Supabase JWT from leaking to other backends.
        if (original.url.host !in allowedHosts) {
            return chain.proceed(original)
        }
        val token = tokenProvider.tokenBlocking()
        val authed = if (token != null) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        return chain.proceed(authed)
    }
}
