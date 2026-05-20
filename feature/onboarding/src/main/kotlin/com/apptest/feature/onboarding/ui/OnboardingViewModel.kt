package com.apptest.feature.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptest.core.common.onFailure
import com.apptest.core.common.onSuccess
import com.apptest.core.domain.auth.AuthRepository
import com.apptest.feature.onboarding.domain.model.OnboardingDraft
import com.apptest.feature.onboarding.domain.model.OnboardingIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val authRepo: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    fun onIntentChange(intent: OnboardingIntent) =
        _state.update { it.copy(draft = it.draft.copy(intent = intent)) }

    fun onCategoryToggle(category: String) =
        _state.update { it.copy(draft = it.draft.copy(categories = it.draft.categories.toggle(category))) }

    fun onLanguageToggle(language: String) =
        _state.update { it.copy(draft = it.draft.copy(languages = it.draft.languages.toggle(language))) }

    fun onNext() {
        val s = _state.value
        if (!s.canProceed) return
        if (s.isLastStep) submit()
        else _state.update { it.copy(currentStep = it.currentStep + 1) }
    }

    fun onBack() {
        _state.update { if (it.currentStep > 1) it.copy(currentStep = it.currentStep - 1) else it }
    }

    fun onSkipAll() {
        // Skip = accept current defaults + submit
        submit()
    }

    private fun submit() {
        _state.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            // TODO(R-040+): also persist OnboardingDraft to ProfileRepository
            authRepo.markOnboardingComplete()
                .onSuccess { /* AuthState=Ready → MainActivity re-keys to MainRoot */ }
                .onFailure { err -> _state.update { it.copy(isSubmitting = false, submitError = err) } }
        }
    }

    private fun <T> Set<T>.toggle(item: T): Set<T> =
        if (contains(item)) this - item else this + item

    @Suppress("unused") private val _draftAlive: OnboardingDraft? = null
}
