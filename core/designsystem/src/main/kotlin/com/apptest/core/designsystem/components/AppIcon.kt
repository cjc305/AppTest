package com.apptest.core.designsystem.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Atomic icon wrapper. Default 24dp; size scale per `_specs/design_system.md` §10
 * (16 / 20 / 24 / 32 / 40 dp). Tint defaults to `LocalContentColor`.
 *
 * `contentDescription` is required (Accessibility hard rule per design_system §12).
 * Pass `null` only for decorative icons paired with adjacent text labels.
 */
@Composable
fun AppIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp,
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint,
    )
}
