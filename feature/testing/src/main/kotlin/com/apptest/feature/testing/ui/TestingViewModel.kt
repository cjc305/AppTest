package com.apptest.feature.testing.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptest.feature.testing.data.TestingRepository
import com.apptest.feature.testing.domain.model.TestFilter
import com.apptest.feature.testing.domain.model.TestingSnapshot
import com.apptest.feature.testing.domain.usecase.ObserveTestingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TestingViewModel @Inject constructor(
    observe: ObserveTestingUseCase,
    private val repo: TestingRepository,
) : ViewModel() {

    private val filter = MutableStateFlow(TestFilter.Active)

    val state: StateFlow<TestingUiState> =
        combine(observe(), filter) { snap, f -> mapState(snap, f) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TestingUiState.Loading)

    fun setFilter(f: TestFilter) {
        filter.value = f
    }

    fun heartbeat(testId: String) {
        viewModelScope.launch { repo.submitHeartbeat(testId) }
    }

    fun abandon(testId: String) {
        viewModelScope.launch { repo.abandon(testId) }
    }

    private fun mapState(snap: TestingSnapshot, f: TestFilter): TestingUiState {
        val empty = snap.active.isEmpty() && snap.completed.isEmpty()
        return if (empty) TestingUiState.Empty(nextBatchEta = "4h 23m")
        else TestingUiState.Loaded(snap, f)
    }
}
