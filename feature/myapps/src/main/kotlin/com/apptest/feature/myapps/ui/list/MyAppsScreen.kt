@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.apptest.feature.myapps.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.apptest.core.designsystem.spacing.AppSpacing
import com.apptest.core.designsystem.theme.AppL10n
import com.apptest.core.ui.components.AppEmptyState
import com.apptest.core.ui.components.AppErrorState
import com.apptest.core.ui.components.AppFAB
import com.apptest.core.ui.components.AppListItem
import com.apptest.core.ui.components.AppLoadingState
import com.apptest.core.ui.components.AppProgressBar
import com.apptest.core.ui.components.AppTopBar
import com.apptest.core.ui.templates.ScreenScaffold
import com.apptest.feature.myapps.domain.model.OwnedAppRow

@Composable
fun MyAppsScreen(
    state: MyAppsUiState,
    onCreate: () -> Unit,
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val l = AppL10n.current
    ScreenScaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = l.myapps_title) },
        fab = {
            AppFAB(
                icon = Icons.Filled.Add,
                onClick = onCreate,
                text = l.myapps_fab_create,
            )
        },
    ) { padding ->
        when (state) {
            MyAppsUiState.Loading -> AppLoadingState(modifier = Modifier.padding(padding))
            is MyAppsUiState.Error -> AppErrorState(
                error = state.error,
                modifier = Modifier.padding(padding),
            )
            MyAppsUiState.Empty -> AppEmptyState(
                illustration = Icons.Filled.Star,
                title = l.myapps_empty_title,
                description = l.myapps_empty_desc,
                ctaText = l.myapps_empty_cta,
                onCta = onCreate,
                modifier = Modifier.padding(padding),
            )
            is MyAppsUiState.Loaded -> LazyColumn(
                contentPadding = padding,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
                modifier = Modifier.padding(horizontal = AppSpacing.Md),
            ) {
                items(items = state.items, key = { it.id }) { row ->
                    OwnedAppListItem(row = row, onClick = { onEdit(row.id) })
                }
            }
        }
    }
}

@Composable
private fun OwnedAppListItem(row: OwnedAppRow, onClick: () -> Unit) {
    val l = AppL10n.current
    val supporting = buildString {
        append(l.myapps_row_supporting.format(row.status.name.lowercase(), row.currentTesters, row.requiredTesters))
        if (row.daysLeft > 0) append(l.myapps_days_left.format(row.daysLeft))
    }
    AppListItem(
        headline = row.name,
        supporting = supporting,
        trailing = {
            AppProgressBar(
                progress = row.currentTesters.toFloat() / row.requiredTesters.toFloat(),
                modifier = Modifier.fillMaxWidth(0.3f),
            )
        },
        onClick = onClick,
        modifier = Modifier.padding(vertical = AppSpacing.Xxs),
    )
}
