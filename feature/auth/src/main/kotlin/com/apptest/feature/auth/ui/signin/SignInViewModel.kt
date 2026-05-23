package com.apptest.feature.auth.ui.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptest.core.common.GoogleWebClientId
import com.apptest.core.common.onFailure
import com.apptest.core.common.onSuccess
import com.apptest.core.domain.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** One-shot navigation events emitted by [SignInViewModel]. */
sealed interface SignInEvent {
    data class NavigateToVerify(val email: String) : SignInEvent
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    /** Exposed to SignInRoute so it can build the GetGoogleIdOption without depending on :app. */
    @GoogleWebClientId val googleWebClientId: String,
) : ViewModel() {

    private val _state = MutableStateFlow<SignInUiState>(SignInUiState.Idle)
    val state: StateFlow<SignInUiState> = _state.asStateFlow()

    /**
     * One-shot navigation events. `replay=0` so a re-collector (e.g. SignIn re-entering
     * composition after the user presses Back from EmailVerify) does NOT re-receive a
     * stale navigation event — fixing the "trapped on back" bug.
     */
    private val _events = MutableSharedFlow<SignInEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<SignInEvent> = _events.asSharedFlow()

    /** 30 s cooldown between submit attempts — Supabase free tier allows 2 emails/hour. */
    private var lastSubmitAtMs: Long = 0L

    /** Called by SignInRoute after Credential Manager returns a Google ID token. */
    fun onGoogleIdToken(idToken: String) {
        if (_state.value is SignInUiState.Working) return // in-flight guard
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
        if (email.isBlank()) return
        if (_state.value is SignInUiState.Working) return  // in-flight guard

        val now = System.currentTimeMillis()
        val sinceLast = now - lastSubmitAtMs
        if (sinceLast in 1..29_999L) {
            _state.value = SignInUiState.EnteringEmail(
                email = email,
                error = "請稍候 ${(30_000 - sinceLast) / 1000} 秒再重發 / Wait before retrying",
            )
            return
        }
        lastSubmitAtMs = now

        _state.value = SignInUiState.Working
        viewModelScope.launch {
            authRepo.requestMagicLink(email)
                .onSuccess {
                    // Reset state to EnteringEmail so back-navigation lands on a clean form,
                    // NOT on MagicLinkSent (which would re-fire navigation in old design).
                    _state.value = SignInUiState.EnteringEmail(email)
                    _events.emit(SignInEvent.NavigateToVerify(email))
                }
                .onFailure { err ->
                    _state.value = SignInUiState.EnteringEmail(email, err.message ?: "Send failed")
                }
        }
    }

    fun onBackToIdle() {
        _state.value = SignInUiState.Idle
    }
}
