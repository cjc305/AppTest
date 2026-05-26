package com.apptest.feature.auth.ui.signin

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun SignInRoute(
    onNavigateToVerify: (email: String) -> Unit,
    viewModel: SignInViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    // One-shot event collection. repeatOnLifecycle ensures we only collect when STARTED;
    // emissions while STOPPED are buffered (capacity=1) or dropped (DROP_OLDEST).
    // Critically: SharedFlow with replay=0 means re-entering this composable (e.g. user
    // navigates back from EmailVerify) does NOT re-fire stale NavigateToVerify events.
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    is SignInEvent.NavigateToVerify -> onNavigateToVerify(event.email)
                }
            }
        }
    }

    SignInScreen(
        state = state,
        onGoogleClick = {
            scope.launch {
                triggerGoogleSignIn(
                    context = context,
                    serverClientId = viewModel.googleWebClientId,
                    onToken = viewModel::onGoogleIdToken,
                    onError = viewModel::onGoogleSignInFailed,
                )
            }
        },
        onEmailModeClick = viewModel::onEmailModeClick,
        onEmailChange = viewModel::onEmailChange,
        onSubmitEmail = viewModel::onSubmitEmail,
        onBack = viewModel::onBackToIdle,
    )
}

private suspend fun triggerGoogleSignIn(
    context: Context,
    serverClientId: String,
    onToken: (String) -> Unit,
    onError: (String) -> Unit,
) {
    if (serverClientId.isEmpty()) {
        onError("Google Sign-In not configured (missing GOOGLE_WEB_CLIENT_ID)")
        return
    }
    val credentialManager = CredentialManager.create(context)
    // 2026-05-26: switched from GetGoogleIdOption to GetSignInWithGoogleOption.
    // GetGoogleIdOption is for seamless One Tap and was returning "no credentials
    // available" on devices where the user hadn't recently signed in with Google.
    // GetSignInWithGoogleOption is the button-click counterpart — always shows a
    // chooser, no filter-by-authorized-accounts gate, so it matches a "Sign in
    // with Google" CTA's UX expectation.
    val option = GetSignInWithGoogleOption.Builder(serverClientId).build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(option)
        .build()
    runCatching {
        val result = credentialManager.getCredential(context = context, request = request)
        GoogleIdTokenCredential.createFrom(result.credential.data).idToken
    }.fold(
        onSuccess = { idToken -> onToken(idToken) },
        onFailure = { e ->
            if (e is GetCredentialCancellationException) return // user cancelled — no error shown
            onError(e.message ?: "Google Sign-In failed")
        },
    )
}
