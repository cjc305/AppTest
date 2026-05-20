package com.apptest.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.apptest.core.designsystem.components.AppText

/**
 * Linear progress molecule per `_specs/compose_components.md §3 AppProgressBar`.
 * Always determinate; if you need indeterminate use M3 `LinearProgressIndicator()` directly.
 *
 * `progress` is clamped to [0f, 1f] so callers can pass derived values safely.
 */
@Composable
fun AppProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
    height: Dp = 8.dp,
) {
    Column(modifier = modifier) {
        if (label != null) {
            AppText(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        )
    }
}
