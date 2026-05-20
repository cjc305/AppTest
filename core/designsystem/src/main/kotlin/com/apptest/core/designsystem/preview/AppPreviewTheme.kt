package com.apptest.core.designsystem.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.apptest.core.designsystem.theme.AppTheme

/**
 * Standard preview wrapper. Use for EVERY `@Preview` in `:core:*` and `:feature:*`.
 *
 * - Pins [AppTheme] (so previews look like production)
 * - Disables Dynamic Color (previews are reproducible — Dynamic Color = system-dependent)
 * - Adds 16dp padding (avoids edge-clipping in IDE preview pane)
 * - Background = `MaterialTheme.colorScheme.surface` so light/dark show contrast
 *
 * Pair with [PreviewLightDark] / [PreviewScreenSizes] annotations on the @Preview function.
 *
 * ```kotlin
 * @PreviewLightDark
 * @Composable
 * private fun MyAtomPreview() = AppPreviewTheme { MyAtom(...) }
 * ```
 */
@Composable
fun AppPreviewTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    AppTheme(darkTheme = darkTheme, dynamicColor = false) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
        ) { content() }
    }
}

/** Convenience meta-annotation: renders both light + dark in one preview row. */
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
annotation class AppPreviewLightDark
