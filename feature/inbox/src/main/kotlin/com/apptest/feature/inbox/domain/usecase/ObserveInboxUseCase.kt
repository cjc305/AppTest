package com.apptest.feature.inbox.domain.usecase

import com.apptest.feature.inbox.data.InboxRepository
import com.apptest.feature.inbox.domain.model.InboxNotification
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveInboxUseCase @Inject constructor(private val repo: InboxRepository) {
    operator fun invoke(): Flow<List<InboxNotification>> = repo.observe()
}
