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
import kotlinx.coroutines.flow.updateAndGet
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
        // MED-004: use updateAndGet for atomic check-then-set so double-tap or race between
        // two verify() calls cannot start two concurrent network requests.
        var captured: EmailVerifyUiState.AwaitingCode? = null
        val after = _state.updateAndGet { current ->
            if (current is EmailVerifyUiState.AwaitingCode && current.code.length == CODE_LENGTH) {
                captured = current
                EmailVerifyUiState.Verifying(current.email)
            } else current
        }
        val awaiting = captured ?: return  // not AwaitingCode or code too short — bail
        if (after !is EmailVerifyUiState.Verifying) return // lost the CAS race
        viewModelScope.launch {
            authRepo.verifyMagicLink(email = awaiting.email, token = awaiting.code)
                .onSuccess { _state.value = EmailVerifyUiState.Succeeded(awaiting.email) }
                .onFailure { _state.value = EmailVerifyUiState.Failed(awaiting.email, it) }
        }
    }

    companion object {
        const val CODE_LENGTH = 6
    }
}
