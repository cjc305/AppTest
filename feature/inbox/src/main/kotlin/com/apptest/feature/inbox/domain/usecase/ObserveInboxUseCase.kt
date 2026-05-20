package com.apptest.feature.inbox.domain.usecase

import com.apptest.core.domain.inbox.InboxNotification
import com.apptest.core.domain.inbox.InboxRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveInboxUseCase @Inject constructor(private val repo: InboxRepository) {
    operator fun invoke(): Flow<List<InboxNotification>> = repo.observe()
}
