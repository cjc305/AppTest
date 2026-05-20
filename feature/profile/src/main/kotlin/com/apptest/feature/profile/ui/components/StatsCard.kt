package com.apptest.feature.profile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.apptest.core.designsystem.components.AppText
import com.apptest.core.designsystem.spacing.AppSpacing
import com.apptest.core.designsystem.theme.AppL10n
import com.apptest.core.ui.components.AppCard
import com.apptest.feature.profile.domain.model.ProfileStats30d

/**
 * 30-day stats summary card on the Profile screen.
 *
 * Visual improvements:
 * - Each stat value uses primaryContainer background chip for visual separation
 * - Value text in primary color (was onSurface) to increase numeric prominence
 */
@Composable
internal fun StatsCard(stats: ProfileStats30d, modifier: Modifier = Modifier) {
    val l = AppL10n.current
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.Md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.Md),
        ) {
            AppText(l.profile_stats_title, style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Stat(label = l.profile_stats_completed, value = "${stats.completedTests}")
                Stat(label = l.profile_stats_days, value = "${stats.daysContributed}")
                Stat(
                    label = l.profile_stats_rep,
                    value = (if (stats.reputationDelta >= 0) "+" else "") + stats.reputationDelta,
                )
                Stat(label = l.profile_stats_streak, value = l.profile_stats_streak_value.format(stats.streakDays))
            }
        }
    }
}

@com.apptest.core.designsystem.preview.AppPreviewLightDark
@Composable
private fun StatsCardPreview() = com.apptest.core.designsystem.preview.AppPreviewTheme {
    StatsCard(
        stats = com.apptest.feature.profile.domain.model.ProfileStats30d(
            completedTests = 14,
            daysContributed = 22,
            reputationDelta = 18,
            streakDays = 7,
        ),
    )
}

@Composable
private fun Stat(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.Xs),
    ) {
        // Chip background gives each number its own visual container
        AppText(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small,
                )
                .padding(horizontal = AppSpacing.Sm, vertical = AppSpacing.Xs),
        )
        AppText(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
