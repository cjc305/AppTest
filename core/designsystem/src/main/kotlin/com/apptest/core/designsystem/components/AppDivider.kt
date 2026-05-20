package com.apptest.core.designsystem.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Atomic divider. Default color = `MaterialTheme.colorScheme.outlineVariant` (M3 recommended)
 * which is subtler than `outline` and works in both light/dark.
 *
 * Use [AppDivider] for typical row dividers (horizontal). Use [AppVerticalDivider] for
 * column separators (e.g., bottom-bar item dividers in `Compact` density).
 */
@Composable
fun AppDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
) {
    HorizontalDivider(modifier = modifier, thickness = thickness, color = color)
}

@Composable
fun AppVerticalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
) {
    VerticalDivider(modifier = modifier, thickness = thickness, color = color)
}
