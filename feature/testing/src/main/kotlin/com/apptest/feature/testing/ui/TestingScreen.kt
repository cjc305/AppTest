@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.apptest.feature.testing.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.apptest.core.common.AppStrings
import com.apptest.core.designsystem.components.AppText
import com.apptest.core.designsystem.spacing.AppSpacing
import com.apptest.core.designsystem.theme.AppL10n
import com.apptest.core.ui.components.AppButton
import com.apptest.core.ui.components.AppButtonVariant
import com.apptest.core.ui.components.AppEmptyState
import com.apptest.core.ui.components.AppErrorState
import com.apptest.core.ui.components.AppFilterChip
import com.apptest.core.ui.components.AppListItem
import com.apptest.core.ui.components.AppLoadingState
import com.apptest.core.ui.components.AppProgressBar
import com.apptest.core.ui.components.AppTopBar
import com.apptest.core.ui.templates.ScreenScaffold
import com.apptest.feature.testing.domain.model.ActiveTestEntry
import com.apptest.feature.testing.domain.model.CompletedTestEntry
import com.apptest.feature.testing.domain.model.TestFilter
import com.apptest.feature.testing.domain.model.TestStatus

@Composable
fun TestingScreen(
    state: TestingUiState,
    onFilterChange: (TestFilter) -> Unit,
    onTestClick: (String) -> Unit,
    onHeartbeat: (String) -> Unit,
    onAbandon: (String) -> Unit,
    onProofClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val l = AppL10n.current
    ScreenScaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = l.testing_title) },
    ) { padding ->
        when (state) {
            TestingUiState.Loading -> AppLoadingState(modifier = Modifier.padding(padding))
            is TestingUiState.Error -> AppErrorState(state.error, modifier = Modifier.padding(padding))
            is TestingUiState.Empty -> AppEmptyState(
                illustration = Icons.Filled.Email,
                title = l.testing_empty_title,
                description = l.testing_empty_desc.format(state.nextBatchEta),
                modifier = Modifier.padding(padding),
            )
            is TestingUiState.Loaded -> LoadedBody(
                state = state,
                onFilterChange = onFilterChange,
                onTestClick = onTestClick,
                onHeartbeat = onHeartbeat,
                onAbandon = onAbandon,
                onProofClick = onProofClick,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding(),
                ),
            )
        }
    }
}

@Composable
private fun LoadedBody(
    state: TestingUiState.Loaded,
    onFilterChange: (TestFilter) -> Unit,
    onTestClick: (String) -> Unit,
    onHeartbeat: (String) -> Unit,
    onAbandon: (String) -> Unit,
    onProofClick: (String) -> Unit,
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
) {
    val l = AppL10n.current
    val showActive = state.filter != TestFilter.Done
    val showDone = state.filter != TestFilter.Active

    LazyColumn(
        contentPadding = contentPadding,
        modifier = Modifier.padding(horizontal = AppSpacing.Md),
    ) {
        item { FilterRow(current = state.filter, onChange = onFilterChange) }

        if (showActive && state.snapshot.active.isNotEmpty()) {
            item { SectionHeader(l.testing_section_active.format(state.snapshot.active.size)) }
            items(state.snapshot.active, key = { it.testId }) { e ->
                ActiveRow(e, onClick = { onTestClick(e.appId) }, onHeartbeat = { onHeartbeat(e.testId) }, onAbandon = { onAbandon(e.testId) })
            }
        }
        if (showDone && state.snapshot.completed.isNotEmpty()) {
            item { SectionHeader(l.testing_section_completed.format(state.snapshot.completed.size)) }
            items(state.snapshot.completed, key = { it.testId }) { e ->
                CompletedRow(e, onProofClick = { e.proofId?.let(onProofClick) })
            }
        }
    }
}

private fun filterLabel(f: TestFilter, l: AppStrings): String = when (f) {
    TestFilter.Active -> l.testing_filter_active
    TestFilter.Done -> l.testing_filter_done
    TestFilter.All -> l.testing_filter_all
}

@Composable
private fun FilterRow(current: TestFilter, onChange: (TestFilter) -> Unit) {
    val l = AppL10n.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.Sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
    ) {
        TestFilter.entries.forEach { f ->
            AppFilterChip(
                text = filterLabel(f, l),
                selected = current == f,
                onSelectedChange = { if (it) onChange(f) },
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    AppText(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = AppSpacing.Md, bottom = AppSpacing.Xs),
    )
}

@Composable
private fun ActiveRow(e: ActiveTestEntry, onClick: () -> Unit, onHeartbeat: () -> Unit, onAbandon: () -> Unit) {
    val l = AppL10n.current
    val sub = if (e.status == TestStatus.AtRisk) {
        l.testing_active_supporting_atrisk.format(e.day, e.totalDays)
    } else {
        l.testing_active_supporting.format(e.day, e.totalDays)
    }
    AppListItem(
        headline = e.appName,
        supporting = sub,
        trailing = {
            AppProgressBar(
                progress = e.day.toFloat() / e.totalDays.toFloat(),
                modifier = Modifier.fillMaxWidth(0.3f),
            )
        },
        onClick = onClick,
    )
    if (e.status == TestStatus.AtRisk) {
        Row(modifier = Modifier.padding(bottom = AppSpacing.Sm), horizontalArrangement = Arrangement.spacedBy(AppSpacing.Sm)) {
            AppButton(l.testing_cta_heartbeat, onClick = onHeartbeat, variant = AppButtonVariant.Tonal)
            AppButton(l.testing_cta_abandon, onClick = onAbandon, variant = AppButtonVariant.Text)
        }
    }
}

@Composable
private fun CompletedRow(e: CompletedTestEntry, onProofClick: () -> Unit) {
    val l = AppL10n.current
    AppListItem(
        headline = l.testing_completed_row.format(e.appName, e.daysCompleted, e.reputationGained),
        supporting = if (e.proofId != null) l.testing_view_proof else l.testing_proof_pending,
        onClick = onProofClick,
    )
}
