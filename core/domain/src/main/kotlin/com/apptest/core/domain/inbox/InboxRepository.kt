package com.apptest.core.domain.inbox

import com.apptest.core.common.AppResult
import kotlinx.coroutines.flow.Flow

/**
 * Contract for the Inbox/Notifications data source.
 * Implementation lives in :core:data (SupabaseInboxRepository).
 */
interface InboxRepository {
    fun observe(): Flow<List<InboxNotification>>
    suspend fun markRead(id: String): AppResult<Unit>
    suspend fun markAllRead(): AppResult<Unit>
}
