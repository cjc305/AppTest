package com.apptest.core.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.apptest.core.designsystem.components.AppIcon
import com.apptest.core.designsystem.components.AppText

enum class AppButtonVariant { Primary, Secondary, Tonal, Text, Destructive }

/**
 * Variant-aware button molecule per `_specs/compose_components.md §3 AppButton`.
 * Wraps M3 Button family + standardizes loading / leading-icon UX.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Primary,
    leadingIcon: ImageVector? = null,
    loading: Boolean = false,
    enabled: Boolean = true,
) {
    val isEnabled = enabled && !loading
    when (variant) {
        AppButtonVariant.Primary -> Button(onClick, modifier, enabled = isEnabled) {
            ButtonBody(text, leadingIcon, loading)
        }
        AppButtonVariant.Secondary -> OutlinedButton(onClick, modifier, enabled = isEnabled) {
            ButtonBody(text, leadingIcon, loading)
        }
        AppButtonVariant.Tonal -> FilledTonalButton(onClick, modifier, enabled = isEnabled) {
            ButtonBody(text, leadingIcon, loading)
        }
        AppButtonVariant.Text -> TextButton(onClick, modifier, enabled = isEnabled) {
            ButtonBody(text, leadingIcon, loading)
        }
        AppButtonVariant.Destructive -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = isEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
        ) { ButtonBody(text, leadingIcon, loading) }
    }
}

@Composable
private fun ButtonBody(text: String, leadingIcon: ImageVector?, loading: Boolean) {
    if (loading) {
        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        Spacer(modifier = Modifier.width(8.dp))
    } else if (leadingIcon != null) {
        AppIcon(leadingIcon, contentDescription = null, size = 18.dp)
        Spacer(modifier = Modifier.width(8.dp))
    }
    AppText(text, style = MaterialTheme.typography.labelLarge)
}
