package com.apptest.feature.myapps.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptest.feature.myapps.data.MyAppsRepository
import com.apptest.feature.myapps.domain.model.MyAppsLoadStatus
import com.apptest.feature.myapps.domain.usecase.GetMyAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Observes owned-apps Flow. Editor save mutates the same repo → list auto-refreshes
 * without manual reload (proving the Flow round-trip).
 *
 * Combines repo's load status so we distinguish "loaded empty list" (Empty state) from
 * "load failed before any data arrived" (Error state with retry).
 */
@HiltViewModel
class MyAppsViewModel @Inject constructor(
    getMyApps: GetMyAppsUseCase,
    repo: MyAppsRepository,
) : ViewModel() {

    val state: StateFlow<MyAppsUiState> = combine(
        getMyApps(),
        repo.loadStatus(),
    ) { items, status ->
        when (status) {
            is MyAppsLoadStatus.Failed -> if (items.isEmpty()) MyAppsUiState.Error(status.error)
                                          else MyAppsUiState.Loaded(items)
            MyAppsLoadStatus.Loading,
            MyAppsLoadStatus.Idle -> if (items.isNotEmpty()) MyAppsUiState.Loaded(items)
                                     else MyAppsUiState.Loading
            MyAppsLoadStatus.Loaded -> if (items.isEmpty()) MyAppsUiState.Empty
                                       else MyAppsUiState.Loaded(items)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MyAppsUiState.Loading,
    )
}
