@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.apptest.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apptest.core.designsystem.components.AppText
import com.apptest.core.designsystem.components.AppVSpacer
import com.apptest.core.designsystem.spacing.AppSpacing
import com.apptest.core.designsystem.theme.AppL10n
import com.apptest.core.ui.components.AppEmptyState
import com.apptest.core.ui.components.AppErrorState
import com.apptest.core.ui.components.AppListItem
import com.apptest.core.ui.components.AppLoadingState
import com.apptest.core.ui.components.AppProgressBar
import com.apptest.core.ui.components.AppTierBadge
import com.apptest.core.ui.components.AppTierBadgeSize
import com.apptest.core.ui.components.AppTopBar
import com.apptest.core.ui.templates.ScreenScaffold
import com.apptest.feature.home.domain.model.ActiveTest
import com.apptest.feature.home.domain.model.HomeData
import com.apptest.feature.home.domain.model.HomeUser
import com.apptest.feature.home.domain.model.OwnedApp
import com.apptest.feature.home.ui.components.HeroMatchCard

@Composable
fun HomeScreen(
    state: HomeUiState,
    onRetry: () -> Unit,
    onAppClick: (String) -> Unit,
    onJoinMatch: (String) -> Unit,
    onSkipMatch: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val l = AppL10n.current
    ScreenScaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = l.appName) },
    ) { padding ->
        when (state) {
            HomeUiState.Loading -> AppLoadingState(modifier = Modifier.padding(padding))
            is HomeUiState.Error -> AppErrorState(state.error, onRetry = onRetry, modifier = Modifier.padding(padding))
            is HomeUiState.Empty -> Column(modifier = Modifier.padding(padding)) {
                state.poolStats?.let { PoolStatusBanner(stats = it) }
                AppEmptyState(
                    illustration = Icons.Filled.Email,
                    title = l.testing_empty_title,
                    description = l.testing_empty_desc.format(state.nextBatchEta),
                )
            }
            is HomeUiState.Loaded -> HomeLoadedContent(
                data = state.data,
                poolStats = state.poolStats,
                contentPadding = padding,
                onAppClick = onAppClick,
                onJoinMatch = onJoinMatch,
                onSkipMatch = onSkipMatch,
            )
        }
    }
}

@Composable
private fun HomeLoadedContent(
    data: HomeData,
    poolStats: PoolStatsSnapshot?,
    contentPadding: PaddingValues,
    onAppClick: (String) -> Unit,
    onJoinMatch: (String) -> Unit,
    onSkipMatch: (String) -> Unit,
) {
    val l = AppL10n.current
    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
        modifier = Modifier.padding(horizontal = AppSpacing.Md),
    ) {
        // Pool-status banner above greeting — surfaces cold-start state ("waiting for testers")
        // or healthy-pool reassurance ("N developers online"). Hidden if fetch failed.
        poolStats?.let { stats ->
            item { PoolStatusBanner(stats = stats) }
        }
        item { GreetingHeader(user = data.user) }

        item { SectionLabel(l.home_section_today) }
        item {
            if (data.newMatch != null) {
                HeroMatchCard(
                    match = data.newMatch,
                    onJoin = { onJoinMatch(data.newMatch.id) },
                    onSkip = { onSkipMatch(data.newMatch.id) },
                )
            } else {
                AppText(
                    text = l.home_no_match_today,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (data.activeTests.isNotEmpty()) {
            item { SectionLabel(l.home_section_active_tests.format(data.activeTests.size)) }
            items(items = data.activeTests, key = { it.appId }) { test ->
                ActiveTestRow(test = test, onClick = { onAppClick(test.appId) })
            }
        }

        if (data.myApps.isNotEmpty()) {
            item { SectionLabel(l.home_section_your_apps.format(data.myApps.size)) }
            items(items = data.myApps, key = { it.id }) { app ->
                OwnedAppRow(app = app, onClick = { onAppClick(app.id) })
            }
        }

        item { AppVSpacer(AppSpacing.Xl) }
    }
}

@Composable
private fun GreetingHeader(user: HomeUser) {
    val l = AppL10n.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.Md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
    ) {
        AppText(
            text = l.home_greeting.format(user.displayName),
            style = MaterialTheme.typography.headlineSmall,
        )
        AppTierBadge(tier = user.tier, size = AppTierBadgeSize.Small, showLabel = false)
        AppText(
            text = l.home_credits_suffix.format(user.credits),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    AppText(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = AppSpacing.Md, bottom = AppSpacing.Xs),
    )
}

@Composable
private fun ActiveTestRow(test: ActiveTest, onClick: () -> Unit) {
    val l = AppL10n.current
    val supporting = buildString {
        append(l.home_day_progress.format(test.day, test.totalDays))
        if (!test.pingStatusOk) append(l.home_ping_overdue)
    }
    AppListItem(
        headline = test.appName,
        supporting = supporting,
        trailing = {
            // LOW-006: guard totalDays=0 (V1 placeholder) to avoid NaN/Infinity progress.
            AppProgressBar(
                progress = if (test.totalDays > 0) test.day.toFloat() / test.totalDays else 0f,
                modifier = Modifier.fillMaxWidth(0.3f),
            )
        },
        onClick = onClick,
    )
}

/**
 * Cold-start awareness banner. Tells user the pool state so empty home isn't read as broken:
 *   - empty:    "Only you in the pool — invite friends"
 *   - small:    "N active app(s), immediate matching on new arrivals"
 *   - healthy:  "N developers in pool, next batch at 02:00 UTC"
 */
@Composable
private fun PoolStatusBanner(stats: PoolStatsSnapshot) {
    val (titleText, bodyText, tone) = when (stats.hint) {
        "empty" -> Triple(
            "🌱 配對池剛起步 / Pool is brand new",
            "目前只有你註冊。邀請開發者朋友一起來,配對才會開始。\n" +
                "Only you here so far. Invite developer friends to start matching.",
            MaterialTheme.colorScheme.tertiaryContainer,
        )
        "small" -> Triple(
            "⚡ 小池立即配對 / Small-pool active",
            "目前 ${stats.activeApps} 個 App、${stats.testers} 位測試者線上。\n" +
                "新 App 啟用時立即配對(不必等每日批次)。\n" +
                "$ {stats.activeApps} apps × ${stats.testers} testers — instant matching on new app activation.",
            MaterialTheme.colorScheme.primaryContainer,
        )
        else -> Triple(
            "✅ 配對池健康 / Pool healthy",
            "${stats.activeApps} 個 App、${stats.testers} 位測試者線上。每天 02:00 UTC 批次配對。\n" +
                "${stats.activeApps} apps × ${stats.testers} testers — daily batch at 02:00 UTC.",
            MaterialTheme.colorScheme.secondaryContainer,
        )
    }
    Surface(
        tonalElevation = 2.dp,
        color = tone,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.Xs),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.Md)) {
            AppText(text = titleText, style = MaterialTheme.typography.titleSmall)
            AppVSpacer(AppSpacing.Xs)
            AppText(text = bodyText, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun OwnedAppRow(app: OwnedApp, onClick: () -> Unit) {
    val l = AppL10n.current
    AppListItem(
        headline = app.name,
        supporting = l.home_owned_testers.format(app.currentTesters, app.requiredTesters),
        trailing = {
            // LOW-006: guard requiredTesters=0 to avoid NaN/Infinity progress.
            AppProgressBar(
                progress = if (app.requiredTesters > 0) app.currentTesters.toFloat() / app.requiredTesters else 0f,
                modifier = Modifier.fillMaxWidth(0.3f),
            )
        },
        onClick = onClick,
    )
}
