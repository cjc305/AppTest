package com.apptest.feature.testing.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.apptest.core.navigation.AppDestination
import com.apptest.feature.testing.ui.TestingRoute

fun NavGraphBuilder.testingGraph(
    onTestClick: (appId: String) -> Unit,
    onProofClick: (proofId: String) -> Unit,
) {
    composable<AppDestination.Testing> {
        TestingRoute(onTestClick = onTestClick, onProofClick = onProofClick)
    }
}
