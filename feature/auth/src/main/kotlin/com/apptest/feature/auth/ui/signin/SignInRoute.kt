package com.apptest.feature.auth.ui.signin

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun SignInRoute(viewModel: SignInViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
    val option = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(serverClientId)
        .setAutoSelectEnabled(false)
        .build()
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
