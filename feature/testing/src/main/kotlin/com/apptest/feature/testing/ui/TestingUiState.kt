package com.apptest.feature.testing.ui

import androidx.compose.runtime.Immutable
import com.apptest.core.common.AppError
import com.apptest.feature.testing.domain.model.TestFilter
import com.apptest.feature.testing.domain.model.TestingSnapshot

@Immutable
sealed interface TestingUiState {
    data object Loading : TestingUiState
    data class Error(val error: AppError) : TestingUiState
    data class Empty(val nextBatchEta: String) : TestingUiState
    data class Loaded(
        val snapshot: TestingSnapshot,
        val filter: TestFilter,
    ) : TestingUiState
}
