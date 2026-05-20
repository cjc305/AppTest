package com.apptest.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptest.core.common.AppResult
import com.apptest.core.common.onFailure
import com.apptest.core.common.onSuccess
import com.apptest.feature.home.domain.model.HomeData
import com.apptest.feature.home.domain.usecase.GetHomeDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Home ViewModel — Hilt-injected per `compose_components.md §6 anti-pattern #1`.
 * No Android types imported (modularization hard rule).
 *
 * Reload via [load] (pull-to-refresh / onRetry).
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeData: GetHomeDataUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = HomeUiState.Loading
            getHomeData()
                .onSuccess { data -> _state.value = toLoadedOrEmpty(data) }
                .onFailure { _state.value = HomeUiState.Error(it) }
        }
    }

    /**
     * In-memory dismissal of the current new-match hero card. Server-side persistence
     * (`POST /me/matches/{id}/skip` per `api_contracts.md` §5) lands with backend integration.
     */
    fun skipCurrentMatch() {
        val current = _state.value as? HomeUiState.Loaded ?: return
        val withoutMatch = current.data.copy(newMatch = null)
        _state.value = toLoadedOrEmpty(withoutMatch)
    }

    private fun toLoadedOrEmpty(data: HomeData): HomeUiState {
        val empty = data.newMatch == null && data.activeTests.isEmpty() && data.myApps.isEmpty()
        return if (empty) {
            HomeUiState.Empty(nextBatchEta = nextBatchEtaLabel())
        } else {
            HomeUiState.Loaded(data)
        }
    }

    /**
     * Hours/minutes until the next 02:00 UTC matching-cron run (per `backend_architecture.md` §4).
     * Pure on `System.currentTimeMillis()` — replaced with [java.time.Clock] injection in tests later.
     */
    private fun nextBatchEtaLabel(): String {
        val now = java.time.Instant.now().atZone(java.time.ZoneOffset.UTC)
        val today02 = now.toLocalDate().atTime(2, 0).atZone(java.time.ZoneOffset.UTC)
        val target = if (now.toLocalTime().hour < 2) today02 else today02.plusDays(1)
        val mins = java.time.Duration.between(now, target).toMinutes().coerceAtLeast(0)
        val h = mins / 60
        val m = mins % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    // Keep AppResult import alive for future when we add specific error handling paths
    @Suppress("unused") private val _appResultRef: AppResult<Unit>? = null
}
