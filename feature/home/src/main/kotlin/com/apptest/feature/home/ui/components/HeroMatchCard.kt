package com.apptest.feature.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.apptest.core.designsystem.components.AppText
import com.apptest.core.designsystem.components.AppVSpacer
import com.apptest.core.designsystem.spacing.AppSpacing
import com.apptest.core.designsystem.theme.AppL10n
import com.apptest.core.ui.components.AppButton
import com.apptest.core.ui.components.AppButtonVariant
import com.apptest.core.ui.components.AppCard
import com.apptest.feature.home.domain.model.MatchedApp

/**
 * Hero card for the "Today" section — the conversion focal point of the Home screen.
 * Split out from [com.apptest.feature.home.ui.HomeScreen] for visual / behavioural complexity
 * (description truncation, dual CTA, accent label).
 */
@Composable
internal fun HeroMatchCard(
    match: MatchedApp,
    onJoin: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val l = AppL10n.current
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AppSpacing.Md)) {
            AppText(
                text = l.home_new_match_label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            AppVSpacer(AppSpacing.Xs)
            AppText("${match.name} · ${match.category}", style = MaterialTheme.typography.titleLarge)
            AppVSpacer(AppSpacing.Xs)
            AppText(match.description, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
            AppVSpacer(AppSpacing.Sm)
            AppText(
                text = l.home_testers_needed.format(match.testersNeeded),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppVSpacer(AppSpacing.Md)
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.Sm)) {
                AppButton(text = l.home_cta_skip_match, onClick = onSkip, variant = AppButtonVariant.Text)
                AppButton(text = l.home_cta_join, onClick = onJoin, variant = AppButtonVariant.Primary)
            }
        }
    }
}
