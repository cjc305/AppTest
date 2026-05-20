package com.apptest.feature.inbox.data

import com.apptest.core.common.AppResult
import com.apptest.feature.inbox.domain.model.InboxNotification
import com.apptest.feature.inbox.domain.model.InboxNotificationType
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class FakeInboxRepository @Inject constructor() : InboxRepository {

    private val _items = MutableStateFlow(seed())

    override fun observe(): Flow<List<InboxNotification>> = _items.asStateFlow()

    override suspend fun markRead(id: String): AppResult<Unit> {
        _items.update { list -> list.map { if (it.id == id) it.copy(isRead = true) else it } }
        return AppResult.Success(Unit)
    }

    override suspend fun markAllRead(): AppResult<Unit> {
        _items.update { list -> list.map { it.copy(isRead = true) } }
        return AppResult.Success(Unit)
    }

    private companion object {
        fun seed(): List<InboxNotification> {
            val now = Instant.now()
            return listOf(
                InboxNotification(
                    id = "n1", type = InboxNotificationType.NewMatch,
                    title = "New match: NoteFlash",
                    body = "Productivity · 7 testers needed",
                    timestamp = now.minus(2, ChronoUnit.HOURS),
                    isRead = false,
                    deepLink = "apptest://app/demo-app-001",
                ),
                InboxNotification(
                    id = "n2", type = InboxNotificationType.HeartbeatReminder,
                    title = "Don't forget App B today",
                    body = "Open app to log your D12 ping",
                    timestamp = now.minus(8, ChronoUnit.HOURS),
                    isRead = false,
                    deepLink = null,
                ),
                InboxNotification(
                    id = "n3", type = InboxNotificationType.ReputationChange,
                    title = "🎉 +6 reputation",
                    body = "Completed test for App X",
                    timestamp = now.minus(1, ChronoUnit.DAYS),
                    isRead = true,
                    deepLink = null,
                ),
                InboxNotification(
                    id = "n4", type = InboxNotificationType.Completion,
                    title = "Test complete: App X",
                    body = "Proof card ready — view in Profile",
                    timestamp = now.minus(1, ChronoUnit.DAYS),
                    isRead = true,
                    deepLink = null,
                ),
            )
        }
    }
}
