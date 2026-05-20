package com.apptest.core.network.interceptor

import com.apptest.core.domain.auth.TokenProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches `Authorization: Bearer <jwt>` per request when [TokenProvider] returns a non-null
 * valid token. Skips the header (sends unauthenticated request) when token is `null` —
 * caller-side handlers must accept that some endpoints (sign-in itself) need anonymous access.
 *
 * Uses [runBlocking] because the OkHttp `Interceptor` contract is synchronous. DataStore reads
 * are fast (microseconds when cached) so this does not meaningfully impact request latency.
 * If profiling later shows a problem, swap [TokenProvider] for a cached pre-fetched variant.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenProvider.token() }
        val original = chain.request()
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
