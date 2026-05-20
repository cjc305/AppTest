package com.apptest.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape tokens per `_specs/design_system.md` §4. M3 Expressive 鼓勵更大圓角；hero surfaces 一律 `large` 起跳。
 *
 * Slot mapping (per M3):
 * - extraSmall  → chip, badge
 * - small       → button, small card
 * - medium      → card
 * - large       → bottom sheet, hero
 * - extraLarge  → modal, full-screen-ish surfaces
 */
internal val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(16.dp),
    large      = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(40.dp),
)
