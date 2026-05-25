package com.apptest.core.network.testing

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit interface for active-test management via Supabase PostgREST.
 *
 * Re-audited 2026-05-24:
 *   matches.status enum is {PENDING, ACCEPTED, IN_PROGRESS, COMPLETED, SKIPPED, EXPIRED}
 *   (previous code filtered "active"/"matched" which never matched — fixed below).
 *
 * Used by [com.apptest.core.data.worker.SupabaseHeartbeatWorker]:
 *   - getActiveMatches() — fetch matches where the current user is the tester
 *   - heartbeatMatch()   — positive ping when dev's app is detected installed
 *   - abandonMatch()     — when dev's app is detected uninstalled
 */
interface SupabaseTestingApiService {

    /**
     * Returns matches where the caller is the tester and status is active.
     * RLS ensures only own matches.
     *
     * Active = {PENDING, ACCEPTED, IN_PROGRESS} per matches_status_check.
     */
    @GET("matches")
    suspend fun getActiveMatches(
        @Query("select") select: String = "id,app_id,status,apps(package_name)",
        @Query("status") status: String = "in.(PENDING,ACCEPTED,IN_PROGRESS)",
    ): List<ActiveMatchDto>

    /**
     * Positive heartbeat: tells server the dev's app is still installed.
     * Server-side `heartbeat_match` RPC sets matches.last_heartbeat_at = now()
     * and verifies tester_id = auth.uid().
     */
    @POST("rpc/heartbeat_match")
    suspend fun heartbeatMatch(@Body body: HeartbeatRequest): Response<Unit>

    /**
     * Mark a match as abandoned — SKIPPED in the schema enum.
     * [idFilter] format: `"eq.<uuid>"`. RLS owner-only.
     */
    @PATCH("matches")
    suspend fun abandonMatch(
        @Query("id") idFilter: String,
        @Body body: AbandonBody,
        @Header("Prefer") prefer: String = "return=minimal",
    ): Response<Unit>
}

// ─── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
data class ActiveMatchDto(
    val id: String,
    @SerialName("app_id") val appId: String,
    val status: String = "PENDING",
    val apps: AppPackageDto? = null,
)

@Serializable
data class AppPackageDto(
    @SerialName("package_name") val packageName: String? = null,
)

@Serializable
data class HeartbeatRequest(@SerialName("p_match_id") val matchId: String)

@Serializable
data class AbandonBody(
    val status: String = "SKIPPED",
)
