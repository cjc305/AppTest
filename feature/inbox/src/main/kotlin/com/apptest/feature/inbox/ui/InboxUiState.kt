package com.apptest.feature.inbox.ui

import androidx.compose.runtime.Immutable
import com.apptest.core.common.AppError
import com.apptest.feature.inbox.domain.model.InboxNotification

@Immutable
sealed interface InboxUiState {
    data object Loading : InboxUiState
    data class Error(val error: AppError) : InboxUiState
    data object Empty : InboxUiState
    data class Loaded(val items: List<InboxNotification>, val unreadCount: Int) : InboxUiState
}
