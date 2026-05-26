@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.apptest.feature.auth.ui.signin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.apptest.core.designsystem.components.AppText
import com.apptest.core.designsystem.components.AppVSpacer
import com.apptest.core.designsystem.spacing.AppSpacing
import com.apptest.core.designsystem.theme.AppL10n
import com.apptest.core.ui.components.AppButton
import com.apptest.core.ui.components.AppButtonVariant
import com.apptest.core.ui.components.AppLoadingState
import com.apptest.core.ui.templates.ScreenScaffold

@Composable
fun SignInScreen(
    state: SignInUiState,
    onGoogleClick: () -> Unit,
    onEmailModeClick: () -> Unit,
    onEmailChange: (String) -> Unit,
    onSubmitEmail: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val l = AppL10n.current
    ScreenScaffold(modifier = modifier) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).padding(AppSpacing.Lg)) {
            when (state) {
                SignInUiState.Idle -> IdleBody(onGoogleClick, onEmailModeClick)
                is SignInUiState.EnteringEmail -> EmailFormBody(
                    email = state.email,
                    error = state.error,
                    onEmailChange = onEmailChange,
                    onSubmit = onSubmitEmail,
                    onBack = onBack,
                )
                SignInUiState.Working -> AppLoadingState(message = l.signin_signing_in)
                // MagicLinkSent state no longer shown — SignInRoute emits a NavigateToVerify
                // event the moment the OTP is sent and immediately navigates to EmailVerify.
                // The state is reset to EnteringEmail right after success, so this branch is
                // technically unreachable but kept as a safe fallback to avoid an exhaustive
                // when warning. Renders the same as Working (1-frame visible at most).
                is SignInUiState.MagicLinkSent -> AppLoadingState(message = l.signin_signing_in)
                is SignInUiState.Error -> ErrorBody(message = state.message, onBack = onBack)
            }
        }
    }
}

@Composable
private fun IdleBody(
    @Suppress("UNUSED_PARAMETER") onGoogleClick: () -> Unit,
    onEmailClick: () -> Unit,
) {
    val l = AppL10n.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AppText(l.appName, style = MaterialTheme.typography.displayMedium)
        AppVSpacer(AppSpacing.Lg)
        AppText(text = l.signin_social_proof, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        AppVSpacer(AppSpacing.Xxl)
        // Alpha workaround (2026-05-26): Google sign-in button hidden until the
        // Android OAuth client (debug + Play App Signing SHA-1) is registered in
        // Google Cloud Console — without it, Credential Manager returns "no
        // credentials available". Email magic link works today; restore this button
        // once the OAuth client lands (PRELAUNCH §2.3 line 67).
        // AppButton(l.signin_cta_google, onGoogleClick, variant = AppButtonVariant.Primary, modifier = Modifier.fillMaxWidth())
        // AppVSpacer(AppSpacing.Sm)
        AppButton(l.signin_cta_email, onEmailClick, variant = AppButtonVariant.Primary, modifier = Modifier.fillMaxWidth())
        AppVSpacer(AppSpacing.Xxl)
        AppText(
            text = l.signin_terms,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EmailFormBody(
    email: String,
    error: String?,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
) {
    val l = AppL10n.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AppText(l.signin_email_title, style = MaterialTheme.typography.headlineMedium)
        AppVSpacer(AppSpacing.Md)
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { AppText(l.signin_email_label) },
            singleLine = true,
            isError = error != null,
            supportingText = { error?.let { AppText(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth(),
        )
        AppVSpacer(AppSpacing.Md)
        AppButton(
            text = l.signin_cta_send_link,
            onClick = onSubmit,
            enabled = email.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        )
        AppVSpacer(AppSpacing.Sm)
        TextButton(onClick = onBack) { AppText(l.cta_back) }
    }
}

@Composable
private fun ErrorBody(message: String, onBack: () -> Unit) {
    val l = AppL10n.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AppText(l.signin_failed_title, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
        AppVSpacer(AppSpacing.Sm)
        AppText(message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        AppVSpacer(AppSpacing.Md)
        AppButton(l.cta_retry, onBack, variant = AppButtonVariant.Primary)
    }
}
