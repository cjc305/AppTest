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
import com.apptest.core.ui.components.AppProgressBar
import com.apptest.feature.appdetail.domain.model.Requirements

@Composable
internal fun RequirementsSection(
    requirements: Requirements,
    modifier: Modifier = Modifier,
) {
    val l = AppL10n.current
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.Md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
        ) {
            AppText(text = l.appdetail_requirements_title, style = MaterialTheme.typography.titleMedium)
            Bullet(l.appdetail_req_days_testers.format(requirements.requiredDays, requirements.requiredTesters))
            Bullet(l.appdetail_req_daily.format(requirements.dailyMinutesEstimated))
            Bullet(l.appdetail_req_current.format(requirements.currentTesters, requirements.requiredTesters))
            AppProgressBar(
                progress = requirements.currentTesters.toFloat() / requirements.requiredTesters.toFloat(),
                label = null,
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.Xs),
            )
        }
    }
}

@Composable
private fun Bullet(text: String) {
    AppText(
        text = "• $text",
        style = MaterialTheme.typography.bodyMedium,
    )
}
