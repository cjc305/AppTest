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
    onJoin: () -> Unit,
    contentPadding: PaddingValues,
) {
    Column(modifier = Modifier.padding(contentPadding).fillMaxWidth()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.Md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.Md),
        ) {
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
        JoinFooter(joinInProgress = joinInProgress, onJoin = onJoin)
    }
}

@Composable
private fun JoinFooter(joinInProgress: Boolean, onJoin: () -> Unit) {
    val l = AppL10n.current
    Surface(
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(AppSpacing.Md)) {
            AppButton(
                text = if (joinInProgress) l.appdetail_cta_join_opening else l.appdetail_cta_join_credits,
                onClick = onJoin,
                enabled = !joinInProgress,
                loading = joinInProgress,
                variant = AppButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth(),
            )
            AppText(
                text = l.appdetail_maybe_later,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = AppSpacing.Xs),
            )
        }
    }
}
