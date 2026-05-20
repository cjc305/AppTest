@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.apptest.feature.inbox.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.apptest.core.common.AppStrings
import com.apptest.core.designsystem.components.AppIcon
import com.apptest.core.designsystem.components.AppText
import com.apptest.core.designsystem.spacing.AppSpacing
import com.apptest.core.designsystem.theme.AppL10n
import com.apptest.core.ui.components.AppEmptyState
import com.apptest.core.ui.components.AppErrorState
import com.apptest.core.ui.components.AppListItem
import com.apptest.core.ui.components.AppLoadingState
import com.apptest.core.ui.components.AppTopBar
import com.apptest.core.ui.templates.ScreenScaffold
import com.apptest.feature.inbox.domain.model.InboxNotification
import com.apptest.feature.inbox.domain.model.InboxNotificationType
import java.time.Duration
import java.time.Instant

@Composable
fun InboxScreen(
    state: InboxUiState,
    onNavigateUp: () -> Unit,
    onItemClick: (InboxNotification) -> Unit,
    onMarkAllRead: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val l = AppL10n.current
    ScreenScaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = l.inbox_title,
                navIcon = {
                    IconButton(onClick = onNavigateUp) {
                        AppIcon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = l.cta_back)
                    }
                },
                actions = {
                    if (state is InboxUiState.Loaded && state.unreadCount > 0) {
                        TextButton(onClick = onMarkAllRead) {
                            AppText(l.inbox_mark_all_read, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                },
            )
        },
    ) { padding ->
        when (state) {
            InboxUiState.Loading -> AppLoadingState(modifier = Modifier.padding(padding))
            is InboxUiState.Error -> AppErrorState(state.error, modifier = Modifier.padding(padding))
            InboxUiState.Empty -> AppEmptyState(
                illustration = Icons.Filled.Email,
                title = l.inbox_empty_title,
                description = l.inbox_empty_desc,
                modifier = Modifier.padding(padding),
            )
            is InboxUiState.Loaded -> LazyColumn(
                contentPadding = padding,
                modifier = Modifier.padding(horizontal = AppSpacing.Md),
            ) {
                items(state.items, key = { it.id }) { n ->
                    NotificationRow(n, onClick = { onItemClick(n) })
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(n: InboxNotification, onClick: () -> Unit) {
    val l = AppL10n.current
    AppListItem(
        headline = n.title,
        supporting = "${n.body} · ${relative(n.timestamp, l)}",
        leading = { TypeDot(type = n.type, read = n.isRead) },
        onClick = onClick,
    )
}

@Composable
private fun TypeDot(type: InboxNotificationType, read: Boolean) {
    val color = when (type) {
        InboxNotificationType.NewMatch -> MaterialTheme.colorScheme.primary
        InboxNotificationType.HeartbeatReminder -> MaterialTheme.colorScheme.tertiary
        InboxNotificationType.ReputationChange -> MaterialTheme.colorScheme.secondary
        InboxNotificationType.Completion -> MaterialTheme.colorScheme.primary
        InboxNotificationType.System -> MaterialTheme.colorScheme.outline
    }
    val finalColor = if (read) color.copy(alpha = 0.4f) else color
    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(finalColor))
}

private fun relative(t: Instant, l: AppStrings): String {
    val secs = Duration.between(t, Instant.now()).seconds
    return when {
        secs < 60 -> l.time_just_now
        secs < 3600 -> l.time_min_ago.format(secs / 60)
        secs < 86400 -> l.time_hour_ago.format(secs / 3600)
        else -> l.time_day_ago.format(secs / 86400)
    }
}
