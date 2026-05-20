package com.apptest.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.apptest.core.designsystem.components.AppText

/**
 * List item molecule per `_specs/compose_components.md §3 AppListItem`.
 * Wraps M3 `ListItem` with type-safety + slot APIs and applies AppTypography slots.
 */
@Composable
fun AppListItem(
    headline: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = {
            AppText(headline, style = MaterialTheme.typography.titleMedium)
        },
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        supportingContent = supporting?.let {
            { AppText(it, style = MaterialTheme.typography.bodyMedium) }
        },
        leadingContent = leading,
        trailingContent = trailing,
    )
}
