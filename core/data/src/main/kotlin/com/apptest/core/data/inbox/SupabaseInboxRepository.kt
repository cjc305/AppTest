package com.apptest.core.data.inbox

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Production [InboxRepository] backed by Supabase PostgREST + Realtime (R-044).
 *
 * V1: `is_read` and `deep_link` aren't in the DB schema yet. Read-state is persisted **locally**
 * via DataStore (set of read notification IDs) so the unread badge survives process death. When
 * the schema gains `is_read`, swap this for a PATCH call and drop the DataStore key.
 */
@Singleton
class SupabaseInboxRepository @Inject constructor(
    private val apiService: SupabaseNotificationsApiService,
    private val realtimeManager: RealtimeManager,
    private val dataStore: DataStore<Preferences>,
    @ApplicationScope private val scope: CoroutineScope,
) : InboxRepository {

    private val _notifications = MutableStateFlow<List<InboxNotification>>(emptyList())
    private val readIds = MutableStateFlow<Set<String>>(emptySet())

    // HIGH-7 fix: replace AtomicBoolean (which ran loadInitial only once per process) with a
    // time-throttled re-load so coming back to Inbox after WhileSubscribed(5000) expiry triggers
    // a refresh. Realtime push alone is unreliable in the background (Doze, WS drop).
    @Volatile private var lastLoadAt: Long = 0L

    init {
        // Hydrate read-id cache from DataStore once, then keep it in sync with subsequent edits.
        scope.launch {
            dataStore.data.map { it[KEY_READ_IDS] ?: emptySet() }.collect { readIds.value = it }
        }
        scope.launch {
            realtimeManager.events.collect { event ->
                if (event.table != TABLE_NOTIFICATIONS) return@collect
                when (event.eventType) {
                    "INSERT" -> {
                        val n = event.fields.toDomain(readIds.value) ?: return@collect
                        _notifications.update { current ->
                            listOf(n) + current.filter { it.id != n.id }
                        }
                    }
                    "UPDATE" -> {
                        val incoming = event.fields.toDomain(readIds.value) ?: return@collect
                        // HIGH-6 fix: realtime UPDATE may carry stale isRead because DataStore →
                        // readIds flush is async. OR the local read state so a freshly-tapped row
                        // doesn't flicker back to unread.
                        _notifications.update { current ->
                            current.map { existing ->
                                if (existing.id == incoming.id) {
                                    incoming.copy(isRead = existing.isRead || incoming.isRead)
                                } else existing
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    override fun observe(): Flow<List<InboxNotification>> {
        val now = System.currentTimeMillis()
        if (now - lastLoadAt > MIN_RELOAD_MS) {
            lastLoadAt = now
            scope.launch { loadInitial() }
        }
        return _notifications.asStateFlow()
    }

    override suspend fun markRead(id: String): AppResult<Unit> = runCatching {
        dataStore.edit { prefs ->
            prefs[KEY_READ_IDS] = (prefs[KEY_READ_IDS] ?: emptySet()) + id
        }
        _notifications.update { list -> list.map { if (it.id == id) it.copy(isRead = true) else it } }
        AppResult.Success(Unit)
    }.getOrElse { AppResult.Failure(AppError.fromThrowable(it)) }

    override suspend fun markAllRead(): AppResult<Unit> = runCatching {
        val allIds = _notifications.value.map { it.id }.toSet()
        dataStore.edit { prefs ->
            prefs[KEY_READ_IDS] = (prefs[KEY_READ_IDS] ?: emptySet()) + allIds
        }
        _notifications.update { list -> list.map { it.copy(isRead = true) } }
        AppResult.Success(Unit)
    }.getOrElse { AppResult.Failure(AppError.fromThrowable(it)) }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private suspend fun loadInitial() {
        runCatching {
            // Make sure the read-id cache is hydrated before we map DTOs → domain.
            val persistedReadIds = dataStore.data.map { it[KEY_READ_IDS] ?: emptySet() }.firstOrNull().orEmpty()
            readIds.value = persistedReadIds
            val dtos = apiService.getNotifications()
            _notifications.value = dtos.mapNotNull { it.toDomain(persistedReadIds) }
        }
    }

    private companion object {
        const val TABLE_NOTIFICATIONS = "notifications"
        const val MIN_RELOAD_MS = 30_000L   // throttle: don't refetch more than every 30s
        val KEY_READ_IDS = stringSetPreferencesKey("inbox_read_notification_ids")
    }
}

// ─── mapping ──────────────────────────────────────────────────────────────────

private fun NotificationDto.toDomain(readIds: Set<String>): InboxNotification? = runCatching {
    InboxNotification(
        id = id,
        type = type.toNotificationType(),
        title = title,
        body = body,
        timestamp = Instant.parse(createdAt),
        isRead = id in readIds,
        deepLink = null, // V1: deep_link not in DB
    )
}.getOrNull()

private fun Map<String, String>.toDomain(readIds: Set<String>): InboxNotification? = runCatching {
    val id = get("id") ?: return null
    InboxNotification(
        id = id,
        type = get("type").orEmpty().toNotificationType(),
        title = get("title").orEmpty(),
        body = get("body").orEmpty(),
        timestamp = Instant.parse(get("created_at") ?: return null),
        isRead = id in readIds,
        deepLink = null, // V1: deep_link not in DB
    )
}.getOrNull()

private fun String.toNotificationType(): InboxNotificationType =
    InboxNotificationType.entries.firstOrNull { it.name == this } ?: InboxNotificationType.System
