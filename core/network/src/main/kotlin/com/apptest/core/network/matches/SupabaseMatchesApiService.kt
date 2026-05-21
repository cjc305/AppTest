package com.apptest.core.network.matches

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Query

/**
 * Retrofit interface for Supabase PostgREST `/rest/v1/matches`.
 *
 * Richer queries for feature repositories (Home, Testing screens). The heartbeat worker
 * uses [com.apptest.core.network.testing.SupabaseTestingApiService] for its own queries.
 *
 * RLS policies scope all queries to the authenticated tester's own rows.
 */
interface SupabaseMatchesApiService {

    /** Latest status=matched entry for the Home hero card. Returns ≤1 item. */
    @GET("matches")
    suspend fun getNewMatch(
        @Query("select") select: String = SELECT_NEW_MATCH,
        @Query("status") status: String = "eq.matched",
        @Query("order") order: String = "matched_at.desc",
        @Query("limit") limit: Int = 1,
    ): List<MatchWithAppDto>

    /** Active + installed entries — Home active list and Testing screen. */
    @GET("matches")
    suspend fun getActiveTests(
        @Query("select") select: String = SELECT_ACTIVE,
        @Query("status") status: String = "in.(active,installed)",
    ): List<ActiveMatchWithAppDto>

    /** Completed entries — Testing history tab. */
    @GET("matches")
    suspend fun getCompletedTests(
        @Query("select") select: String = SELECT_COMPLETED,
        @Query("status") status: String = "eq.completed",
        @Query("order") order: String = "matched_at.desc",
    ): List<CompletedMatchWithAppDto>

    /** Stamp last_heartbeat_at = now(). [idFilter] format: "eq.<uuid>". */
    @PATCH("matches")
    suspend fun submitHeartbeat(
        @Query("id") idFilter: String,
        @Body body: HeartbeatBody = HeartbeatBody(),
        @Header("Prefer") prefer: String = "return=minimal",
    ): ResponseBody

    /** Mark match abandoned. [idFilter] format: "eq.<uuid>". */
    @PATCH("matches")
    suspend fun abandon(
        @Query("id") idFilter: String,
        @Body body: AbandonMatchBody = AbandonMatchBody(),
        @Header("Prefer") prefer: String = "return=minimal",
    ): ResponseBody

    private companion object {
        const val SELECT_NEW_MATCH =
            "id,app_id,match_score,apps(name,category,description,required_testers,required_days)"
        const val SELECT_ACTIVE =
            "id,app_id,days_active,last_heartbeat_at,apps(name,required_days)"
        const val SELECT_COMPLETED =
            "id,app_id,days_active,apps(name)"
    }
}

// ─── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
data class MatchWithAppDto(
    val id: String,
    @SerialName("app_id") val appId: String,
    @SerialName("match_score") val matchScore: Int = 0,
    val apps: MatchedAppInfoDto? = null,
)

@Serializable
data class MatchedAppInfoDto(
    val name: String = "",
    val category: String = "",
    val description: String = "",
    @SerialName("required_testers") val requiredTesters: Int = 12,
    @SerialName("required_days") val requiredDays: Int = 14,
)

@Serializable
data class ActiveMatchWithAppDto(
    val id: String,
    @SerialName("app_id") val appId: String,
    @SerialName("days_active") val daysActive: Int = 0,
    @SerialName("last_heartbeat_at") val lastHeartbeatAt: String? = null,
    val apps: ActiveMatchAppInfoDto? = null,
)

@Serializable
data class ActiveMatchAppInfoDto(
    val name: String = "",
    @SerialName("required_days") val requiredDays: Int = 14,
)

@Serializable
data class CompletedMatchWithAppDto(
    val id: String,
    @SerialName("app_id") val appId: String,
    @SerialName("days_active") val daysActive: Int = 14,
    val apps: CompletedMatchAppInfoDto? = null,
)

@Serializable
data class CompletedMatchAppInfoDto(
    val name: String = "",
)

@Serializable
data class HeartbeatBody(
    @SerialName("last_heartbeat_at") val lastHeartbeatAt: String = "now()",
)

@Serializable
data class AbandonMatchBody(
    val status: String = "abandoned",
)
