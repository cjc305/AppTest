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
 * Actual DB columns (audited 2026-05-21):
 *   id, app_id, tester_id, status, assigned_at, completed_at.
 *
 * V2 columns not yet in DB: match_score, days_active, last_heartbeat_at, matched_at.
 */
interface SupabaseMatchesApiService {

    /** Latest status=matched entry for the Home hero card. Returns ≤1 item. */
    @GET("matches")
    suspend fun getNewMatch(
        @Query("select") select: String = SELECT_NEW_MATCH,
        @Query("status") status: String = "eq.matched",
        @Query("order") order: String = "assigned_at.desc",
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
        @Query("order") order: String = "completed_at.desc",
    ): List<CompletedMatchWithAppDto>

    /**
     * Heartbeat keep-alive: touch the match status to signal tester is still active.
     * V1: no last_heartbeat_at column — sets status=active as keep-alive.
     */
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
            "id,app_id,apps(name,category,description)"
        const val SELECT_ACTIVE =
            "id,app_id,status,apps(name)"
        const val SELECT_COMPLETED =
            "id,app_id,completed_at,apps(name)"
    }
}

// ─── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
data class MatchWithAppDto(
    val id: String,
    @SerialName("app_id") val appId: String,
    val apps: MatchedAppInfoDto? = null,
)

@Serializable
data class MatchedAppInfoDto(
    val name: String = "",
    val category: String = "",
    val description: String = "",
)

@Serializable
data class ActiveMatchWithAppDto(
    val id: String,
    @SerialName("app_id") val appId: String,
    val status: String = "active",
    val apps: ActiveMatchAppInfoDto? = null,
)

@Serializable
data class ActiveMatchAppInfoDto(
    val name: String = "",
)

@Serializable
data class CompletedMatchWithAppDto(
    val id: String,
    @SerialName("app_id") val appId: String,
    @SerialName("completed_at") val completedAt: String? = null,
    val apps: CompletedMatchAppInfoDto? = null,
)

@Serializable
data class CompletedMatchAppInfoDto(
    val name: String = "",
)

/** V1: no last_heartbeat_at column — touch status to signal activity. */
@Serializable
data class HeartbeatBody(
    val status: String = "active",
)

@Serializable
data class AbandonMatchBody(
    val status: String = "abandoned",
)
