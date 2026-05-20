package com.apptest.feature.myapps.ui.list

import androidx.compose.runtime.Immutable
import com.apptest.core.common.AppError
import com.apptest.feature.myapps.domain.model.OwnedAppRow

@Immutable
sealed interface MyAppsUiState {
    data object Loading : MyAppsUiState
    data class Error(val error: AppError) : MyAppsUiState
    data object Empty : MyAppsUiState
    data class Loaded(val items: List<OwnedAppRow>) : MyAppsUiState
}
