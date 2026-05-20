package com.apptest.core.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.apptest.core.designsystem.components.AppText

enum class AppTopBarStyle { Small, CenterAligned, LargeCollapsible }

/**
 * Top app bar organism per `_specs/compose_components.md §4 AppTopBar`.
 * Wraps M3 TopAppBar variants; defaults to LargeCollapsible per design_system §3 (expressive
 * emphasis for screen titles). Pass [scrollBehavior] from a `Scaffold` to get collapse-on-scroll.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    style: AppTopBarStyle = AppTopBarStyle.LargeCollapsible,
) {
    val titleSlot: @Composable () -> Unit = {
        AppText(title, style = MaterialTheme.typography.titleLarge)
    }
    val navSlot: @Composable () -> Unit = { navIcon?.invoke() }

    when (style) {
        AppTopBarStyle.Small -> TopAppBar(
            title = titleSlot,
            modifier = modifier,
            navigationIcon = navSlot,
            actions = actions,
            scrollBehavior = scrollBehavior,
        )
        AppTopBarStyle.CenterAligned -> CenterAlignedTopAppBar(
            title = titleSlot,
            modifier = modifier,
            navigationIcon = navSlot,
            actions = actions,
            scrollBehavior = scrollBehavior,
        )
        AppTopBarStyle.LargeCollapsible -> LargeTopAppBar(
            title = titleSlot,
            modifier = modifier,
            navigationIcon = navSlot,
            actions = actions,
            scrollBehavior = scrollBehavior,
        )
    }
}
