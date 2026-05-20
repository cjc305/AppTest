package com.apptest.feature.auth.ui.signin

import androidx.compose.runtime.Immutable

@Immutable
sealed interface SignInUiState {
    data object Idle : SignInUiState
    data class EnteringEmail(val email: String, val error: String? = null) : SignInUiState
    data object Working : SignInUiState
    data class MagicLinkSent(val email: String) : SignInUiState
    data class Error(val message: String) : SignInUiState
}
