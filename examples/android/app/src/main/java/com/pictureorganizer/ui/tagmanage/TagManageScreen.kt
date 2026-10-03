package com.pictureorganizer.ui.tagmanage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pictureorganizer.PictureOrganizerApplication
import com.pictureorganizer.R
import com.pictureorganizer.model.Tag
import com.pictureorganizer.model.TagTemplate
import com.pictureorganizer.ui.common.BackNavIconButton
import com.pictureorganizer.ui.common.FabOverlayListContentPadding
import com.pictureorganizer.ui.common.LogScreenLifecycle
import com.pictureorganizer.ui.common.showSnackbarReplacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagManageScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LogScreenLifecycle("TagManage")
    val context = LocalContext.current
    val app = context.applicationContext as PictureOrganizerApplication
    val viewModel: TagManageViewModel =
        viewModel(
            factory =
                TagManageViewModel.Factory(
                    app.tagRepository,
                    app.imageRepository,
                    app.userPreferencesRepository,
                ),
        )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TagManageUiEffect.ShowMessage -> {
                    val text =
                        if (effect.formatArgs.isEmpty()) {
                            context.getString(effect.messageResId)
                        } else {
                            context.getString(
                                effect.messageResId,
                                *effect.formatArgs.toTypedArray(),
                            )
                        }
                    snackbarHostState.showSnackbarReplacing(text)
                }
                is TagManageUiEffect.ShowMessageText -> {
                    snackbarHostState.showSnackbarReplacing(effect.text)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tag_manage_title)) },
                navigationIcon = {
                    BackNavIconButton(
                        onBack = onBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (state.selectedTab) {
                        TagManageTab.Tags -> viewModel.onEvent(TagManageUiEvent.OpenAddTag)
                        TagManageTab.Templates -> viewModel.onEvent(TagManageUiEvent.OpenAddTemplate)
                    }
                },
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.action_add))
            }
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            PrimaryTabRow(selectedTabIndex = state.selectedTab.ordinal) {
                Tab(
                    selected = state.selectedTab == TagManageTab.Tags,
                    onClick = { viewModel.onEvent(TagManageUiEvent.SelectTab(TagManageTab.Tags)) },
                    text = { Text(stringResource(R.string.tag_manage_tab_tags)) },
                )
                Tab(
                    selected = state.selectedTab == TagManageTab.Templates,
                    onClick = {
                        viewModel.onEvent(TagManageUiEvent.SelectTab(TagManageTab.Templates))
                    },
                    text = { Text(stringResource(R.string.tag_manage_tab_templates)) },
                )
            }
            when (state.selectedTab) {
                TagManageTab.Tags ->
                    TagsList(
                        tags = state.tags,
                        onEdit = { viewModel.onEvent(TagManageUiEvent.OpenEditTag(it)) },
                        onDelete = { viewModel.onEvent(TagManageUiEvent.RequestDeleteTag(it.id)) },
                    )
                TagManageTab.Templates ->
                    TemplatesList(
                        templates = state.templates,
                        onEdit = { viewModel.onEvent(TagManageUiEvent.OpenEditTemplate(it)) },
                        onDelete = { viewModel.onEvent(TagManageUiEvent.RequestDeleteTemplate(it.id)) },
                        onSetDefault = { viewModel.onEvent(TagManageUiEvent.SetDefaultTemplate(it.id)) },
                    )
            }
        }
    }

    state.tagDialog?.let { dialog ->
        TagEditDialog(
            dialog = dialog,
            isBusy = state.isBusy,
            onNameChanged = { viewModel.onEvent(TagManageUiEvent.TagNameChanged(it)) },
            onSave = { viewModel.onEvent(TagManageUiEvent.SaveTag) },
            onDismiss = { viewModel.onEvent(TagManageUiEvent.DismissTagDialog) },
        )
    }

    state.templateDialog?.let { dialog ->
        TemplateEditDialog(
            dialog = dialog,
            availableTags = state.tags,
            isBusy = state.isBusy,
            onNameChanged = { viewModel.onEvent(TagManageUiEvent.TemplateNameChanged(it)) },
            onToggleTag = { viewModel.onEvent(TagManageUiEvent.ToggleTemplateTag(it)) },
            onDefaultChanged = { viewModel.onEvent(TagManageUiEvent.TemplateDefaultChanged(it)) },
            onSave = { viewModel.onEvent(TagManageUiEvent.SaveTemplate) },
            onDismiss = { viewModel.onEvent(TagManageUiEvent.DismissTemplateDialog) },
        )
    }

    state.confirmDeleteTagId?.let {
        ConfirmActionDialog(
            titleResId = R.string.action_delete,
            message = stringResource(R.string.tag_manage_confirm_delete_tag),
            confirmTextResId = R.string.action_delete,
            onConfirm = { viewModel.onEvent(TagManageUiEvent.ConfirmDeleteTag) },
            onDismiss = { viewModel.onEvent(TagManageUiEvent.CancelDeleteTag) },
        )
    }

    state.cascadeDeleteTag?.let { cascade ->
        ConfirmActionDialog(
            titleResId = R.string.action_delete,
            message =
                stringResource(
                    R.string.tag_manage_confirm_delete_tag_in_use,
                    cascade.imageCount,
                ),
            confirmTextResId = R.string.action_delete,
            onConfirm = { viewModel.onEvent(TagManageUiEvent.ConfirmCascadeDeleteTag) },
            onDismiss = { viewModel.onEvent(TagManageUiEvent.CancelCascadeDeleteTag) },
        )
    }

    state.cascadeRenameTag?.let { cascade ->
        ConfirmActionDialog(
            titleResId = R.string.action_rename,
            message =
                stringResource(
                    R.string.tag_manage_confirm_rename_tag_in_use,
                    cascade.imageCount,
                    cascade.newName,
                ),
            confirmTextResId = R.string.action_rename,
            onConfirm = { viewModel.onEvent(TagManageUiEvent.ConfirmCascadeRenameTag) },
            onDismiss = { viewModel.onEvent(TagManageUiEvent.CancelCascadeRenameTag) },
        )
    }

    state.confirmDeleteTemplateId?.let {
        ConfirmActionDialog(
            titleResId = R.string.action_delete,
            message = stringResource(R.string.tag_manage_confirm_delete_template),
            confirmTextResId = R.string.action_delete,
            onConfirm = { viewModel.onEvent(TagManageUiEvent.ConfirmDeleteTemplate) },
            onDismiss = { viewModel.onEvent(TagManageUiEvent.CancelDeleteTemplate) },
        )
    }
}

