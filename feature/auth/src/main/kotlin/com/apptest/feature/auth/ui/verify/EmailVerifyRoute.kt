package com.apptest.feature.auth.ui.verify

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apptest.core.designsystem.components.AppText
import com.apptest.core.designsystem.components.AppVSpacer
import com.apptest.core.designsystem.spacing.AppSpacing
import com.apptest.core.designsystem.theme.AppL10n
import com.apptest.core.ui.components.AppButton
import com.apptest.core.ui.components.AppLoadingState
import com.apptest.core.ui.templates.ScreenScaffold

@Composable
fun EmailVerifyRoute(viewModel: EmailVerifyViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val l = AppL10n.current
    ScreenScaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(AppSpacing.Lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (val s = state) {
                is EmailVerifyUiState.Verifying -> AppLoadingState(message = l.verify_progress.format(s.email))
                is EmailVerifyUiState.Failed -> {
                    AppText(l.verify_failed_title, color = MaterialTheme.colorScheme.error)
                    AppVSpacer(AppSpacing.Sm)
                    AppText(
                        text = s.error.message ?: l.cta_retry,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                    AppVSpacer(AppSpacing.Md)
                    AppButton(l.cta_retry, onClick = viewModel::verify)
                }
                EmailVerifyUiState.Succeeded -> {
                    AppText(l.verify_signed_in_loading, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
