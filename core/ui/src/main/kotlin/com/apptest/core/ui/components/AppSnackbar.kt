package com.apptest.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.apptest.core.designsystem.components.AppText
import com.apptest.core.designsystem.theme.AppExtended

enum class AppSnackbarSeverity { Success, Info, Warning, Error }

@Immutable
private data class SnackbarColorSet(
    val container: Color,
    val content: Color,
    val action: Color,
)

/**
 * Snackbar organism per `_specs/compose_components.md §4 AppSnackbar`.
 * Semantic variants: Success / Info (default) / Warning / Error pull from AppExtended.colors.
 *
 * Show via standard `SnackbarHostState.showSnackbar(...)` then render the host with
 * `SnackbarHost(state) { AppSnackbar(message = it.visuals.message, ...) }`.
 *
 * Toast equivalent = use Severity.Info + no action button + short duration.
 */
@Composable
fun AppSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    severity: AppSnackbarSeverity = AppSnackbarSeverity.Info,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = colorsFor(severity)
    Snackbar(
        modifier = modifier,
        containerColor = colors.container,
        contentColor = colors.content,
        action = if (actionLabel != null && onAction != null) {
            {
                TextButton(onClick = onAction) {
                    AppText(actionLabel, color = colors.action)
                }
            }
        } else null,
    ) {
        AppText(message)
    }
}

@Composable
private fun colorsFor(severity: AppSnackbarSeverity): SnackbarColorSet {
    val ext = AppExtended.colors
    val scheme = MaterialTheme.colorScheme
    return when (severity) {
        AppSnackbarSeverity.Success -> SnackbarColorSet(
            container = ext.successContainer,
            content = ext.onSuccessContainer,
            action = ext.success,
        )
        AppSnackbarSeverity.Warning -> SnackbarColorSet(
            container = ext.warningContainer,
            content = ext.onWarningContainer,
            action = ext.warning,
        )
        AppSnackbarSeverity.Error -> SnackbarColorSet(
            container = scheme.errorContainer,
            content = scheme.onErrorContainer,
            action = scheme.error,
        )
        AppSnackbarSeverity.Info -> SnackbarColorSet(
            container = scheme.inverseSurface,
            content = scheme.inverseOnSurface,
            action = scheme.inversePrimary,
        )
    }
}
