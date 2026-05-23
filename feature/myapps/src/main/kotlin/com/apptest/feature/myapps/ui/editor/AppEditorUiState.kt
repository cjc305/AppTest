package com.apptest.feature.myapps.ui.editor

import androidx.compose.runtime.Immutable
import com.apptest.core.common.AppError
import com.apptest.feature.myapps.domain.model.AppDraft
import com.apptest.feature.myapps.domain.model.PlayUrlValidation

/**
 * Editor state is single-shape with mode flags rather than sealed, because the form is
 * always-visible; we just toggle "saving" / "error banner" / "saved → navigate up".
 */
@Immutable
data class AppEditorUiState(
    val draft: AppDraft = AppDraft(),
    val isEdit: Boolean = false,           // false = create
    val isLoading: Boolean = false,        // true while initial load (edit mode only)
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val urlValidation: PlayUrlValidation = PlayUrlValidation.Empty,
    val canSave: Boolean = false,
    val savedId: String? = null,           // non-null = navigate up after save
    val deletedId: String? = null,         // non-null = navigate up after delete
    val showDeleteConfirm: Boolean = false,
    val loadError: AppError? = null,
    val saveError: AppError? = null,
    val deleteError: AppError? = null,
)
