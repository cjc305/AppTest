@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.apptest.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.apptest.core.designsystem.components.AppIcon
import com.apptest.core.designsystem.components.AppText
import com.apptest.core.designsystem.spacing.AppSpacing
import com.apptest.core.designsystem.theme.AppL10n
import com.apptest.core.ui.components.AppButton
import com.apptest.core.ui.components.AppButtonVariant
import com.apptest.core.ui.components.AppCard
import com.apptest.core.ui.components.AppErrorState
import com.apptest.core.ui.components.AppListItem
import com.apptest.core.ui.components.AppLoadingState
import com.apptest.core.ui.components.AppTopBar
import com.apptest.core.ui.templates.ScreenScaffold
import com.apptest.feature.profile.domain.model.ProfileData
import com.apptest.feature.profile.ui.components.ProfileHeader
import com.apptest.feature.profile.ui.components.ReputationBreakdownCard
import com.apptest.feature.profile.ui.components.StatsCard

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onSettingsClick: () -> Unit,
    onInboxClick: () -> Unit,
    onProofClick: (String) -> Unit,
    onInviteClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val l = AppL10n.current
    ScreenScaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = l.profile_title,
                actions = {
                    IconButton(onClick = onInboxClick) {
                        AppIcon(Icons.Filled.Email, contentDescription = l.profile_action_inbox)
                    }
                    IconButton(onClick = onSettingsClick) {
                        AppIcon(Icons.Filled.Settings, contentDescription = l.profile_action_settings)
                    }
                },
            )
        },
    ) { padding ->
        when (state) {
            ProfileUiState.Loading -> AppLoadingState(modifier = Modifier.padding(padding))
            is ProfileUiState.Error -> AppErrorState(state.error, onRetry = onRetry, modifier = Modifier.padding(padding))
            is ProfileUiState.Loaded -> ProfileBody(
                data = state.data,
                onProofClick = onProofClick,
                onInviteClick = onInviteClick,
                contentPadding = padding,
            )
        }
    }
}

@Composable
private fun ProfileBody(
    data: ProfileData,
    onProofClick: (String) -> Unit,
    onInviteClick: () -> Unit,
    contentPadding: PaddingValues,
) {
    val l = AppL10n.current
    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.Md),
        modifier = Modifier.padding(horizontal = AppSpacing.Md),
    ) {
        item { ProfileHeader(user = data.user) }
        item { StatsCard(stats = data.stats) }
        item { ReputationBreakdownCard(breakdown = data.breakdown) }
        item { ProofsSummary(count = data.proofs.size, onFirstClick = { data.proofs.firstOrNull()?.let { onProofClick(it.proofId) } }) }
        item {
            AppText(l.profile_activity_title, style = MaterialTheme.typography.titleMedium)
        }
        items(data.activity, key = { it.id }) { evt ->
            AppListItem(headline = evt.label, supporting = evt.timestampDisplay)
        }
        item {
            AppButton(
                text = l.profile_invite_cta,
                onClick = onInviteClick,
                variant = AppButtonVariant.Tonal,
                modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.Sm),
            )
        }
    }
}

@Composable
private fun ProofsSummary(count: Int, onFirstClick: () -> Unit) {
    val l = AppL10n.current
    AppCard(onClick = if (count > 0) onFirstClick else null, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AppSpacing.Md)) {
            AppText(l.profile_proofs_title.format(count), style = MaterialTheme.typography.titleMedium)
            AppText(
                text = if (count > 0) l.profile_proofs_tap else l.profile_proofs_empty,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
