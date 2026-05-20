package com.apptest.feature.appdetail.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apptest.core.designsystem.components.AppText
import com.apptest.core.designsystem.spacing.AppSpacing
import com.apptest.core.designsystem.theme.AppL10n
import com.apptest.core.ui.components.AppTierBadge
import com.apptest.core.ui.components.AppTierBadgeSize
import com.apptest.feature.appdetail.domain.model.AppDetailData

/**
 * Top-of-screen App identity block: icon placeholder + name + category + owner tier.
 * Per anonymity hard rule (`product_architecture.md §5 #4`) owner display is name + tier
 * only; no link to historical Apps.
 */
@Composable
internal fun AppDetailHeader(
    data: AppDetailData,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(AppSpacing.Md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.Md),
    ) {
        IconPlaceholder(letter = data.name.firstOrNull()?.uppercase() ?: "?")
        Column(modifier = Modifier.weight(1f)) {
            AppText(text = data.name, style = MaterialTheme.typography.headlineSmall)
            AppText(
                text = data.category,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.Xs),
                modifier = Modifier.padding(top = AppSpacing.Xs),
            ) {
                AppText(
                    text = AppL10n.current.appdetail_by_owner.format(data.owner.displayName),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppTierBadge(tier = data.owner.tier, size = AppTierBadgeSize.Small, showLabel = false)
            }
        }
    }
}

@Composable
private fun IconPlaceholder(letter: String) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        AppText(
            text = letter,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
