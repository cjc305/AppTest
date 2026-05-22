package com.apptest.core.network.interceptor

import com.apptest.core.domain.auth.TokenProvider
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches `Authorization: Bearer <jwt>` per request when [TokenProvider] returns a non-null
 * valid token. Skips the header (sends unauthenticated request) when token is `null` —
 * caller-side handlers must accept that some endpoints (sign-in itself) need anonymous access.
 *
 * Uses [TokenProvider.tokenBlocking] which reads an in-memory cache populated by the
 * SessionStore Flow collector. Avoids the previous `runBlocking { dataStore.firstOrNull() }`
 * that serialized concurrent OkHttp requests behind DataStore's internal Mutex.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider.tokenBlocking()
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
