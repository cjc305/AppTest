@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.apptest.feature.appdetail.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apptest.core.designsystem.components.AppIcon
import com.apptest.core.designsystem.components.AppText
import com.apptest.core.designsystem.components.AppVSpacer
import com.apptest.core.designsystem.spacing.AppSpacing
import com.apptest.core.designsystem.theme.AppL10n
import com.apptest.core.ui.components.AppButton
import com.apptest.core.ui.components.AppButtonVariant
import com.apptest.core.ui.components.AppErrorState
import com.apptest.core.ui.components.AppLoadingState
import com.apptest.core.ui.components.AppTopBar
import com.apptest.core.ui.templates.ScreenScaffold
import com.apptest.feature.appdetail.domain.model.AppDetailData
import com.apptest.feature.appdetail.ui.components.AppDetailHeader
import com.apptest.feature.appdetail.ui.components.ExplainabilityCard
import com.apptest.feature.appdetail.ui.components.RequirementsSection
import com.apptest.feature.appdetail.ui.components.ScreenshotCarousel

@Composable
fun AppDetailScreen(
    state: AppDetailUiState,
    onNavigateUp: () -> Unit,
    onJoin: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val l = AppL10n.current
    ScreenScaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = (state as? AppDetailUiState.Loaded)?.data?.name ?: l.appdetail_default_title,
                navIcon = {
                    IconButton(onClick = onNavigateUp) {
                        AppIcon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = l.cta_back,
                        )
                    }
                },
            )
        },
    ) { padding ->
        when (state) {
            AppDetailUiState.Loading -> AppLoadingState(modifier = Modifier.padding(padding))
            is AppDetailUiState.Error -> AppErrorState(
                error = state.error,
                onRetry = onRetry,
                modifier = Modifier.padding(padding),
            )
            is AppDetailUiState.Loaded -> LoadedBody(
                data = state.data,
                joinInProgress = state.joinInProgress,
                joinError = state.joinError,
                onJoin = onJoin,
                contentPadding = padding,
            )
        }
    }
}

@Composable
private fun LoadedBody(
    data: AppDetailData,
    joinInProgress: Boolean,
    joinError: String?,
    onJoin: () -> Unit,
    contentPadding: PaddingValues,
) {
    val isArchived = data.status.equals("ARCHIVED", ignoreCase = true)
    Column(modifier = Modifier.padding(contentPadding).fillMaxWidth()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.Md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.Md),
        ) {
            if (isArchived) ArchivedBanner()
            AppDetailHeader(data = data)
            ScreenshotCarousel(
                urls = data.screenshotUrls,
                fallbackCount = data.screenshotCount,
                modifier = Modifier.fillMaxWidth(),
            )
            AppText(text = data.description, style = MaterialTheme.typography.bodyLarge)
            RequirementsSection(requirements = data.requirements)
            ExplainabilityCard(reasons = data.matchReasons)
            AppVSpacer(AppSpacing.Lg)
        }
        JoinFooter(
            joinInProgress = joinInProgress,
            joinError = joinError,
            onJoin = onJoin,
            isArchived = isArchived,
        )
    }
}

@Composable
private fun ArchivedBanner() {
    Surface(
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.Md)) {
            AppText(
                text = "此 App 已下架 / App removed",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            AppText(
                text = "開發者已將此 App 從測試池移除。已完成的測試紀錄保留。\n" +
                    "The developer has removed this app. Completed test records are preserved.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

@Composable
private fun JoinFooter(
    joinInProgress: Boolean,
    joinError: String?,
    onJoin: () -> Unit,
    isArchived: Boolean = false,
) {
    val l = AppL10n.current
    Surface(
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(AppSpacing.Md)) {
            AppButton(
                text = when {
                    isArchived -> "已下架 / Unavailable"
                    joinInProgress -> l.appdetail_cta_join_opening
                    else -> l.appdetail_cta_join_credits
                },
                onClick = onJoin,
                enabled = !isArchived && !joinInProgress,
                loading = joinInProgress,
                variant = AppButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth(),
            )
            if (joinError != null) {
                AppText(
                    text = joinError,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = AppSpacing.Xs),
                )
            }
            // "Maybe later — back to feed" hint removed (2026-05-24): non-clickable
            // text was confusing UX; users have the back arrow in the top bar already.
        }
    }
}
