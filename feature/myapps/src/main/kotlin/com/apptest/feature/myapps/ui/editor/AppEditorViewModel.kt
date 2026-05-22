package com.apptest.feature.myapps.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.apptest.core.common.AppError
import com.apptest.core.common.onFailure
import com.apptest.core.common.onSuccess
import com.apptest.core.navigation.AppDestination
import com.apptest.feature.myapps.data.MyAppsRepository
import com.apptest.feature.myapps.domain.PlayOptInUrlValidator
import com.apptest.feature.myapps.domain.model.AppDraft
import com.apptest.feature.myapps.domain.model.PlayUrlValidation
import com.apptest.feature.myapps.domain.usecase.SaveAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AppEditorViewModel @Inject constructor(
    private val repo: MyAppsRepository,
    private val saveApp: SaveAppUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(AppEditorUiState())
    val state: StateFlow<AppEditorUiState> = _state.asStateFlow()

    private val editingAppId: String? =
        savedStateHandle.toRoute<AppDestination.AppEditor>().appId

    init {
        if (editingAppId == null) {
            _state.update { it.copy(isEdit = false) }
        } else {
            _state.update { it.copy(isEdit = true, isLoading = true) }
            viewModelScope.launch { loadForEdit(editingAppId) }
        }
    }

    /** Retry hook for the editor screen — re-attempts load after a transient failure. */
    fun retryLoad() {
        val id = editingAppId ?: return
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, loadError = null) }
        viewModelScope.launch { loadForEdit(id) }
    }

    private suspend fun loadForEdit(id: String) {
        val row = try {
            repo.get(id)
        } catch (t: Throwable) {
            _state.update { it.copy(isLoading = false, loadError = AppError.fromThrowable(t)) }
            return
        }
        if (row == null) {
            _state.update { it.copy(isLoading = false, loadError = AppError.NotFound("App")) }
            return
        }
        val draft = AppDraft(
            id = row.id, name = row.name, packageName = row.packageName,
            requiredTesters = row.requiredTesters, requiredDays = row.requiredDays,
        )
        _state.update { it.copy(draft = draft, isLoading = false, loadError = null).recomputed() }
    }

    fun onField(update: (AppDraft) -> AppDraft) {
        _state.update { it.copy(draft = update(it.draft), saveError = null).recomputed() }
    }

    fun save() {
        val current = _state.value
        if (current.isSaving || !current.canSave) return
        val draft = current.draft
        _state.update { it.copy(isSaving = true, saveError = null).recomputed() }
        viewModelScope.launch {
            saveApp(draft)
                .onSuccess { id ->
                    _state.update { it.copy(isSaving = false, savedId = id).recomputed() }
                }
                .onFailure { err ->
                    _state.update { it.copy(isSaving = false, saveError = err).recomputed() }
                }
        }
    }

    private fun AppEditorUiState.recomputed(): AppEditorUiState {
        val urlV = PlayOptInUrlValidator.validate(draft.playOptInUrl)
        val basicOk = draft.name.length in 2..50 &&
            draft.packageName.isNotBlank() &&
            draft.description.length <= 500 &&
            draft.requiredTesters in 1..100 &&
            draft.requiredDays in 7..30
        val canSave = basicOk && urlV == PlayUrlValidation.Valid && !isSaving && !isLoading
        return copy(urlValidation = urlV, canSave = canSave)
    }
}
