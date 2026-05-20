package com.apptest.feature.myapps.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptest.feature.myapps.domain.usecase.GetMyAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Observes owned-apps Flow. Editor save mutates the same fake repo → list auto-refreshes
 * without manual reload (proving the Flow round-trip).
 */
@HiltViewModel
class MyAppsViewModel @Inject constructor(
    getMyApps: GetMyAppsUseCase,
) : ViewModel() {

    val state: StateFlow<MyAppsUiState> = getMyApps()
        .map { items ->
            if (items.isEmpty()) MyAppsUiState.Empty else MyAppsUiState.Loaded(items)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MyAppsUiState.Loading,
        )
}
