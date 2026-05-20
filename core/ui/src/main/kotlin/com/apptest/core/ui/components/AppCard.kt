package com.apptest.core.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class AppCardVariant { Elevated, Outlined, Filled }

/**
 * Card molecule per `_specs/compose_components.md §3 AppCard`.
 * - Default variant = Elevated (surface elevation 2 per design_system.md §5).
 * - When [onClick] is non-null uses M3's onClick overload to get correct ripple + a11y.
 * - Scale-on-press animation deferred to future polish task (not blocking V1 demo).
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    variant: AppCardVariant = AppCardVariant.Elevated,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    when (variant) {
        AppCardVariant.Elevated -> if (onClick != null) {
            ElevatedCard(onClick = onClick, modifier = modifier, content = content)
        } else {
            ElevatedCard(modifier = modifier, content = content)
        }
        AppCardVariant.Outlined -> if (onClick != null) {
            OutlinedCard(onClick = onClick, modifier = modifier, content = content)
        } else {
            OutlinedCard(modifier = modifier, content = content)
        }
        AppCardVariant.Filled -> if (onClick != null) {
            Card(onClick = onClick, modifier = modifier, content = content)
        } else {
            Card(modifier = modifier, content = content)
        }
    }
}
