package com.apptest.core.network.notifications

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for Supabase PostgREST `/rest/v1/notifications`.
 *
 * Actual DB columns (audited 2026-05-21): id, user_id, type, title, body, created_at.
 * V2 columns not yet in DB: deep_link, is_read.
 *
 * Authorization (`Bearer <jwt>`) is attached automatically by [AuthInterceptor].
 * RLS policies ensure each authenticated user sees only their own rows.
 */
interface SupabaseNotificationsApiService {

    @GET("notifications")
    suspend fun getNotifications(
        @Query("select") select: String = "id,user_id,type,title,body,created_at",
        @Query("order") order: String = "created_at.desc",
        @Query("limit") limit: Int = 50,
    ): List<NotificationDto>

    /** Lightweight ping used by [com.apptest.core.data.worker.SupabaseHeartbeatWorker]. */
    @GET("notifications")
    suspend fun ping(
        @Query("select") select: String = "id",
        @Query("limit") limit: Int = 1,
    ): List<NotificationDto>
}

// ─── DTOs ─────────────────────────────────────────────────────────────────────

/**
 * V1: is_read and deep_link not yet in DB — omitted from DTO.
 * All notifications are treated as unread.
 */
@Serializable
data class NotificationDto(
    val id: String,
    @SerialName("user_id") val userId: String = "",
    val type: String = "",
    val title: String = "",
    val body: String = "",
    @SerialName("created_at") val createdAt: String = "",
)
