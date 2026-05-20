package com.apptest.feature.profile.ui

import androidx.compose.runtime.Immutable
import com.apptest.core.common.AppError
import com.apptest.feature.profile.domain.model.ProfileData

@Immutable
sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Error(val error: AppError) : ProfileUiState
    data class Loaded(val data: ProfileData) : ProfileUiState
}
