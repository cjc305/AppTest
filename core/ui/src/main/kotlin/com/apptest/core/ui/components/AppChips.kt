package com.apptest.core.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.apptest.core.designsystem.components.AppIcon
import com.apptest.core.designsystem.components.AppText

/**
 * Chip molecules per `_specs/compose_components.md §3 AppChip / AppFilterChip`.
 * - [AppChip]: non-selectable, click-only (M3 AssistChip). Use for tags / categories that
 *   just take action.
 * - [AppFilterChip]: selectable with selected / unselected state (M3 FilterChip). Use for
 *   filter rows where multi-state matters.
 */

@Composable
fun AppChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    AssistChip(
        onClick = onClick,
        label = { AppText(text, style = MaterialTheme.typography.labelLarge) },
        modifier = modifier,
        leadingIcon = leadingIcon?.let {
            { AppIcon(it, contentDescription = null, size = 16.dp) }
        },
    )
}

@Composable
fun AppFilterChip(
    text: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    FilterChip(
        selected = selected,
        onClick = { onSelectedChange(!selected) },
        label = { AppText(text, style = MaterialTheme.typography.labelLarge) },
        modifier = modifier,
        leadingIcon = leadingIcon?.let {
            { AppIcon(it, contentDescription = null, size = 16.dp) }
        },
    )
}