@Composable
private fun TagsList(
    tags: List<Tag>,
    onEdit: (Tag) -> Unit,
    onDelete: (Tag) -> Unit,
) {
    if (tags.isEmpty()) {
        Text(
            text = stringResource(R.string.tag_manage_empty_tags),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = FabOverlayListContentPadding,
    ) {
        items(tags, key = { it.id }) { tag ->
            ListItem(
                headlineContent = { Text(tag.name) },
                trailingContent = {
                    Row {
                        IconButton(onClick = { onEdit(tag) }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit))
                        }
                        IconButton(onClick = { onDelete(tag) }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete))
                        }
                    }
                },
                modifier = Modifier.clickable { onEdit(tag) },
            )
        }
    }
}

@Composable
private fun TemplatesList(
    templates: List<TagTemplate>,
    onEdit: (TagTemplate) -> Unit,
    onDelete: (TagTemplate) -> Unit,
    onSetDefault: (TagTemplate) -> Unit,
) {
    if (templates.isEmpty()) {
        Text(
            text = stringResource(R.string.tag_manage_empty_templates),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = FabOverlayListContentPadding,
    ) {
        items(templates, key = { it.id }) { template ->
            ListItem(
                headlineContent = { Text(template.name) },
                supportingContent = {
                    val tagsText =
                        if (template.tagNames.isEmpty()) {
                            stringResource(R.string.tag_manage_template_no_tags)
                        } else {
                            template.tagNames.joinToString(" · ")
                        }
                    Text(
                        if (template.isDefault) {
                            stringResource(R.string.tag_manage_default_badge) + " · " + tagsText
                        } else {
                            tagsText
                        },
                    )
                },
                trailingContent = {
                    Row {
                        if (!template.isDefault) {
                            IconButton(onClick = { onSetDefault(template) }) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = stringResource(R.string.tag_manage_set_default),
                                )
                            }
                        }
                        IconButton(onClick = { onEdit(template) }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit))
                        }
                        IconButton(onClick = { onDelete(template) }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete))
                        }
                    }
                },
                modifier = Modifier.clickable { onEdit(template) },
            )
        }
    }
}

