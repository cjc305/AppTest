package com.apptest.feature.testing.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun TestingRoute(
    onTestClick: (appId: String) -> Unit,
    onProofClick: (proofId: String) -> Unit,
    viewModel: TestingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TestingScreen(
        state = state,
        onFilterChange = viewModel::setFilter,
        onTestClick = onTestClick,
        onHeartbeat = viewModel::heartbeat,
        onAbandon = viewModel::abandon,
        onProofClick = onProofClick,
    )
}
