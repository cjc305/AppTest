package com.apptest.feature.inbox.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptest.core.domain.inbox.InboxRepository
import com.apptest.feature.inbox.domain.usecase.ObserveInboxUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class InboxViewModel @Inject constructor(
    observe: ObserveInboxUseCase,
    private val repo: InboxRepository,
) : ViewModel() {

    val state: StateFlow<InboxUiState> = observe()
        .map { items ->
            if (items.isEmpty()) InboxUiState.Empty
            else InboxUiState.Loaded(items, items.count { !it.isRead })
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            InboxUiState.Loading,
        )

    fun onItemRead(id: String) {
        viewModelScope.launch { repo.markRead(id) }
    }

    fun onMarkAllRead() {
        viewModelScope.launch { repo.markAllRead() }
    }
}
