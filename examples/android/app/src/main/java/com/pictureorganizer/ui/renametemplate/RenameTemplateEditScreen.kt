package com.pictureorganizer.ui.renametemplate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pictureorganizer.R
import com.pictureorganizer.ui.common.BackNavIconButton
import com.pictureorganizer.ui.common.LogScreenLifecycle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RenameTemplateEditScreen(
    viewModel: RenameTemplateEditViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LogScreenLifecycle("RenameTemplateEdit")
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                RenameTemplateEditUiEffect.Saved -> onSaved()
                RenameTemplateEditUiEffect.NotFound -> onBack()
            }
        }
    }

    val title =
        if (state.editingId == null && !state.isLoading) {
            stringResource(R.string.rename_template_add)
        } else {
            stringResource(R.string.rename_template_edit)
        }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    BackNavIconButton(
                        onBack = onBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.onEvent(RenameTemplateEditUiEvent.Save) },
                        enabled = !state.isBusy && !state.isLoading,
                    ) {
                        Text(stringResource(R.string.detail_tag_save))
                    }
                },
            )
        },
    ) { innerPadding ->
        if (state.isLoading) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
        } else {
            var patternField by remember {
                mutableStateOf(
                    TextFieldValue(
                        text = state.pattern,
                        selection = TextRange(state.pattern.length),
                    ),
                )
            }
            LaunchedEffect(state.editingId, state.isLoading) {
                patternField =
                    TextFieldValue(
                        text = state.pattern,
                        selection = TextRange(state.pattern.length),
                    )
            }

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = {
                        viewModel.onEvent(RenameTemplateEditUiEvent.NameChanged(it))
                    },
                    label = { Text(stringResource(R.string.tag_manage_name_label)) },
                    singleLine = true,
                    isError = state.errorMessageResId != null,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = patternField,
                    onValueChange = { newValue ->
                        patternField = newValue
                        viewModel.onEvent(RenameTemplateEditUiEvent.PatternChanged(newValue.text))
                    },
                    label = { Text(stringResource(R.string.rename_template_pattern_label)) },
                    supportingText = {
                        Text(stringResource(R.string.rename_template_pattern_hint))
                    },
                    isError = state.errorMessageResId != null,
                    keyboardOptions =
                        KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(R.string.rename_template_insert_rules),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    RenamePatternInsertTokens.forEach { token ->
                        AssistChip(
                            onClick = {
                                val inserted =
                                    insertAtSelection(
                                        text = patternField.text,
                                        selectionStart = patternField.selection.start,
                                        selectionEnd = patternField.selection.end,
                                        token = token,
                                    )
                                patternField =
                                    TextFieldValue(
                                        text = inserted.text,
                                        selection = TextRange(inserted.cursor),
                                    )
                                viewModel.onEvent(
                                    RenameTemplateEditUiEvent.PatternChanged(inserted.text),
                                )
                            },
                            label = { Text(token) },
                        )
                    }
                }
                state.errorMessageResId?.let { resId ->
                    Text(
                        text = stringResource(resId),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = state.isDefault,
                        onCheckedChange = {
                            viewModel.onEvent(RenameTemplateEditUiEvent.DefaultChanged(it))
                        },
                    )
                    Text(stringResource(R.string.tag_manage_as_default))
                }
            }
        }
    }
}
