package com.apptest.feature.myapps.ui.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MyAppsRoute(
    onCreate: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: MyAppsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MyAppsScreen(
        state = state,
        onCreate = onCreate,
        onEdit = onEdit,
    )
}
