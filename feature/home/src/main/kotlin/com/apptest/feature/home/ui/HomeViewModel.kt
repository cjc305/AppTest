package com.apptest.feature.home.ui

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptest.core.common.AppResult
import com.apptest.core.common.onFailure
import com.apptest.core.common.onSuccess
import com.apptest.core.network.backend.BackendStatsApiService
import com.apptest.feature.home.domain.model.HomeData
import com.apptest.feature.home.domain.usecase.GetHomeDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
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
    private val dataStore: DataStore<Preferences>,
    private val statsApi: BackendStatsApiService,
) : ViewModel() {

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = HomeUiState.Loading
            val skipped = dataStore.data.map { it[KEY_SKIPPED_MATCH_IDS] ?: emptySet() }
                .firstOrNull().orEmpty()
            // Fetch home data + pool stats in parallel — stats failure is non-fatal (banner just hides).
            val poolStatsSnapshot: PoolStatsSnapshot? = runCatching {
                coroutineScope {
                    val deferred = async { statsApi.poolStats() }
                    deferred.await()
                }
            }.map { dto ->
                PoolStatsSnapshot(
                    activeApps = dto.activeApps,
                    testers = dto.testers,
                    immediateMatchMode = dto.immediateMatchMode,
                    hint = dto.hint,
                )
            }.getOrNull()

            getHomeData()
                .onSuccess { data ->
                    val filtered = if (data.newMatch?.id in skipped) data.copy(newMatch = null) else data
                    _state.value = toLoadedOrEmpty(filtered, poolStatsSnapshot)
                }
                .onFailure { _state.value = HomeUiState.Error(it) }
        }
    }

    /**
     * Persisted dismissal — skipped match id is written to DataStore so a pull-to-refresh or
     * cold restart doesn't resurrect the card. Server-side persistence
     * (`POST /me/matches/{id}/skip`) lands with backend integration; this DataStore key can
     * be dropped then.
     */
    fun skipCurrentMatch() {
        val current = _state.value as? HomeUiState.Loaded ?: return
        val matchId = current.data.newMatch?.id ?: return
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_SKIPPED_MATCH_IDS] = (prefs[KEY_SKIPPED_MATCH_IDS] ?: emptySet()) + matchId
            }
        }
        val withoutMatch = current.data.copy(newMatch = null)
        _state.value = toLoadedOrEmpty(withoutMatch, current.poolStats)
    }

    private fun toLoadedOrEmpty(data: HomeData, poolStats: PoolStatsSnapshot?): HomeUiState {
        val empty = data.newMatch == null && data.activeTests.isEmpty() && data.myApps.isEmpty()
        return if (empty) {
            HomeUiState.Empty(nextBatchEta = nextBatchEtaLabel(), poolStats = poolStats)
        } else {
            HomeUiState.Loaded(data, poolStats = poolStats)
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

    private companion object {
        val KEY_SKIPPED_MATCH_IDS = stringSetPreferencesKey("home_skipped_match_ids")
    }
}
