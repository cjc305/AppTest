package com.apptest.core.designsystem.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * Typed spacer wrappers. Prefer over raw `Spacer(Modifier.height(N.dp))` so the call site
 * reads as intent (`AppVSpacer(AppSpacing.Md)`) rather than magic numbers.
 *
 * Pair with `AppSpacing.*` tokens from `:core:designsystem/spacing/AppSpacing.kt`.
 */
@Composable
fun AppVSpacer(height: Dp) {
    Spacer(modifier = Modifier.height(height))
}

@Composable
fun AppHSpacer(width: Dp) {
    Spacer(modifier = Modifier.width(width))
}

@Composable
fun AppSquareSpacer(size: Dp) {
    Spacer(modifier = Modifier.size(size))
}
