package com.apptest.core.ui.templates

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class AdaptiveTwoPanePane { List, Detail }

/**
 * Responsive list/detail layout per `_specs/compose_organisms_templates.md §2 AdaptiveTwoPane`.
 *
 * - **Compact** (< 600dp width): renders only the [currentPane] — navigation is caller's job
 *   (typically: tapping list item → nav.push(detail), back gesture → back to list).
 * - **Medium / Expanded** (≥ 600dp width): renders both side-by-side with 1 : 1.5 weight
 *   (APT-P-023 default; may adjust).
 *
 * Pass [windowSizeClass] from your `calculateWindowSizeClass(activity)` host.
 */
@Composable
fun AdaptiveTwoPane(
    list: @Composable () -> Unit,
    detail: @Composable () -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    currentPane: AdaptiveTwoPanePane = AdaptiveTwoPanePane.List,
) {
    val isExpanded = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    if (isExpanded) {
        Row(modifier = modifier) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) { list() }
            VerticalDivider()
            Box(modifier = Modifier.weight(1.5f).fillMaxHeight()) { detail() }
        }
    } else {
        when (currentPane) {
            AdaptiveTwoPanePane.List -> list()
            AdaptiveTwoPanePane.Detail -> detail()
        }
    }
}
