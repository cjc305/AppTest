package com.apptest.feature.auth.ui.verify

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.apptest.core.common.AppError
import com.apptest.core.common.onFailure
import com.apptest.core.common.onSuccess
import com.apptest.core.domain.auth.AuthRepository
import com.apptest.core.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EmailVerifyUiState {
    data class Verifying(val email: String) : EmailVerifyUiState
    data class Failed(val email: String, val error: AppError) : EmailVerifyUiState
    data object Succeeded : EmailVerifyUiState
}

@HiltViewModel
class EmailVerifyViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val args = savedStateHandle.toRoute<AppDestination.EmailVerify>()

    private val _state = MutableStateFlow<EmailVerifyUiState>(EmailVerifyUiState.Verifying(args.email))
    val state: StateFlow<EmailVerifyUiState> = _state.asStateFlow()

    init { verify() }

    fun verify() {
        _state.value = EmailVerifyUiState.Verifying(args.email)
        viewModelScope.launch {
            // V1: token is mocked from email itself (real flow takes token from deep-link)
            authRepo.verifyMagicLink(token = args.email)
                .onSuccess { _state.value = EmailVerifyUiState.Succeeded }
                .onFailure { _state.value = EmailVerifyUiState.Failed(args.email, it) }
        }
    }
}
