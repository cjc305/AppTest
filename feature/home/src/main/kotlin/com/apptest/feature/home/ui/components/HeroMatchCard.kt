package com.apptest.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apptest.core.designsystem.components.AppText
import com.apptest.core.designsystem.components.AppVSpacer
import com.apptest.core.designsystem.spacing.AppSpacing
import com.apptest.core.designsystem.theme.AppL10n
import com.apptest.core.ui.components.AppButton
import com.apptest.core.ui.components.AppButtonVariant
import com.apptest.core.ui.components.AppCard
import com.apptest.feature.home.domain.model.MatchedApp

// Brand gradient — explicit so it shows regardless of Dynamic Color wallpaper.
private val HeroGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF4F378B), Color(0xFF6750A4)),
)
private val AmberScore = Color(0xFFFFCB47)

/**
 * Hero card for the "Today" section — conversion focal point of the Home screen.
 *
 * Visual design (Claude Design prototype):
 * - Gradient header (brand purple) with app name + category chip
 * - AI match score badge (amber, top-right) when matchScore > 0
 * - Description + testers-needed label below
 * - Skip (text) / Join (filled) CTAs
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
            // ── Gradient header ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeroGradient)
                    .padding(horizontal = AppSpacing.Md, vertical = AppSpacing.Sm),
            ) {
                Column(modifier = Modifier.align(Alignment.CenterStart)) {
                    AppText(
                        text = l.home_new_match_label,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.75f),
                    )
                    AppText(
                        text = match.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                    AppText(
                        text = match.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.65f),
                    )
                }
                // AI match score badge — shown only when score is available
                if (match.matchScore > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(52.dp)
                            .background(
                                color = AmberScore,
                                shape = MaterialTheme.shapes.medium,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AppText(
                                text = "${match.matchScore}%",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color(0xFF1A0A00),
                            )
                            AppText(
                                text = "match",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF1A0A00),
                            )
                        }
                    }
                }
            }

            // ── Body ─────────────────────────────────────────────────────────
            Column(modifier = Modifier.padding(AppSpacing.Md)) {
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

@com.apptest.core.designsystem.preview.AppPreviewLightDark
@Composable
private fun HeroMatchCardPreview() = com.apptest.core.designsystem.preview.AppPreviewTheme {
    HeroMatchCard(
        match = com.apptest.feature.home.domain.model.MatchedApp(
            id = "1",
            name = "DiceX 3D",
            category = "Games",
            description = "A 3D dice rolling app for tabletop gaming enthusiasts.",
            testersNeeded = 3,
            matchScore = 87,
        ),
        onJoin = {},
        onSkip = {},
    )
}
