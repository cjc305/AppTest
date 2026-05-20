package com.apptest.feature.inbox.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun InboxRoute(
    onNavigateUp: () -> Unit,
    onItemClick: (deepLink: String) -> Unit,
    viewModel: InboxViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    InboxScreen(
        state = state,
        onNavigateUp = onNavigateUp,
        onItemClick = { item ->
            viewModel.onItemRead(item.id)
            item.deepLink?.let(onItemClick)
        },
        onMarkAllRead = viewModel::onMarkAllRead,
    )
}
