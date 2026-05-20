package com.apptest.feature.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Stateful entry point. Owns [HomeViewModel] injection + state collection.
 * Pure delegation to the stateless [HomeScreen] keeps the screen previewable.
 *
 * Called from `:app/nav/AppNavHost` via `homeGraph(navController, onAppClick)` extension.
 */
@Composable
fun HomeRoute(
    onAppClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onRetry = viewModel::load,
        onAppClick = onAppClick,
        onJoinMatch = onAppClick,           // V1: Join CTA == open detail (real Join lands with appdetail)
        onSkipMatch = { _ -> viewModel.skipCurrentMatch() },
    )
}
