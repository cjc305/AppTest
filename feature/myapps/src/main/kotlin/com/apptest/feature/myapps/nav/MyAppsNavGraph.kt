package com.apptest.feature.myapps.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.apptest.core.navigation.AppDestination
import com.apptest.feature.myapps.ui.editor.AppEditorRoute
import com.apptest.feature.myapps.ui.list.MyAppsRoute

/**
 * MyApps subgraph contribution. Owns 2 destinations:
 *  - [AppDestination.MyApps]   — list
 *  - [AppDestination.AppEditor] — create (appId=null) / edit (appId set)
 *
 * Both call back to [onNavigateToEditor] / [onNavigateUp] so the host (`:app`) decides
 * how to navigate (single nav controller or nested).
 */
fun NavGraphBuilder.myAppsGraph(
    onNavigateToEditor: (appId: String?) -> Unit,
    onNavigateUp: () -> Unit,
) {
    composable<AppDestination.MyApps> {
        MyAppsRoute(
            onCreate = { onNavigateToEditor(null) },
            onEdit = { appId -> onNavigateToEditor(appId) },
        )
    }
    composable<AppDestination.AppEditor> {
        AppEditorRoute(
            onSaved = onNavigateUp,
            onCancel = onNavigateUp,
        )
    }
}