@Composable
private fun TagEditDialog(
    dialog: TagDialogState,
    isBusy: Boolean,
    onNameChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val title =
        if (dialog.editingId == null) {
            stringResource(R.string.tag_manage_add_tag)
        } else {
            stringResource(R.string.tag_manage_edit_tag)
        }
    AlertDialog(
        onDismissRequest = { if (!isBusy) onDismiss() },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = dialog.name,
                    onValueChange = onNameChanged,
                    label = { Text(stringResource(R.string.tag_manage_name_label)) },
                    singleLine = true,
                    enabled = !isBusy,
                    isError = dialog.errorMessageResId != null,
                    modifier = Modifier.fillMaxWidth(),
                )
                dialog.errorMessageResId?.let { resId ->
                    Text(
                        text = stringResource(resId),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = !isBusy) {
                Text(stringResource(R.string.detail_tag_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isBusy) {
                Text(stringResource(R.string.detail_tag_cancel))
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TemplateEditDialog(
    dialog: TemplateDialogState,
    availableTags: List<Tag>,
    isBusy: Boolean,
    onNameChanged: (String) -> Unit,
    onToggleTag: (String) -> Unit,
    onDefaultChanged: (Boolean) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val title =
        if (dialog.editingId == null) {
            stringResource(R.string.tag_manage_add_template)
        } else {
            stringResource(R.string.tag_manage_edit_template)
        }
    AlertDialog(
        onDismissRequest = { if (!isBusy) onDismiss() },
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = dialog.name,
                    onValueChange = onNameChanged,
                    label = { Text(stringResource(R.string.tag_manage_name_label)) },
                    singleLine = true,
                    enabled = !isBusy,
                    isError = dialog.errorMessageResId != null,
                    modifier = Modifier.fillMaxWidth(),
                )
                dialog.errorMessageResId?.let { resId ->
                    Text(
                        text = stringResource(resId),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                ListItem(
                    headlineContent = { Text(stringResource(R.string.tag_manage_as_default)) },
                    trailingContent = {
                        Checkbox(
                            checked = dialog.isDefault,
                            onCheckedChange = onDefaultChanged,
                            enabled = !isBusy,
                        )
                    },
                )
                Text(
                    text = stringResource(R.string.tag_manage_pick_tags),
                    style = MaterialTheme.typography.labelLarge,
                )
                if (availableTags.isEmpty()) {
                    Text(
                        text = stringResource(R.string.tag_manage_empty_tags),
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        availableTags.forEach { tag ->
                            FilterChip(
                                selected = tag.name in dialog.selectedTagNames,
                                onClick = { onToggleTag(tag.name) },
                                enabled = !isBusy,
                                label = { Text(tag.name) },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = !isBusy) {
                Text(stringResource(R.string.detail_tag_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isBusy) {
                Text(stringResource(R.string.detail_tag_cancel))
            }
        },
    )
}

@Composable
private fun ConfirmActionDialog(
    titleResId: Int,
    message: String,
    confirmTextResId: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleResId)) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(confirmTextResId))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.detail_tag_cancel))
            }
        },
    )
}
