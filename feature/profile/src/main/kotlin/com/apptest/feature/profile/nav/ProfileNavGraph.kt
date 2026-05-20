package com.apptest.feature.profile.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.apptest.core.navigation.AppDestination
import com.apptest.feature.profile.ui.ProfileRoute

fun NavGraphBuilder.profileGraph(
    onSettingsClick: () -> Unit,
    onInboxClick: () -> Unit,
    onProofClick: (proofId: String) -> Unit,
    onInviteClick: () -> Unit,
) {
    composable<AppDestination.Profile> {
        ProfileRoute(
            onSettingsClick = onSettingsClick,
            onInboxClick = onInboxClick,
            onProofClick = onProofClick,
            onInviteClick = onInviteClick,
        )
    }
}
