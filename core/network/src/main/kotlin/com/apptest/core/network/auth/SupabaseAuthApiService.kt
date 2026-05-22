package com.apptest.core.network.auth

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit interface for Supabase Auth REST API (`/auth/v1/`).
 *
 * Base URL provided by [@SupabaseAuth][com.apptest.core.network.di.SupabaseAuth] Retrofit in
 * [com.apptest.core.network.di.NetworkModule]. The `apikey` header is added by the shared
 * Supabase OkHttp client; `Authorization: Bearer <jwt>` by [AuthInterceptor] on auth'd calls.
 *
 * Docs: https://supabase.com/docs/reference/javascript/auth-signinwithotp
 */
interface SupabaseAuthApiService {

    /** Send 6-digit OTP + magic link to [OtpRequest.email]. Returns 200 with empty body. */
    @POST("otp")
    suspend fun sendOtp(@Body request: OtpRequest): ResponseBody

    /** Verify a 6-digit email OTP code. Returns JWT session on success. */
    @POST("verify")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): AuthTokenResponse

    /**
     * Exchange a Google ID token for a Supabase session.
     * Supabase verifies the token against its configured Google OAuth credentials.
     */
    @POST("token")
    suspend fun signInWithIdToken(
        @Query("grant_type") grantType: String = "id_token",
        @Body request: GoogleIdTokenRequest,
    ): AuthTokenResponse

    /**
     * Exchange refresh token for a new access token.
     * Caller must pass current access token as [bearer] (`"Bearer <jwt>"`).
     */
    @POST("token")
    suspend fun refreshToken(
        @Header("Authorization") bearer: String,
        @Query("grant_type") grantType: String = "refresh_token",
        @Body request: RefreshRequest,
    ): AuthTokenResponse

    /**
     * Revoke the current session server-side. Caller passes `"Bearer <jwt>"`.
     */
    @POST("logout")
    suspend fun signOut(@Header("Authorization") bearer: String): ResponseBody
}

// ─── Request DTOs ──────────────────────────────────────────────────────────

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class OtpRequest(
    val email: String,
    /** Creates account if not already registered (V1: always true). */
    @EncodeDefault @SerialName("create_user") val createUser: Boolean = true,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class VerifyOtpRequest(
    /** Must be `"email"` for 6-digit code flow. Use `"magiclink"` for URL-click flow. */
    @EncodeDefault val type: String = "email",
    val token: String,
    val email: String,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class GoogleIdTokenRequest(
    /** Force-encoded even though it has a default; AppJson uses encodeDefaults=false. */
    @EncodeDefault val provider: String = "google",
    @SerialName("id_token") val idToken: String,
)

@Serializable
data class RefreshRequest(
    @SerialName("refresh_token") val refreshToken: String,
)

// ─── Response DTOs ─────────────────────────────────────────────────────────

@Serializable
data class AuthTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("token_type") val tokenType: String = "bearer",
)
