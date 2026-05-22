package com.apptest.feature.myapps.ui.editor

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

    AppEditorScreen(
        state = state,
        onField = viewModel::onField,
        onSave = viewModel::save,
        onCancel = onCancel,
        onRetryLoad = viewModel::retryLoad,
    )
}
