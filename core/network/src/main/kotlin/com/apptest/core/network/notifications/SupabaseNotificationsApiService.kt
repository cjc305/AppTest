package com.apptest.core.network.notifications

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Query

/**
 * Retrofit interface for Supabase PostgREST `/rest/v1/notifications`.
 *
 * Authorization (`Bearer <jwt>`) is attached automatically by [AuthInterceptor].
 * RLS policies ensure each authenticated user sees only their own rows.
 *
 * Endpoints:
 * - [getNotifications] — paginated read (initial load, pull-to-refresh)
 * - [markRead] — PATCH single row `is_read = true`
 * - [markAllRead] — PATCH all unread rows (RLS-scoped to current user)
 * - [ping] — lightweight GET for [SupabaseHeartbeatWorker] keep-alive
 */
interface SupabaseNotificationsApiService {

    @GET("notifications")
    suspend fun getNotifications(
        @Query("order") order: String = "created_at.desc",
        @Query("limit") limit: Int = 50,
    ): List<NotificationDto>

    /** Mark single notification as read. [idFilter] format: `"eq.<uuid>"` */
    @PATCH("notifications")
    suspend fun markRead(
        @Query("id") idFilter: String,
        @Body body: MarkReadBody,
        @Header("Prefer") prefer: String = "return=minimal",
    ): ResponseBody

    /** Mark all unread notifications as read (RLS scopes to current user). */
    @PATCH("notifications")
    suspend fun markAllRead(
        @Query("is_read") unreadFilter: String = "eq.false",
        @Body body: MarkReadBody,
        @Header("Prefer") prefer: String = "return=minimal",
    ): ResponseBody

    /** Lightweight ping used by [com.apptest.core.data.worker.SupabaseHeartbeatWorker]. */
    @GET("notifications")
    suspend fun ping(
        @Query("limit") limit: Int = 1,
    ): List<NotificationDto>
}

// ─── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
data class NotificationDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val type: String,
    val title: String,
    val body: String,
    @SerialName("deep_link") val deepLink: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class MarkReadBody(
    @SerialName("is_read") val isRead: Boolean = true,
)
