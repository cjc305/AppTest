package com.apptest.core.data.inbox

import com.apptest.core.common.AppError
import com.apptest.core.common.AppResult
import com.apptest.core.data.di.ApplicationScope
import com.apptest.core.data.realtime.RealtimeManager
import com.apptest.core.domain.inbox.InboxNotification
import com.apptest.core.domain.inbox.InboxNotificationType
import com.apptest.core.domain.inbox.InboxRepository
import com.apptest.core.network.notifications.NotificationDto
import com.apptest.core.network.notifications.SupabaseNotificationsApiService
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Production [InboxRepository] backed by Supabase PostgREST + Realtime (R-044).
 *
 * V1: is_read and deep_link not in DB — markRead/markAllRead update local state only.
 */
@Singleton
class SupabaseInboxRepository @Inject constructor(
    private val apiService: SupabaseNotificationsApiService,
    private val realtimeManager: RealtimeManager,
    @ApplicationScope private val scope: CoroutineScope,
) : InboxRepository {

    private val _notifications = MutableStateFlow<List<InboxNotification>>(emptyList())
    private var initialized = false

    init {
        scope.launch {
            realtimeManager.events.collect { event ->
                if (event.table != TABLE_NOTIFICATIONS) return@collect
                when (event.eventType) {
                    "INSERT" -> {
                        val n = event.fields.toDomain() ?: return@collect
                        _notifications.update { current ->
                            listOf(n) + current.filter { it.id != n.id }
                        }
                    }
                    "UPDATE" -> {
                        val n = event.fields.toDomain() ?: return@collect
                        _notifications.update { current ->
                            current.map { if (it.id == n.id) n else it }
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    override fun observe(): Flow<List<InboxNotification>> {
        if (!initialized) {
            initialized = true
            scope.launch { loadInitial() }
        }
        return _notifications.asStateFlow()
    }

    /** V1: no is_read column in DB — updates local state only. */
    override suspend fun markRead(id: String): AppResult<Unit> = runCatching {
        _notifications.update { list -> list.map { if (it.id == id) it.copy(isRead = true) else it } }
        AppResult.Success(Unit)
    }.getOrElse { AppResult.Failure(AppError.fromThrowable(it)) }

    /** V1: no is_read column in DB — updates local state only. */
    override suspend fun markAllRead(): AppResult<Unit> = runCatching {
        _notifications.update { list -> list.map { it.copy(isRead = true) } }
        AppResult.Success(Unit)
    }.getOrElse { AppResult.Failure(AppError.fromThrowable(it)) }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private suspend fun loadInitial() {
        runCatching {
            val dtos = apiService.getNotifications()
            _notifications.value = dtos.mapNotNull { it.toDomain() }
        }
    }

    private companion object {
        const val TABLE_NOTIFICATIONS = "notifications"
    }
}

// ─── mapping ──────────────────────────────────────────────────────────────────

private fun NotificationDto.toDomain(): InboxNotification? = runCatching {
    InboxNotification(
        id = id,
        type = type.toNotificationType(),
        title = title,
        body = body,
        timestamp = Instant.parse(createdAt),
        isRead = false, // V1: is_read not in DB
        deepLink = null, // V1: deep_link not in DB
    )
}.getOrNull()

private fun Map<String, String>.toDomain(): InboxNotification? = runCatching {
    InboxNotification(
        id = get("id") ?: return null,
        type = get("type").orEmpty().toNotificationType(),
        title = get("title").orEmpty(),
        body = get("body").orEmpty(),
        timestamp = Instant.parse(get("created_at") ?: return null),
        isRead = false, // V1: is_read not in DB
        deepLink = null, // V1: deep_link not in DB
    )
}.getOrNull()

private fun String.toNotificationType(): InboxNotificationType =
    InboxNotificationType.entries.firstOrNull { it.name == this } ?: InboxNotificationType.System
