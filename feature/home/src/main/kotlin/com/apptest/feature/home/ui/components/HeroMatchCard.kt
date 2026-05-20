package com.apptest.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.apptest.core.designsystem.components.AppText
import com.apptest.core.designsystem.components.AppVSpacer
import com.apptest.core.designsystem.spacing.AppSpacing
import com.apptest.core.designsystem.theme.AppL10n
import com.apptest.core.ui.components.AppButton
import com.apptest.core.ui.components.AppButtonVariant
import com.apptest.core.ui.components.AppCard
import com.apptest.feature.home.domain.model.MatchedApp

/**
 * Hero card for the "Today" section -- the conversion focal point of the Home screen.
 * Split out from [com.apptest.feature.home.ui.HomeScreen] for visual / behavioural complexity
 * (description truncation, dual CTA, accent label).
 *
 * Visual improvements:
 * - 4dp primary accent bar at the top edge for immediate brand recognition
 * - Join button fills remaining row width to maximise tap target
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
        Column {
            // Accent top bar -- 4dp primary stripe draws eye to the card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primary),
            )
            Column(modifier = Modifier.padding(AppSpacing.Md)) {
                AppText(
                    text = l.home_new_match_label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                AppVSpacer(AppSpacing.Xs)
                AppText(
                    text = "${match.name} · ${match.category}",
                    style = MaterialTheme.typography.titleLarge,
                )
                AppVSpacer(AppSpacing.Xs)
                AppText(
                    text = match.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                )
                AppVSpacer(AppSpacing.Sm)
                AppText(
                    text = l.home_testers_needed.format(match.testersNeeded),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppVSpacer(AppSpacing.Md)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
                ) {
                    AppButton(
                        text = l.home_cta_skip_match,
                        onClick = onSkip,
                        variant = AppButtonVariant.Text,
                    )
                    AppButton(
                        text = l.home_cta_join,
                        onClick = onJoin,
                        variant = AppButtonVariant.Primary,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
