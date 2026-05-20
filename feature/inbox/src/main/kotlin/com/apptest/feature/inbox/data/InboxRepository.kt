package com.apptest.feature.inbox.data

import com.apptest.core.common.AppResult
import com.apptest.core.domain.Repository
import com.apptest.feature.inbox.domain.model.InboxNotification
import kotlinx.coroutines.flow.Flow

interface InboxRepository : Repository {
    fun observe(): Flow<List<InboxNotification>>
    suspend fun markRead(id: String): AppResult<Unit>
    suspend fun markAllRead(): AppResult<Unit>
}
