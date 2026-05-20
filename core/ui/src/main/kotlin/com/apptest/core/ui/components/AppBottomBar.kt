package com.apptest.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.apptest.core.designsystem.components.AppIcon
import com.apptest.core.designsystem.components.AppText

/**
 * Stable representation of a bottom-bar destination. Use for both the registry and
 * the currently-selected item. `id` is the stable key (e.g. matches a nav route).
 */
@Immutable
data class AppBottomDest(
    val id: String,
    val label: String,
    val icon: ImageVector,
)

/**
 * Bottom navigation organism per `_specs/compose_components.md §4 AppBottomBar`.
 *
 * TODO(APT-X-006): wrap in Glass Surface backdrop once `AppGlass` ships in :core:designsystem.
 * Currently uses M3 default opaque `surfaceContainer`.
 *
 * TODO(APT-X-007): auto-hide on keyboard open — wire to WindowInsets.ime.
 */
@Composable
fun AppBottomBar(
    destinations: List<AppBottomDest>,
    current: AppBottomDest,
    onSelect: (AppBottomDest) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        destinations.forEach { dest ->
            NavigationBarItem(
                selected = dest.id == current.id,
                onClick = { onSelect(dest) },
                icon = { AppIcon(dest.icon, contentDescription = dest.label) },
                label = {
                    AppText(dest.label, style = MaterialTheme.typography.labelSmall)
                },
                alwaysShowLabel = true,
            )
        }
    }
}
