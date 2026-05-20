package com.apptest.feature.profile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.apptest.core.designsystem.components.AppText
import com.apptest.core.designsystem.spacing.AppSpacing
import com.apptest.core.designsystem.theme.AppL10n
import com.apptest.core.ui.components.AppCard
import com.apptest.core.ui.components.AppProgressBar
import com.apptest.feature.profile.domain.model.ReputationBreakdown

@Composable
internal fun ReputationBreakdownCard(breakdown: ReputationBreakdown, modifier: Modifier = Modifier) {
    val l = AppL10n.current
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AppSpacing.Md), verticalArrangement = Arrangement.spacedBy(AppSpacing.Sm)) {
            AppText(l.profile_breakdown_title, style = MaterialTheme.typography.titleMedium)
            ScoreRow(l.profile_breakdown_completion, breakdown.completionRate, max = 40)
            ScoreRow(l.profile_breakdown_streak, breakdown.streak, max = 20)
            ScoreRow(l.profile_breakdown_volume, breakdown.volume, max = 15)
            ScoreRow(l.profile_breakdown_publish, breakdown.publish, max = 25)
            if (breakdown.penalty > 0) {
                AppText(
                    text = l.profile_breakdown_penalty.format(breakdown.penalty),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun ScoreRow(label: String, value: Int, max: Int) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        AppText(text = label, modifier = Modifier.weight(0.4f), style = MaterialTheme.typography.bodyMedium)
        AppProgressBar(
            progress = value.toFloat() / max.toFloat(),
            modifier = Modifier.weight(0.5f).padding(horizontal = AppSpacing.Sm),
        )
        AppText(
            text = "$value",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(0.1f),
        )
    }
}
