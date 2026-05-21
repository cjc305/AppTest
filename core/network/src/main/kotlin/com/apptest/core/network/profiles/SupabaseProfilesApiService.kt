package com.apptest.core.network.profiles

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for Supabase PostgREST `/rest/v1/profiles`.
 *
 * Actual DB columns (audited 2026-05-21):
 *   id, display_name, avatar_url, reputation_score, tier,
 *   total_tests_completed, total_apps_submitted, is_developer, is_tester.
 *
 * Note: `proofs` table does not exist in V1 DB — getMyProofs returns empty list.
 */
interface SupabaseProfilesApiService {

    /**
     * Fetches the authenticated user's profile row.
     * RLS filters to `id = auth.uid()` server-side.
     */
    @GET("profiles")
    suspend fun getMyProfile(
        @Query("select") select: String = SELECT_PROFILE,
        @Query("limit") limit: Int = 1,
    ): List<ProfileDto>

    private companion object {
        const val SELECT_PROFILE =
            "id,display_name,avatar_url,reputation_score,tier,total_tests_completed"
    }
}

// ─── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
data class ProfileDto(
    val id: String,
    @SerialName("display_name") val displayName: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("reputation_score") val reputationScore: Int = 0,
    val tier: String = "NEWCOMER",
    @SerialName("total_tests_completed") val totalTestsCompleted: Int = 0,
)
