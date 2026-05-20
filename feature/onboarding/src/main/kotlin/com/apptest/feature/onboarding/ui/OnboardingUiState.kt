package com.apptest.feature.onboarding.ui

import androidx.compose.runtime.Immutable
import com.apptest.core.common.AppError
import com.apptest.feature.onboarding.domain.model.OnboardingDraft

@Immutable
data class OnboardingUiState(
    val currentStep: Int = 1,             // 1..3
    val draft: OnboardingDraft = OnboardingDraft(),
    val isSubmitting: Boolean = false,
    val submitError: AppError? = null,
) {
    val totalSteps: Int = 3
    val canProceed: Boolean = when (currentStep) {
        1 -> true                          // intent always has a default
        2 -> draft.isValidStep2
        3 -> draft.isValidStep3
        else -> false
    }
    val isLastStep: Boolean = currentStep == totalSteps
}
