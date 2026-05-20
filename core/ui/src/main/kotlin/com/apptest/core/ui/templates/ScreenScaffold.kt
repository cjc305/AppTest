package com.apptest.core.ui.templates

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Top-level screen scaffold per `_specs/compose_organisms_templates.md §2 ScreenScaffold`.
 *
 * - Edge-to-edge built-in (`contentWindowInsets = WindowInsets.systemBars`).
 * - Auto-handles IME padding via systemBars (M3's default behavior for Compose Scaffold).
 * - Container = `MaterialTheme.colorScheme.surface` (no per-screen override).
 *
 * Pass [snackbarHost] when the screen needs Snackbar; pair with `AppSnackbar` in the host slot.
 */
@Composable
fun ScreenScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    fab: @Composable () -> Unit = {},
    snackbarHost: SnackbarHostState? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = fab,
        snackbarHost = {
            if (snackbarHost != null) SnackbarHost(snackbarHost)
        },
        contentWindowInsets = WindowInsets.systemBars,
        containerColor = MaterialTheme.colorScheme.surface,
        content = content,
    )
}
