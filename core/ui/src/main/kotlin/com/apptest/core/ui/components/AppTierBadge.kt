package com.apptest.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.apptest.core.common.ReputationTier
import com.apptest.core.designsystem.components.AppText
import com.apptest.core.designsystem.theme.AppExtended

enum class AppTierBadgeSize { Small, Medium, Large }

/**
 * Reputation tier visualization per `_specs/compose_components.md §3 AppTierBadge`.
 * Owns tier→color mapping (from [AppExtended.colors]) so callers never hard-code tier colors.
 *
 * Platinum gets a 135° linear gradient (start→end). Subtle shimmer animation deferred to
 * future polish task — current implementation is static gradient.
 */
@Composable
fun AppTierBadge(
    tier: ReputationTier,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    size: AppTierBadgeSize = AppTierBadgeSize.Medium,
) {
    val dim: Dp = when (size) {
        AppTierBadgeSize.Small -> 16.dp
        AppTierBadgeSize.Medium -> 24.dp
        AppTierBadgeSize.Large -> 32.dp
    }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        TierDot(tier = tier, dim = dim)
        if (showLabel) {
            Spacer(modifier = Modifier.width(6.dp))
            AppText(
                text = tier.name,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun TierDot(tier: ReputationTier, dim: Dp) {
    val ext = AppExtended.colors
    when (tier) {
        ReputationTier.Platinum -> Box(
            modifier = Modifier
                .size(dim)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(ext.tierPlatinumStart, ext.tierPlatinumEnd),
                    ),
                ),
        )
        else -> {
            val solid: Color = when (tier) {
                ReputationTier.Newcomer -> ext.tierNewcomer
                ReputationTier.Bronze -> ext.tierBronze
                ReputationTier.Silver -> ext.tierSilver
                ReputationTier.Gold -> ext.tierGold
                ReputationTier.Platinum -> ext.tierPlatinumStart   // unreachable
            }
            Box(
                modifier = Modifier
                    .size(dim)
                    .clip(CircleShape)
                    .background(solid),
            )
        }
    }
}
