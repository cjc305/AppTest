package com.apptest.core.ui.components

import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.apptest.core.designsystem.components.AppText

/**
 * Badge molecule per `_specs/compose_components.md §3 AppBadge`.
 * Pass [text] for arbitrary label OR [count] for numeric (auto "99+" when > 99).
 * Pass neither for a plain dot.
 *
 * For positioning over an icon use M3 `BadgedBox` directly (uncommon enough not to wrap).
 */
@Composable
fun AppBadge(
    modifier: Modifier = Modifier,
    text: String? = null,
    count: Int? = null,
) {
    val display: String? = text ?: count?.let { if (it > 99) "99+" else it.toString() }
    Badge(modifier = modifier) {
        if (display != null) {
            AppText(display, style = MaterialTheme.typography.labelSmall)
        }
    }
}
