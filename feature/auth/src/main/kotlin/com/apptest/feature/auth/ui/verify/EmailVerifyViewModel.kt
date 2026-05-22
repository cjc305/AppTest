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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface EmailVerifyUiState {
    val email: String
    data class AwaitingCode(
        override val email: String,
        val code: String = "",
        val error: AppError? = null,
    ) : EmailVerifyUiState
    data class Verifying(override val email: String) : EmailVerifyUiState
    data class Failed(override val email: String, val error: AppError) : EmailVerifyUiState
    data class Succeeded(override val email: String) : EmailVerifyUiState
}

@HiltViewModel
class EmailVerifyViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val args = savedStateHandle.toRoute<AppDestination.EmailVerify>()

    private val _state = MutableStateFlow<EmailVerifyUiState>(
        EmailVerifyUiState.AwaitingCode(args.email)
    )
    val state: StateFlow<EmailVerifyUiState> = _state.asStateFlow()

    fun onCodeChanged(code: String) {
        val sanitized = code.filter(Char::isDigit).take(CODE_LENGTH)
        val current = _state.value
        if (current is EmailVerifyUiState.AwaitingCode) {
            _state.value = current.copy(code = sanitized, error = null)
        } else if (current is EmailVerifyUiState.Failed) {
            _state.value = EmailVerifyUiState.AwaitingCode(current.email, sanitized)
        }
    }

    /** Resets to AwaitingCode after a failure so user can retry. */
    fun retry() {
        val current = _state.value
        _state.value = EmailVerifyUiState.AwaitingCode(current.email)
    }

    fun verify() {
        val current = _state.value
        val code = (current as? EmailVerifyUiState.AwaitingCode)?.code.orEmpty()
        if (code.length != CODE_LENGTH) return
        if (current !is EmailVerifyUiState.AwaitingCode) return
        _state.value = EmailVerifyUiState.Verifying(current.email)
        viewModelScope.launch {
            authRepo.verifyMagicLink(token = code)
                .onSuccess { _state.value = EmailVerifyUiState.Succeeded(current.email) }
                .onFailure { _state.value = EmailVerifyUiState.Failed(current.email, it) }
        }
    }

    companion object {
        const val CODE_LENGTH = 6
    }
}
