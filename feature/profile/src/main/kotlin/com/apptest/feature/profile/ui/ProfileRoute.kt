package com.apptest.feature.profile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ProfileRoute(
    onSettingsClick: () -> Unit,
    onInboxClick: () -> Unit,
    onProofClick: (String) -> Unit,
    onInviteClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProfileScreen(
        state = state,
        onSettingsClick = onSettingsClick,
        onInboxClick = onInboxClick,
        onProofClick = onProofClick,
        onInviteClick = onInviteClick,
        onRetry = viewModel::load,
    )
}
