package com.apptest.feature.home.ui

import androidx.compose.runtime.Immutable
import com.apptest.core.common.AppError
import com.apptest.feature.home.domain.model.HomeData

/**
 * Immutable Home UI state. Sealed so Screen `when` is exhaustive (compiler-enforced three-state
 * coverage per `compose_components.md §6 anti-pattern #5`).
 */
@Immutable
sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Error(val error: AppError) : HomeUiState

    data class Empty(val nextBatchEta: String) : HomeUiState

    data class Loaded(val data: HomeData) : HomeUiState
}
