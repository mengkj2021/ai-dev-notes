package com.pictureorganizer.ui.renametemplate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pictureorganizer.R
import com.pictureorganizer.ui.common.BackNavIconButton
import com.pictureorganizer.ui.common.FabOverlayListContentPadding
import com.pictureorganizer.ui.common.LogScreenLifecycle
import com.pictureorganizer.ui.common.showSnackbarReplacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameTemplateManageScreen(
    viewModel: RenameTemplateManageViewModel,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (templateId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LogScreenLifecycle("RenameTemplateManage")
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is RenameTemplateManageUiEffect.ShowMessage ->
                    snackbarHostState.showSnackbarReplacing(context.getString(effect.messageResId))
                is RenameTemplateManageUiEffect.ShowMessageText ->
                    snackbarHostState.showSnackbarReplacing(effect.text)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.rename_template_manage_title)) },
                navigationIcon = {
                    BackNavIconButton(
                        onBack = onBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.action_add))
            }
        },
    ) { innerPadding ->
        if (state.templates.isEmpty()) {
            Text(
                text = stringResource(R.string.rename_template_empty),
                modifier =
                    Modifier
                        .padding(innerPadding)
                        .padding(24.dp),
            )
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                contentPadding = FabOverlayListContentPadding,
            ) {
                items(state.templates, key = { it.id }) { template ->
                    ListItem(
                        headlineContent = { Text(template.name) },
                        supportingContent = {
                            Text(
                                buildString {
                                    append(template.pattern)
                                    if (template.isDefault) {
                                        append(" · ")
                                        append(context.getString(R.string.tag_manage_default_badge))
                                    }
                                },
                            )
                        },
                        trailingContent = {
                            Row {
                                if (!template.isDefault) {
                                    IconButton(
                                        onClick = {
                                            viewModel.onEvent(
                                                RenameTemplateManageUiEvent.SetDefault(template.id),
                                            )
                                        },
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null)
                                    }
                                }
                                IconButton(onClick = { onEdit(template.id) }) {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.onEvent(
                                            RenameTemplateManageUiEvent.RequestDelete(template.id),
                                        )
                                    },
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                }
                            }
                        },
                        modifier = Modifier.clickable { onEdit(template.id) },
                    )
                }
            }
        }
    }

    state.confirmDeleteId?.let {
        AlertDialog(
            onDismissRequest = {
                viewModel.onEvent(RenameTemplateManageUiEvent.CancelDelete)
            },
            title = { Text(stringResource(R.string.action_delete)) },
            text = { Text(stringResource(R.string.rename_template_confirm_delete)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onEvent(RenameTemplateManageUiEvent.ConfirmDelete)
                }) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.onEvent(RenameTemplateManageUiEvent.CancelDelete)
                }) {
                    Text(stringResource(R.string.detail_tag_cancel))
                }
            },
        )
    }
}
