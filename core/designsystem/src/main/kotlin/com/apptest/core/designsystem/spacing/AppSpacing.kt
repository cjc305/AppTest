package com.apptest.core.designsystem.spacing

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing tokens on 8dp grid per `_specs/design_system.md` §11.
 *
 * Convention:
 * - Screen padding default = [Md] (16dp).
 * - On `WindowSizeClass.Medium+`, switch to [Lg] (24dp) + max-width 720dp 置中.
 *
 * Usage:
 * ```kotlin
 * Modifier.padding(AppSpacing.Md)
 * Spacer(modifier = Modifier.height(AppSpacing.Lg))
 * ```
 *
 * Hard rule: 任何 `Dp` 字面值不在 8 倍數 = PR reject (per compose_components.md §6).
 */
object AppSpacing {
    val Xxs: Dp = 2.dp
    val Xs: Dp  = 4.dp
    val Sm: Dp  = 8.dp
    val Md: Dp  = 16.dp
    val Lg: Dp  = 24.dp
    val Xl: Dp  = 32.dp
    val Xxl: Dp = 48.dp
}
