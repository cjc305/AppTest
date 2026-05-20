package com.apptest.feature.auth.ui.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptest.core.common.onFailure
import com.apptest.core.common.onSuccess
import com.apptest.core.domain.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepo: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<SignInUiState>(SignInUiState.Idle)
    val state: StateFlow<SignInUiState> = _state.asStateFlow()

    fun onGoogleClick() {
        _state.value = SignInUiState.Working
        viewModelScope.launch {
            authRepo.signInWithGoogle().onFailure { _state.value = SignInUiState.Error(it.message ?: "Sign-in failed") }
            // on success, MainActivity-level AuthState observer drives navigation;
            // our local state can stay Working (we won't be visible)
        }
    }

    fun onEmailModeClick() {
        _state.value = SignInUiState.EnteringEmail(email = "")
    }

    fun onEmailChange(email: String) {
        val current = _state.value
        if (current is SignInUiState.EnteringEmail) {
            _state.value = current.copy(email = email, error = null)
        }
    }

    fun onSubmitEmail() {
        val current = _state.value as? SignInUiState.EnteringEmail ?: return
        val email = current.email.trim()
        _state.value = SignInUiState.Working
        viewModelScope.launch {
            authRepo.requestMagicLink(email)
                .onSuccess { _state.value = SignInUiState.MagicLinkSent(email) }
                .onFailure { err ->
                    _state.value = SignInUiState.EnteringEmail(email, err.message ?: "Invalid email")
                }
        }
    }

    fun onBackToIdle() {
        _state.value = SignInUiState.Idle
    }
}
