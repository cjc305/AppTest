package com.apptest.core.network.testing

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Query

/**
 * Retrofit interface for active-test management via Supabase PostgREST.
 *
 * Used by [com.apptest.core.data.worker.SupabaseHeartbeatWorker] (R-040/R-041) to:
 * - Fetch active matches + their app package names for install verification
 * - Abandon a match when the app is detected as uninstalled
 */
interface SupabaseTestingApiService {

    /**
     * Returns matches in status `active` or `matched` joined with the app's package name.
     * Supabase PostgREST embedded resource syntax: `select=id,app_id,apps(package_name)`.
     * RLS ensures the caller only sees their own matches.
     */
    // V1: apps.package_name not in DB — returns id,app_id only for install checks
    @GET("matches")
    suspend fun getActiveMatches(
        @Query("select") select: String = "id,app_id",
        @Query("status") status: String = "in.(active,matched)",
    ): List<ActiveMatchDto>

    /**
     * Mark a match as abandoned.
     * [idFilter] format: `"eq.<uuid>"`. RLS ensures only own matches can be updated.
     */
    @PATCH("matches")
    suspend fun abandonMatch(
        @Query("id") idFilter: String,
        @Body body: AbandonBody,
        @Header("Prefer") prefer: String = "return=minimal",
    ): ResponseBody
}

// ─── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
data class ActiveMatchDto(
    val id: String,
    @SerialName("app_id") val appId: String,
    val apps: AppPackageDto? = null,
)

// V1: apps.package_name not in DB — packageName always null for now
@Serializable
data class AppPackageDto(
    @SerialName("package_name") val packageName: String? = null,
)

@Serializable
data class AbandonBody(
    val status: String = "abandoned",
)
