package com.apptest.core.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import com.apptest.core.designsystem.components.AppText

/**
 * Search input organism per `_specs/compose_components.md §4 AppSearchBar`.
 *
 * V1 uses styled OutlinedTextField (simpler API than M3 SearchBar's expandable pattern).
 * Migrate to M3 `SearchBar` if we need the expandable history dropdown later.
 *
 * Pressing IME search key invokes [onSubmit]; live edits invoke [onQueryChange] per keystroke.
 */
@Composable
fun AppSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailingActions: @Composable RowScope.() -> Unit = {},
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            AppText(placeholder, style = MaterialTheme.typography.bodyLarge)
        },
        trailingIcon = { Row(content = trailingActions) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
    )
}
