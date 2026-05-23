package com.apptest.feature.myapps.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AppEditorRoute(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AppEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedId) {
        if (state.savedId != null) onSaved()
    }

    // HIGH-011 (audit 2026-05-23): swallow system back-press while a save is in flight.
    // Prior C-2 fix added the re-entry guard inside save() itself but didn't block the user
    // from leaving mid-save — popBackStack() cancelled viewModelScope, dropping the
    // suspending HTTP call and the local cache update (data loss). The Cancel button is
    // already disabled when isSaving via AppEditorScreen.
    BackHandler(enabled = state.isSaving) { /* swallow */ }

    AppEditorScreen(
        state = state,
        onField = viewModel::onField,
        onSave = viewModel::save,
        onCancel = { if (!state.isSaving) onCancel() },
        onRetryLoad = viewModel::retryLoad,
    )
}
