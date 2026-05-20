package com.apptest.feature.inbox.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.apptest.core.navigation.AppDestination
import com.apptest.feature.inbox.ui.InboxRoute

fun NavGraphBuilder.inboxGraph(
    onNavigateUp: () -> Unit,
    onItemDeepLink: (String) -> Unit,
) {
    composable<AppDestination.Inbox> {
        InboxRoute(
            onNavigateUp = onNavigateUp,
            onItemClick = onItemDeepLink,
        )
    }
}
