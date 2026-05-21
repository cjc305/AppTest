package com.apptest.core.network.profiles

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for Supabase PostgREST `/rest/v1/profiles` and `/rest/v1/proofs`.
 *
 * RLS ensures each user can only read their own profile row and their own proofs.
 * Used by [com.apptest.feature.profile.data.SupabaseProfileRepository].
 */
interface SupabaseProfilesApiService {

    /**
     * Fetches the authenticated user's profile row.
     * RLS filters to `user_id = auth.uid()` server-side; no explicit user_id query needed.
     */
    @GET("profiles")
    suspend fun getMyProfile(
        @Query("select") select: String = SELECT_PROFILE,
        @Query("limit") limit: Int = 1,
    ): List<ProfileDto>

    /**
     * Fetches proofs with embedded match → app name for ProofCardSummary.
     * PostgREST chain: proofs → matches (via test_request_id) → apps (via app_id).
     */
    @GET("proofs")
    suspend fun getMyProofs(
        @Query("select") select: String = SELECT_PROOFS,
        @Query("order") order: String = "created_at.desc",
        @Query("limit") limit: Int = 20,
    ): List<ProofDto>

    private companion object {
        const val SELECT_PROFILE =
            "user_id,display_name,photo_url,reputation_tier,credits,reputation_score,streak_days"
        const val SELECT_PROOFS =
            "id,created_at,matches(apps(name))"
    }
}

// ─── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
data class ProfileDto(
    @SerialName("user_id") val userId: String,
    @SerialName("display_name") val displayName: String = "",
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("reputation_tier") val reputationTier: String = "Newcomer",
    val credits: Int = 0,
    @SerialName("reputation_score") val reputationScore: Int = 0,
    @SerialName("streak_days") val streakDays: Int = 0,
)

@Serializable
data class ProofDto(
    val id: String,
    @SerialName("created_at") val createdAt: String = "",
    val matches: ProofMatchDto? = null,
)

@Serializable
data class ProofMatchDto(
    val apps: ProofAppDto? = null,
)

@Serializable
data class ProofAppDto(
    val name: String = "",
)
