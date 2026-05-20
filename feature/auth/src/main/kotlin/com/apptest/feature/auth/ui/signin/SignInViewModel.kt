package com.apptest.feature.auth.ui.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptest.core.common.GoogleWebClientId
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
    /** Exposed to SignInRoute so it can build the GetGoogleIdOption without depending on :app. */
    @GoogleWebClientId val googleWebClientId: String,
) : ViewModel() {

    private val _state = MutableStateFlow<SignInUiState>(SignInUiState.Idle)
    val state: StateFlow<SignInUiState> = _state.asStateFlow()

    /** Called by SignInRoute after Credential Manager returns a Google ID token. */
    fun onGoogleIdToken(idToken: String) {
        _state.value = SignInUiState.Working
        viewModelScope.launch {
            authRepo.signInWithGoogle(idToken)
                .onFailure { _state.value = SignInUiState.Error(it.message ?: "Sign-in failed") }
            // on success, MainActivity-level AuthState observer drives navigation
        }
    }

    /** Called by SignInRoute when Credential Manager fails or user cancels. */
    fun onGoogleSignInFailed(message: String) {
        _state.value = SignInUiState.Error(message)
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
