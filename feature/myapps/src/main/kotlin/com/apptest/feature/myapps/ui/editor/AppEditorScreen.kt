@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.apptest.feature.myapps.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.apptest.core.designsystem.components.AppIcon
import com.apptest.core.designsystem.components.AppText
import com.apptest.core.designsystem.spacing.AppSpacing
import com.apptest.core.designsystem.theme.AppL10n
import com.apptest.core.ui.components.AppButton
import com.apptest.core.ui.components.AppButtonVariant
import com.apptest.core.ui.components.AppErrorState
import com.apptest.core.ui.components.AppLoadingState
import com.apptest.core.ui.components.AppTopBar
import com.apptest.core.ui.templates.ScreenScaffold
import com.apptest.feature.myapps.domain.model.PlayUrlValidation

@Composable
fun AppEditorScreen(
    state: AppEditorUiState,
    onField: ((com.apptest.feature.myapps.domain.model.AppDraft) -> com.apptest.feature.myapps.domain.model.AppDraft) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onRetryLoad: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val l = AppL10n.current
    ScreenScaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = if (state.isEdit) l.editor_title_edit else l.editor_title_create,
                navIcon = {
                    IconButton(onClick = onCancel) {
                        AppIcon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = l.cta_back,
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            AppLoadingState(modifier = Modifier.padding(padding))
            return@ScreenScaffold
        }
        if (state.loadError != null) {
            AppErrorState(
                error = state.loadError,
                onRetry = onRetryLoad,
                modifier = Modifier.padding(padding),
            )
            return@ScreenScaffold
        }
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.Md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.Md),
        ) {
            OutlinedTextField(
                value = state.draft.packageName,
                onValueChange = { v -> onField { it.copy(packageName = v) } },
                label = { AppText(l.editor_field_package) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isEdit,
            )
            OutlinedTextField(
                value = state.draft.name,
                onValueChange = { v -> onField { it.copy(name = v) } },
                label = { AppText(l.editor_field_name) },
                singleLine = true,
                isError = state.draft.name.isNotEmpty() && state.draft.name.length !in 2..50,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.draft.description,
                onValueChange = { v -> onField { it.copy(description = v) } },
                label = { AppText(l.editor_field_description) },
                supportingText = { AppText(l.editor_field_desc_counter.format(state.draft.description.length, 500)) },
                isError = state.draft.description.length > 500,
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.draft.playOptInUrl,
                onValueChange = { v -> onField { it.copy(playOptInUrl = v) } },
                label = { AppText(l.editor_field_play_url) },
                isError = state.urlValidation is PlayUrlValidation.Invalid,
                supportingText = {
                    when (val v = state.urlValidation) {
                        PlayUrlValidation.Empty -> AppText(l.editor_field_play_url_help_empty)
                        PlayUrlValidation.Valid -> AppText(l.editor_field_play_url_help_valid, color = MaterialTheme.colorScheme.primary)
                        is PlayUrlValidation.Invalid -> AppText(l.editor_field_play_url_help_invalid.format(v.reason), color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            NumberField(
                value = state.draft.requiredTesters,
                onChange = { v -> onField { d -> d.copy(requiredTesters = v) } },
                label = l.editor_field_required_testers,
                range = 1..100,
            )
            NumberField(
                value = state.draft.requiredDays,
                onChange = { v -> onField { d -> d.copy(requiredDays = v) } },
                label = l.editor_field_required_days,
                range = 7..30,
            )
            AppText(
                text = l.editor_cost_label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (state.saveError != null) {
                AppText(
                    text = l.editor_save_error_prefix + (state.saveError.message ?: l.err_unknown),
                    color = MaterialTheme.colorScheme.error,
                )
            }
            EditorActions(
                canSave = state.canSave,
                isSaving = state.isSaving,
                onSave = onSave,
                onCancel = onCancel,
            )
        }
    }
}

/**
 * Numeric input with an inline string buffer so the user can clear the field, type "0"-prefixed
 * values, or paste non-digit chars without silently dropping their keystrokes. Length is capped
 * at the digits needed for the range's upper bound; non-digit chars are stripped on input.
 * The model receives a parsed Int only when it falls inside [range]; out-of-range or unparseable
 * input is surfaced via [isError] + supportingText rather than swallowed.
 */
@Composable
private fun NumberField(value: Int, onChange: (Int) -> Unit, label: String, range: IntRange) {
    val maxLen = range.last.toString().length
    var raw by rememberSaveable(value) { mutableStateOf(value.toString()) }
    val parsed = raw.toIntOrNull()
    val outOfRange = parsed == null || parsed !in range
    OutlinedTextField(
        value = raw,
        onValueChange = { input ->
            raw = input.filter(Char::isDigit).take(maxLen)
            raw.toIntOrNull()?.takeIf { it in range }?.let(onChange)
        },
        label = { AppText(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = raw.isNotEmpty() && outOfRange,
        supportingText = {
            if (raw.isNotEmpty() && outOfRange) {
                Text("${range.first} – ${range.last}")
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun EditorActions(canSave: Boolean, isSaving: Boolean, onSave: () -> Unit, onCancel: () -> Unit) {
    val l = AppL10n.current
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
    ) {
        AppButton(text = l.cta_cancel, onClick = onCancel, variant = AppButtonVariant.Text)
        AppButton(
            text = if (isSaving) l.cta_saving else l.cta_save,
            onClick = onSave,
            enabled = canSave,
            loading = isSaving,
            variant = AppButtonVariant.Primary,
        )
    }
}
