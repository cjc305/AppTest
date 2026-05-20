@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.apptest.feature.onboarding.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.apptest.core.common.AppStrings
import com.apptest.core.designsystem.components.AppText
import com.apptest.core.designsystem.components.AppVSpacer
import com.apptest.core.designsystem.spacing.AppSpacing
import com.apptest.core.designsystem.theme.AppL10n
import com.apptest.core.ui.components.AppButton
import com.apptest.core.ui.components.AppButtonVariant
import com.apptest.core.ui.components.AppFilterChip
import com.apptest.core.ui.components.AppProgressBar
import com.apptest.core.ui.components.AppTopBar
import com.apptest.core.ui.templates.ScreenScaffold
import com.apptest.feature.onboarding.domain.model.OnboardingCatalog
import com.apptest.feature.onboarding.domain.model.OnboardingIntent

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onIntentChange: (OnboardingIntent) -> Unit,
    onCategoryToggle: (String) -> Unit,
    onLanguageToggle: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val l = AppL10n.current
    ScreenScaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = l.onboarding_step_format.format(state.currentStep, state.totalSteps),
                actions = {
                    TextButton(onClick = onSkip) {
                        AppText(l.cta_skip, style = MaterialTheme.typography.labelLarge)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(AppSpacing.Md)
                .verticalScroll(rememberScrollState()),
        ) {
            AppProgressBar(
                progress = state.currentStep.toFloat() / state.totalSteps.toFloat(),
                modifier = Modifier.fillMaxWidth(),
            )
            AppVSpacer(AppSpacing.Lg)

            when (state.currentStep) {
                1 -> Step1Intent(state.draft.intent, onIntentChange)
                2 -> Step2Categories(state.draft.categories, onCategoryToggle)
                3 -> Step3Languages(state.draft.languages, onLanguageToggle)
            }

            if (state.submitError != null) {
                AppVSpacer(AppSpacing.Md)
                AppText(
                    text = l.onboarding_submit_error_prefix + (state.submitError.message ?: l.err_unknown),
                    color = MaterialTheme.colorScheme.error,
                )
            }

            AppVSpacer(AppSpacing.Xl)
            BottomActions(
                canBack = state.currentStep > 1 && !state.isSubmitting,
                canProceed = state.canProceed && !state.isSubmitting,
                isLast = state.isLastStep,
                isSubmitting = state.isSubmitting,
                onBack = onBack,
                onNext = onNext,
            )
        }
    }
}

@Composable
private fun Step1Intent(current: OnboardingIntent, onChange: (OnboardingIntent) -> Unit) {
    val l = AppL10n.current
    AppText(l.onboarding_step1_title, style = MaterialTheme.typography.headlineSmall)
    AppVSpacer(AppSpacing.Md)
    OnboardingIntent.entries.forEach { intent ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(selected = current == intent, onClick = { onChange(intent) })
                .padding(vertical = AppSpacing.Sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = current == intent, onClick = { onChange(intent) })
            AppText(intentLabel(intent, l), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private fun intentLabel(intent: OnboardingIntent, l: AppStrings): String = when (intent) {
    OnboardingIntent.FindTesters -> l.onboarding_intent_find
    OnboardingIntent.TestOthers -> l.onboarding_intent_test
    OnboardingIntent.Both -> l.onboarding_intent_both
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun Step2Categories(selected: Set<String>, onToggle: (String) -> Unit) {
    val l = AppL10n.current
    AppText(l.onboarding_step2_title, style = MaterialTheme.typography.headlineSmall)
    AppVSpacer(AppSpacing.Xs)
    AppText(
        text = l.onboarding_step2_help,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    AppVSpacer(AppSpacing.Md)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.Sm)) {
        OnboardingCatalog.ALL_CATEGORIES.forEach { cat ->
            AppFilterChip(text = cat, selected = cat in selected, onSelectedChange = { onToggle(cat) })
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun Step3Languages(selected: Set<String>, onToggle: (String) -> Unit) {
    val l = AppL10n.current
    AppText(l.onboarding_step3_title, style = MaterialTheme.typography.headlineSmall)
    AppVSpacer(AppSpacing.Xs)
    AppText(
        text = l.onboarding_step3_help,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    AppVSpacer(AppSpacing.Md)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.Sm)) {
        OnboardingCatalog.ALL_LANGUAGES.forEach { lang ->
            AppFilterChip(text = lang, selected = lang in selected, onSelectedChange = { onToggle(lang) })
        }
    }
}

@Composable
private fun BottomActions(
    canBack: Boolean,
    canProceed: Boolean,
    isLast: Boolean,
    isSubmitting: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    val l = AppL10n.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
    ) {
        if (canBack) {
            AppButton(text = l.cta_back, onClick = onBack, variant = AppButtonVariant.Text)
        }
        AppButton(
            text = when {
                isSubmitting -> l.cta_saving
                isLast -> l.onboarding_cta_done
                else -> l.cta_continue
            },
            onClick = onNext,
            enabled = canProceed,
            loading = isSubmitting,
            variant = AppButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
