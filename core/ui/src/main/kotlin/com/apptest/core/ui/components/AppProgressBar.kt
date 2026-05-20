package com.apptest.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.apptest.core.designsystem.components.AppText

/**
 * Linear progress molecule per `_specs/compose_components.md §3 AppProgressBar`.
 * Always determinate; if you need indeterminate use M3 `LinearProgressIndicator()` directly.
 *
 * `progress` is clamped to [0f, 1f] so callers can pass derived values safely.
 * `color` defaults to [MaterialTheme.colorScheme.primary]; pass an explicit value for
 * semantic color coding (e.g. amber for mid-range, red for low scores).
 */
@Composable
fun AppProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
    height: Dp = 8.dp,
    color: Color = Color.Unspecified,
) {
    val resolvedColor = if (color == Color.Unspecified) MaterialTheme.colorScheme.primary else color
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
            color = resolvedColor,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        )
    }
}

@com.apptest.core.designsystem.preview.AppPreviewLightDark
@Composable
private fun AppProgressBarPreview() = com.apptest.core.designsystem.preview.AppPreviewTheme {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppProgressBar(progress = 0.75f, label = "Completion Rate")
        AppProgressBar(progress = 0.45f)
        AppProgressBar(progress = 0.2f, color = MaterialTheme.colorScheme.error)
    }
}
