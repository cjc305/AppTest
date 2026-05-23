package com.apptest.app.nav

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.apptest.core.designsystem.components.AppText
import com.apptest.core.designsystem.components.AppVSpacer
import com.apptest.core.designsystem.spacing.AppSpacing
import com.apptest.core.designsystem.theme.AppL10n
import com.apptest.core.ui.components.AppButton
import com.apptest.core.ui.templates.ScreenScaffold

/** Temporary Settings screen — full :feature:settings module planned for V2. */
@Composable
internal fun SettingsStub(onSignOut: () -> Unit, onBack: () -> Unit) {
    val l = AppL10n.current
    ScreenScaffold { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).padding(AppSpacing.Lg),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.Md),
            ) {
                AppText(l.settings_title, style = MaterialTheme.typography.headlineMedium)
                AppText(
                    l.settings_locale_note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppVSpacer(AppSpacing.Md)
                AppButton(l.cta_sign_out, onSignOut)
                AppVSpacer(AppSpacing.Sm)
                AppButton(l.cta_back, onBack)
            }
        }
    }
}
