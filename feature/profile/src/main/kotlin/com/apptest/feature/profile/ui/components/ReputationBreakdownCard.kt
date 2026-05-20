package com.apptest.feature.profile.ui.components

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
import com.apptest.core.ui.components.AppProgressBar
import com.apptest.feature.profile.domain.model.ReputationBreakdown

private const val SCORE_LABEL_WEIGHT = 0.38f
private const val SCORE_BAR_WEIGHT   = 0.48f
private const val SCORE_VALUE_WEIGHT = 0.14f

/**
 * Credit score breakdown card on the Profile screen.
 *
 * Visual improvements:
 * - Score label now shows "value / max" to communicate absolute headroom
 * - Progress bar color shifts: >=80% primary, 50-79% tertiary (amber), <50% error
 *   giving instant at-a-glance health signal per category
 */
@Composable
internal fun ReputationBreakdownCard(breakdown: ReputationBreakdown, modifier: Modifier = Modifier) {
    val l = AppL10n.current
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.Md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
        ) {
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

@com.apptest.core.designsystem.preview.AppPreviewLightDark
@Composable
private fun ReputationBreakdownCardPreview() = com.apptest.core.designsystem.preview.AppPreviewTheme {
    ReputationBreakdownCard(
        breakdown = com.apptest.feature.profile.domain.model.ReputationBreakdown(
            completionRate = 34,
            streak = 12,
            volume = 10,
            publish = 20,
            penalty = 0,
        ),
    )
}

@Composable
private fun ScoreRow(label: String, value: Int, max: Int) {
    // Color encodes health: green (>=80%) / amber (50-79%) / red (<50%)
    val pct = value.toFloat() / max.toFloat()
    val barColor = when {
        pct >= 0.8f -> MaterialTheme.colorScheme.primary
        pct >= 0.5f -> MaterialTheme.colorScheme.tertiary
        else        -> MaterialTheme.colorScheme.error
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppText(
            text = label,
            modifier = Modifier.weight(SCORE_LABEL_WEIGHT),
            style = MaterialTheme.typography.bodyMedium,
        )
        AppProgressBar(
            progress = pct,
            color = barColor,
            modifier = Modifier
                .weight(SCORE_BAR_WEIGHT)
                .padding(horizontal = AppSpacing.Sm),
        )
        AppText(
            text = "$value / $max",
            style = MaterialTheme.typography.labelMedium,
            color = barColor,
            modifier = Modifier.weight(SCORE_VALUE_WEIGHT),
        )
    }
}
