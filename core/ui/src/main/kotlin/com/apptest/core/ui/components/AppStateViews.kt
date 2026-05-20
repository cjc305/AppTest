package com.apptest.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.apptest.core.common.AppError
import com.apptest.core.designsystem.components.AppIcon
import com.apptest.core.designsystem.components.AppText
import com.apptest.core.designsystem.theme.AppL10n

/**
 * Loading / Error / Empty state organisms per `_specs/compose_components.md §4`.
 *
 * Hard rule: EVERY list/feed/screen 三態必備（per compose_components.md §6 anti-pattern #5）.
 * Code review reject if any of these is missing where applicable.
 */

@Composable
fun AppLoadingState(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator()
            if (message != null) {
                AppText(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun AppErrorState(
    error: AppError,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    val l = AppL10n.current
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppText(
                text = errorTitle(error),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
            val detail = error.message
            if (!detail.isNullOrBlank()) {
                AppText(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
            if (onRetry != null) {
                AppButton(
                    text = l.cta_retry,
                    onClick = onRetry,
                    variant = AppButtonVariant.Secondary,
                )
            }
        }
    }
}

@Composable
fun AppEmptyState(
    illustration: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    ctaText: String? = null,
    onCta: (() -> Unit)? = null,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppIcon(
                imageVector = illustration,
                contentDescription = null,
                size = 64.dp,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppText(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            if (description != null) {
                AppText(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            if (ctaText != null && onCta != null) {
                AppButton(text = ctaText, onClick = onCta, variant = AppButtonVariant.Primary)
            }
        }
    }
}

@Composable
private fun errorTitle(error: AppError): String {
    val l = AppL10n.current
    return when (error) {
        is AppError.Network -> l.err_network
        is AppError.Http -> l.err_http.format(error.code)
        is AppError.Auth -> l.err_auth
        is AppError.Validation -> l.err_validation
        is AppError.NotFound -> l.err_not_found
        is AppError.Forbidden -> l.err_forbidden
        is AppError.Conflict -> l.err_conflict
        is AppError.RateLimited -> l.err_rate_limited
        is AppError.Unknown -> l.err_unknown
    }
}
