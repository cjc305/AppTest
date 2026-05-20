package com.apptest.core.ui.components

import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.apptest.core.designsystem.components.AppIcon
import com.apptest.core.designsystem.components.AppText

/**
 * Floating action button organism per `_specs/compose_components.md §4 AppFAB`.
 *
 * - Pass [text] + [expanded]=true for ExtendedFAB; null text or expanded=false → small FAB.
 * - Expand/collapse transition handled by M3 ExtendedFAB internally per
 *   motionSchemeExpressive (design_system §6).
 */
@Composable
fun AppFAB(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    expanded: Boolean = true,
) {
    if (text != null) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            expanded = expanded,
            icon = { AppIcon(icon, contentDescription = text) },
            text = { AppText(text, style = MaterialTheme.typography.labelLarge) },
        )
    } else {
        FloatingActionButton(onClick = onClick, modifier = modifier) {
            AppIcon(icon, contentDescription = null)
        }
    }
}
