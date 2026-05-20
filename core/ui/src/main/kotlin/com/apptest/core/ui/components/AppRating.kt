package com.apptest.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.apptest.core.designsystem.components.AppText

/**
 * Star rating molecule per `_specs/compose_components.md §3 AppRating`.
 * - readOnly when [onChange] is null (V1 internal use only — tester ratings ship V2).
 * - Uses character glyphs (★ / ☆) to avoid pulling in `material-icons-core` for one widget.
 *   Replace with vector icons if/when icon dep added repo-wide.
 */
@Composable
fun AppRating(
    value: Float,
    modifier: Modifier = Modifier,
    max: Int = 5,
    onChange: ((Float) -> Unit)? = null,
) {
    Row(
        modifier = modifier.semantics {
            contentDescription = "$value out of $max stars"
        },
    ) {
        for (i in 1..max) {
            val filled = value >= i
            val starMod = if (onChange != null) {
                Modifier.clickable { onChange(i.toFloat()) }
            } else Modifier
            AppText(
                text = if (filled) "★" else "☆",
                style = MaterialTheme.typography.titleLarge,
                color = if (filled) MaterialTheme.colorScheme.tertiary else LocalContentColor.current,
                modifier = starMod,
            )
        }
    }
}
