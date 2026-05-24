package com.apptest.core.network.apps

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
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
 * Actual DB columns (re-audited 2026-05-24):
 *   id (uuid), owner_id (uuid, auto-filled by trigger), name, description,
 *   play_url (NOT NULL — required), icon_url, category, min_android_version,
 *   status (DRAFT default), test_duration_minutes, required_tier,
 *   created_at, updated_at, package_name, play_opt_in_url,
 *   required_testers (default 12), required_days (default 14), deleted_at.
 *
 * Status lifecycle: DRAFT → ACTIVE → PAUSED → ARCHIVED  (via [AppStatus])
 * App becomes eligible for matching ONLY when status = ACTIVE.
 */
interface SupabaseAppsApiService {

    /**
     * All apps owned by current user (RLS scopes via owner_id = auth.uid()).
     * Hides soft-deleted rows by default via `deleted_at=is.null`.
     */
    @GET("apps")
    suspend fun listOwned(
        @Query("select") select: String = SELECT_LIST,
        @Query("order") order: String = "created_at.desc",
        @Query("deleted_at") deletedAtFilter: String = "is.null",
    ): List<AppDto>

    /**
     * Single app by id — includes embedded owner profile for AppDetail screen.
     * Filters `deleted_at=is.null` by default so owner-side flows don't see archived
     * rows. Use [getByIdIncludingArchived] for the tester-facing AppDetail screen
     * which needs to render an "App removed" banner even for archived apps.
     */
    @GET("apps")
    suspend fun getById(
        @Query("id") idFilter: String,
        @Query("select") select: String = SELECT_DETAIL,
        @Query("deleted_at") deletedAtFilter: String = "is.null",
    ): List<AppDto>

    /**
     * Single app by id INCLUDING archived rows — for AppDetail screen which renders
     * an "App removed by developer" banner instead of 404 when a tester clicks an
     * inbox notification for an app that's been archived since match assignment.
     */
    @GET("apps")
    suspend fun getByIdIncludingArchived(
        @Query("id") idFilter: String,
        @Query("select") select: String = SELECT_DETAIL,
    ): List<AppDto>

    /** Create app. Returns the created row (Prefer: return=representation). */
    @POST("apps")
    suspend fun create(
        @Body body: AppUpsertBody,
        @Header("Prefer") prefer: String = "return=representation",
    ): List<AppDto>

    /**
     * Update mutable fields (name, description, etc.) for an owned app.
     * Returns Response<Unit> so PostgREST's 204 No Content (from return=minimal) doesn't
     * trigger the "null body but non-null type" Retrofit error.
     */
    @PATCH("apps")
    suspend fun update(
        @Query("id") idFilter: String,
        @Body body: AppUpsertBody,
        @Header("Prefer") prefer: String = "return=minimal",
    ): Response<Unit>

    /** Change recruitment status (DRAFT ↔ ACTIVE ↔ PAUSED). */
    @PATCH("apps")
    suspend fun updateStatus(
        @Query("id") idFilter: String,
        @Body body: AppStatusBody,
        @Header("Prefer") prefer: String = "return=minimal",
    ): Response<Unit>

    /**
     * Activate a DRAFT app via the `activate_app` Postgres RPC. Server-side ownership
     * check via SECURITY DEFINER + `auth.uid() = owner_id`. Triggers immediate matching.
     */
    @POST("rpc/activate_app")
    suspend fun activateApp(@Body body: ActivateAppRequest): AppDto

    /**
     * Soft delete (recommended). Marks `deleted_at = now()` + `status = ARCHIVED`.
     * - Matches already in flight are NOT cancelled — they expire naturally via
     *   `matches.expires_at`. Testers' completion stats stay intact.
     * - App disappears from owner's My Apps list (filter `deleted_at=is.null`).
     * - Backend matching filters `status = ACTIVE` so ARCHIVED rows skip the pool.
     */
    @PATCH("apps")
    suspend fun softDelete(
        @Query("id") idFilter: String,
        @Body body: SoftDeleteBody,
        @Header("Prefer") prefer: String = "return=minimal",
    ): Response<Unit>

    /** Hard delete (RLS owner only). Use [softDelete] for typical cases. */
    @DELETE("apps")
    suspend fun hardDelete(
        @Query("id") idFilter: String,
        @Header("Prefer") prefer: String = "return=minimal",
    ): Response<Unit>

    private companion object {
        // SELECT_LIST is the source of truth for the My Apps list cache.
        // It MUST include every column that AppEditor reads back when the user taps
        // "Edit" — otherwise cached rows missing the field cause the editor to show
        // blank inputs (regression noted 2026-05-24).
        const val SELECT_LIST =
            "id,name,status,category,description,play_url," +
                "package_name,play_opt_in_url,required_testers,required_days,created_at"
        const val SELECT_DETAIL =
            "id,name,description,category,icon_url,owner_id,status,play_url," +
                "package_name,play_opt_in_url,required_testers,required_days," +
                "profiles!owner_id(display_name,tier)"
    }
}

// ─── Enums ────────────────────────────────────────────────────────────────────

/** Mirror of `public.apps.status` CHECK constraint enum. */
@Serializable
enum class AppStatus {
    DRAFT,
    ACTIVE,
    PAUSED,
    ARCHIVED;

    companion object {
        /** Lenient parse: unknown strings → DRAFT to avoid crash on schema drift. */
        fun fromOrDraft(raw: String?): AppStatus =
            values().firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: DRAFT
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
    val status: String = AppStatus.DRAFT.name,
    @SerialName("play_url") val playUrl: String = "",
    @SerialName("package_name") val packageName: String? = null,
    @SerialName("play_opt_in_url") val playOptInUrl: String? = null,
    @SerialName("required_testers") val requiredTesters: Int = 12,
    @SerialName("required_days") val requiredDays: Int = 14,
    @SerialName("created_at") val createdAt: String = "",
    val profiles: OwnerProfileDto? = null,
)

@Serializable
data class OwnerProfileDto(
    @SerialName("display_name") val displayName: String,
    val tier: String,
)

/**
 * Upsert body for `POST/PATCH /apps`.
 * Includes all client-editable columns. `owner_id` is auto-filled by `set_app_owner`
 * BEFORE INSERT trigger (no need to send from client). `status` defaults to DRAFT via
 * DB column default — client should call [SupabaseAppsApiService.activateApp] to flip
 * to ACTIVE and start matching.
 */
@Serializable
data class AppUpsertBody(
    val name: String,
    val description: String,
    @SerialName("play_url") val playUrl: String,
    val category: String = "UTILITY",
    @SerialName("icon_url") val iconUrl: String? = null,
    @SerialName("package_name") val packageName: String? = null,
    @SerialName("play_opt_in_url") val playOptInUrl: String? = null,
    @SerialName("required_testers") val requiredTesters: Int = 12,
    @SerialName("required_days") val requiredDays: Int = 14,
)

@Serializable
data class AppStatusBody(val status: String) {
    companion object {
        fun of(status: AppStatus) = AppStatusBody(status.name)
    }
}

@Serializable
data class ActivateAppRequest(@SerialName("p_app_id") val appId: String)

/**
 * Soft-delete payload. `deletedAt` is an ISO-8601 timestamp (e.g. "2026-05-24T01:23:45Z");
 * `status` flips to ARCHIVED so backend matching naturally excludes the row.
 */
@Serializable
data class SoftDeleteBody(
    @SerialName("deleted_at") val deletedAt: String,
    val status: String = AppStatus.ARCHIVED.name,
)
