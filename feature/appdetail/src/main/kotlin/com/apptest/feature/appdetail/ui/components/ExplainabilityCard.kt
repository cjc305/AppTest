package com.apptest.feature.appdetail.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.apptest.core.designsystem.components.AppText
import com.apptest.core.designsystem.spacing.AppSpacing
import com.apptest.core.designsystem.theme.AppL10n
import com.apptest.core.ui.components.AppCard
import com.apptest.feature.appdetail.domain.model.MatchReason

/**
 * "Why you got this match" explainability per `_specs/ai_matchmaking.md §10 APT-P-010` —
 * top-3 contributing factors visible to user. Builds trust + reduces matchmaking opacity.
 *
 * Empty list = hide card entirely (don't render an empty box).
 */
@Composable
internal fun ExplainabilityCard(
    reasons: List<MatchReason>,
    modifier: Modifier = Modifier,
) {
    if (reasons.isEmpty()) return
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.Md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
        ) {
            AppText(
                text = AppL10n.current.appdetail_explainability_title,
                style = MaterialTheme.typography.titleMedium,
            )
            reasons.forEach { r ->
                AppText(
                    text = "• ${r.label}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
