package com.apptest.feature.appdetail.ui

import androidx.compose.runtime.Immutable
import com.apptest.core.common.AppError
import com.apptest.feature.appdetail.domain.model.AppDetailData

@Immutable
sealed interface AppDetailUiState {
    data object Loading : AppDetailUiState
    data class Error(val error: AppError) : AppDetailUiState
    data class Loaded(
        val data: AppDetailData,
        val joinInProgress: Boolean = false,
        /** Inline error for the Join action (URL invalid / Play Store not installed). */
        val joinError: String? = null,
    ) : AppDetailUiState
}
