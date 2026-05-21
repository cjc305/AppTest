package com.apptest.core.network.apps

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit interface for Supabase PostgREST `/rest/v1/apps`.
 *
 * RLS policies:
 * - Authenticated users can SELECT apps WHERE status=recruiting
 * - Owner can SELECT/UPDATE/DELETE their own apps
 *
 * Used by: SupabaseHomeRepository, SupabaseAppDetailRepository, SupabaseMyAppsRepository.
 */
interface SupabaseAppsApiService {

    /** All apps owned by current user (RLS scopes via owner_id = auth.uid()). */
    @GET("apps")
    suspend fun listOwned(
        @Query("select") select: String = SELECT_LIST,
        @Query("deleted_at") deletedFilter: String = "is.null",
        @Query("order") order: String = "created_at.desc",
    ): List<AppDto>

    /** Single app by id — includes embedded owner profile for AppDetail screen. */
    @GET("apps")
    suspend fun getById(
        @Query("id") idFilter: String,
        @Query("select") select: String = SELECT_DETAIL,
    ): List<AppDto>

    /** Create app. Returns the created row (Prefer: return=representation). */
    @POST("apps")
    suspend fun create(
        @Body body: AppUpsertBody,
        @Header("Prefer") prefer: String = "return=representation",
    ): List<AppDto>

    /** Update mutable fields (name, description, etc.) for an owned app. */
    @PATCH("apps")
    suspend fun update(
        @Query("id") idFilter: String,
        @Body body: AppUpsertBody,
        @Header("Prefer") prefer: String = "return=minimal",
    ): ResponseBody

    /** Change recruitment status (recruiting ↔ paused). */
    @PATCH("apps")
    suspend fun updateStatus(
        @Query("id") idFilter: String,
        @Body body: AppStatusBody,
        @Header("Prefer") prefer: String = "return=minimal",
    ): ResponseBody

    /** Soft-delete: stamp deleted_at (RLS owner only). */
    @PATCH("apps")
    suspend fun softDelete(
        @Query("id") idFilter: String,
        @Body body: AppDeleteBody,
        @Header("Prefer") prefer: String = "return=minimal",
    ): ResponseBody

    private companion object {
        const val SELECT_LIST =
            "id,name,package_name,status,required_testers,required_days,created_at"
        const val SELECT_DETAIL =
            "id,name,package_name,description,category,icon_url,play_opt_in_url," +
                "required_testers,required_days,owner_id," +
                "profiles!owner_id(display_name,reputation_tier)"
    }
}

// ─── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
data class AppDto(
    val id: String,
    val name: String,
    @SerialName("package_name") val packageName: String = "",
    val description: String = "",
    val category: String = "",
    @SerialName("icon_url") val iconUrl: String? = null,
    @SerialName("play_opt_in_url") val playOptInUrl: String = "",
    @SerialName("required_testers") val requiredTesters: Int = 12,
    @SerialName("required_days") val requiredDays: Int = 14,
    @SerialName("owner_id") val ownerId: String = "",
    val status: String = "recruiting",
    @SerialName("created_at") val createdAt: String = "",
    val profiles: OwnerProfileDto? = null,
)

@Serializable
data class OwnerProfileDto(
    @SerialName("display_name") val displayName: String,
    @SerialName("reputation_tier") val reputationTier: String,
)

@Serializable
data class AppUpsertBody(
    val name: String,
    @SerialName("package_name") val packageName: String,
    val description: String,
    @SerialName("play_opt_in_url") val playOptInUrl: String,
    val category: String = "Other",
    @SerialName("required_testers") val requiredTesters: Int = 12,
    @SerialName("required_days") val requiredDays: Int = 14,
    @SerialName("icon_url") val iconUrl: String = "",
)

@Serializable
data class AppStatusBody(val status: String)

@Serializable
data class AppDeleteBody(
    @SerialName("deleted_at") val deletedAt: String = "now()",
)
