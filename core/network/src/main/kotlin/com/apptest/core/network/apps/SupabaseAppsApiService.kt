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
 * Actual DB columns (audited 2026-05-21):
 *   id, owner_id, name, status, category, description, icon_url,
 *   created_at, updated_at, min_android_version.
 *
 * V2 columns not yet in DB: package_name, play_opt_in_url, required_testers,
 *   required_days, deleted_at.
 */
interface SupabaseAppsApiService {

    /** All apps owned by current user (RLS scopes via owner_id = auth.uid()). */
    @GET("apps")
    suspend fun listOwned(
        @Query("select") select: String = SELECT_LIST,
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

    /** Hard delete (RLS owner only). V1: no deleted_at column in schema. */
    @DELETE("apps")
    suspend fun softDelete(
        @Query("id") idFilter: String,
        @Header("Prefer") prefer: String = "return=minimal",
    ): ResponseBody

    private companion object {
        const val SELECT_LIST =
            "id,name,status,category,created_at"
        const val SELECT_DETAIL =
            "id,name,description,category,icon_url,owner_id," +
                "profiles!owner_id(display_name,tier)"
    }
}

// ─── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
data class AppDto(
    val id: String,
    val name: String,
    val description: String = "",
    val category: String = "",
    @SerialName("icon_url") val iconUrl: String? = null,
    @SerialName("owner_id") val ownerId: String = "",
    val status: String = "recruiting",
    @SerialName("created_at") val createdAt: String = "",
    val profiles: OwnerProfileDto? = null,
)

@Serializable
data class OwnerProfileDto(
    @SerialName("display_name") val displayName: String,
    val tier: String,
)

/** Only includes columns that actually exist in the DB. */
@Serializable
data class AppUpsertBody(
    val name: String,
    val description: String,
    val category: String = "Other",
    @SerialName("icon_url") val iconUrl: String = "",
)

@Serializable
data class AppStatusBody(val status: String)
