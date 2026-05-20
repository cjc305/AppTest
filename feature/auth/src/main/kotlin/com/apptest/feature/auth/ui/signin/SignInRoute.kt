package com.apptest.feature.auth.ui.signin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SignInRoute(viewModel: SignInViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SignInScreen(
        state = state,
        onGoogleClick = viewModel::onGoogleClick,
        onEmailModeClick = viewModel::onEmailModeClick,
        onEmailChange = viewModel::onEmailChange,
        onSubmitEmail = viewModel::onSubmitEmail,
        onBack = viewModel::onBackToIdle,
    )
}
