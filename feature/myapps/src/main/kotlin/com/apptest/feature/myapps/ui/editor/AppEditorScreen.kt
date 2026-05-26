@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.apptest.feature.myapps.ui.editor

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
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
import com.apptest.feature.myapps.domain.model.MatchedTesterEmail
import com.apptest.feature.myapps.domain.model.PlayUrlValidation

@Composable
fun AppEditorScreen(
    state: AppEditorUiState,
    onField: ((com.apptest.feature.myapps.domain.model.AppDraft) -> com.apptest.feature.myapps.domain.model.AppDraft) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onRetryLoad: () -> Unit,
    onRequestDelete: () -> Unit = {},
    onCancelDelete: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    onRetryMatchedTesters: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val l = AppL10n.current
    if (state.showDeleteConfirm) {
        DeleteConfirmDialog(
            appName = state.draft.name.ifBlank { l.editor_field_name },
            onDismiss = onCancelDelete,
            onConfirm = onConfirmDelete,
        )
    }
    ScreenScaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = if (state.isEdit) l.editor_title_edit else l.editor_title_create,
                navIcon = {
                    // HIGH-011: disable while saving (matches BackHandler + Cancel button).
                    IconButton(onClick = onCancel, enabled = !state.isSaving && !state.isDeleting) {
                        AppIcon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = l.cta_back)
                    }
                },
                actions = {
                    // Delete button shown only in edit mode.
                    if (state.isEdit) {
                        IconButton(
                            onClick = onRequestDelete,
                            enabled = !state.isSaving && !state.isDeleting,
                        ) {
                            AppIcon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
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
                // Editable even in edit mode — package_name is just metadata, not a FK
                // target (matches reference app_id uuid). Typo-fix without delete+recreate.
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

            // Plan A (2026-05-26): show matched testers' emails so dev can paste them
            // into Play Console's closed-test allowlist. Only meaningful in edit mode —
            // a brand-new app has no matches yet.
            if (state.isEdit) {
                MatchedTestersSection(
                    testers = state.matchedTesters,
                    loading = state.matchedTestersLoading,
                    error = state.matchedTestersError?.message,
                    onRetry = onRetryMatchedTesters,
                )
            }
            if (state.saveError != null) {
                AppText(
                    text = l.editor_save_error_prefix + (state.saveError.message ?: l.err_unknown),
                    color = MaterialTheme.colorScheme.error,
                )
            }
            if (state.deleteError != null) {
                AppText(
                    text = "刪除失敗 / Delete failed: " + (state.deleteError.message ?: l.err_unknown),
                    color = MaterialTheme.colorScheme.error,
                )
            }
            EditorActions(
                canSave = state.canSave,
                isSaving = state.isSaving,
                isDeleting = state.isDeleting,
                onSave = onSave,
                onCancel = onCancel,
            )
        }
    }
}

/** Numeric input with inline string buffer; non-digit input stripped, out-of-range surfaced. */
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
private fun EditorActions(
    canSave: Boolean,
    isSaving: Boolean,
    isDeleting: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val l = AppL10n.current
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
    ) {
        AppButton(
            text = l.cta_cancel,
            onClick = onCancel,
            enabled = !isSaving && !isDeleting,
            variant = AppButtonVariant.Text,
        )
        AppButton(
            text = if (isSaving) l.cta_saving else l.cta_save,
            onClick = onSave,
            enabled = canSave,
            loading = isSaving,
            variant = AppButtonVariant.Primary,
        )
    }
}

/**
 * Plan A (2026-05-26): always-visible "Matched testers' emails" section in edit mode.
 *
 * Dev copies the list (one-per-line or comma-separated) into Play Console's
 * closed-testing allowlist. Replaces the previous Plan C Google-Group auto-sync
 * section, which required a 5-step service-account setup most devs skipped.
 *
 * Source: `MyAppsRepository.getMatchedTesterEmails(appId)` → SECURITY DEFINER RPC
 * `get_matched_tester_emails` (migration 002). RPC enforces caller == app.owner_id,
 * so this safely surfaces tester emails that profile RLS would otherwise mask.
 */
@Composable
private fun MatchedTestersSection(
    testers: List<MatchedTesterEmail>,
    loading: Boolean,
    error: String?,
    onRetry: () -> Unit,
) {
    val l = AppL10n.current
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    fun copy(joined: String, count: Int) {
        clipboard.setText(AnnotatedString(joined))
        Toast.makeText(
            context,
            l.editor_matched_testers_copied_fmt.format(count),
            Toast.LENGTH_SHORT,
        ).show()
    }

    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(AppSpacing.Md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
        ) {
            AppText(
                text = l.editor_matched_testers_title,
                style = MaterialTheme.typography.titleSmall,
            )
            AppText(
                text = l.editor_matched_testers_help,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            when {
                loading -> CircularProgressIndicator(modifier = Modifier.padding(AppSpacing.Sm))
                error != null -> {
                    AppText(
                        text = l.editor_matched_testers_error_prefix.format(error),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                    TextButton(onClick = onRetry) { AppText(l.editor_matched_testers_retry) }
                }
                testers.isEmpty() -> AppText(
                    text = l.editor_matched_testers_empty,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                else -> {
                    AppText(
                        text = l.editor_matched_testers_count_fmt.format(testers.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    testers.forEach { t ->
                        AppText(
                            text = "• ${t.email}  (${t.status})",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
                    ) {
                        AppButton(
                            text = l.editor_matched_testers_copy_lines,
                            onClick = { copy(testers.joinToString("\n") { it.email }, testers.size) },
                            variant = AppButtonVariant.Secondary,
                        )
                        AppButton(
                            text = l.editor_matched_testers_copy_commas,
                            onClick = { copy(testers.joinToString(", ") { it.email }, testers.size) },
                            variant = AppButtonVariant.Text,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmDialog(appName: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { AppText("刪除 \"$appName\"?") },
        text = {
            AppText(
                "此操作無法復原。app 從你的列表移除，配對中的測試會被取消。\n" +
                    "This action can't be undone. Tests already matched will be cancelled.",
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                AppText("刪除 / Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { AppText("取消 / Cancel") }
        },
    )
}
