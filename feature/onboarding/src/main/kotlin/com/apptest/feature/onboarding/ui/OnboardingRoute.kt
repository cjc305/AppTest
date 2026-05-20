package com.apptest.feature.onboarding.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OnboardingRoute(viewModel: OnboardingViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    OnboardingScreen(
        state = state,
        onIntentChange = viewModel::onIntentChange,
        onCategoryToggle = viewModel::onCategoryToggle,
        onLanguageToggle = viewModel::onLanguageToggle,
        onNext = viewModel::onNext,
        onBack = viewModel::onBack,
        onSkip = viewModel::onSkipAll,
    )
}
