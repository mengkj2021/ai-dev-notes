package com.pictureorganizer.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pictureorganizer.PictureOrganizerApplication
import com.pictureorganizer.R
import com.pictureorganizer.ui.common.BackNavIconButton
import com.pictureorganizer.ui.common.LogScreenLifecycle
import com.pictureorganizer.util.list.ListPaging

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToTagManage: () -> Unit,
    onNavigateToRenameTemplates: () -> Unit,
    onNavigateToDefaultTags: () -> Unit,
    onNavigateToTutorial: () -> Unit,
    onNavigateToOssLicenses: () -> Unit,
    onNavigateToExportManage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LogScreenLifecycle("Settings")
    val context = LocalContext.current
    val app = context.applicationContext as PictureOrganizerApplication
    val viewModel: SettingsViewModel =
        viewModel(factory = SettingsViewModel.Factory(app.userPreferencesRepository))
    val askDuplicate by viewModel.importDuplicateAskEnabled.collectAsStateWithLifecycle()
    val listPaging by viewModel.listPagingEnabled.collectAsStateWithLifecycle()
    val listPageSize by viewModel.listPageSize.collectAsStateWithLifecycle()
    var showPageSizeDialog by remember { mutableStateOf(false) }
    val versionName =
        remember(context) {
            runCatching {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            }.getOrNull().orEmpty().ifEmpty { "—" }
        }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    BackNavIconButton(
                        onBack = onBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            item {
                SettingsRow(
                    title = stringResource(R.string.settings_tag_manage),
                    desc = stringResource(R.string.settings_tag_manage_desc),
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    onClick = onNavigateToTagManage,
                )
            }
            item {
                SettingsRow(
                    title = stringResource(R.string.settings_rename_templates),
                    desc = stringResource(R.string.settings_rename_templates_desc),
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    onClick = onNavigateToRenameTemplates,
                )
            }
            item {
                SettingsRow(
                    title = stringResource(R.string.settings_default_tags),
                    desc = stringResource(R.string.settings_default_tags_desc),
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    onClick = onNavigateToDefaultTags,
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_import_duplicate_ask)) },
                    supportingContent = {
                        Text(stringResource(R.string.settings_import_duplicate_ask_desc))
                    },
                    leadingContent = {
                        Icon(Icons.Default.Warning, contentDescription = null)
                    },
                    trailingContent = {
                        Switch(
                            checked = askDuplicate,
                            onCheckedChange = viewModel::setImportDuplicateAskEnabled,
                        )
                    },
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_list_paging)) },
                    supportingContent = {
                        Text(stringResource(R.string.settings_list_paging_desc))
                    },
                    leadingContent = {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                    },
                    trailingContent = {
                        Switch(
                            checked = listPaging,
                            onCheckedChange = viewModel::setListPagingEnabled,
                        )
                    },
                )
            }
            item {
                val pageSizeColors =
                    if (listPaging) {
                        ListItemDefaults.colors()
                    } else {
                        ListItemDefaults.colors(
                            headlineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            supportingColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                            leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                        )
                    }
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_list_page_size)) },
                    supportingContent = {
                        Text(stringResource(R.string.settings_list_page_size_desc, listPageSize))
                    },
                    leadingContent = {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                    },
                    trailingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                        )
                    },
                    colors = pageSizeColors,
                    modifier =
                        Modifier.clickable(
                            enabled = listPaging,
                            onClick = { showPageSizeDialog = true },
                        ),
                )
            }
            item {
                SettingsRow(
                    title = stringResource(R.string.settings_export_manage),
                    desc = stringResource(R.string.settings_export_manage_desc),
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    onClick = onNavigateToExportManage,
                )
            }
            item {
                SettingsRow(
                    title = stringResource(R.string.settings_tutorial),
                    desc = stringResource(R.string.settings_tutorial_desc),
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    onClick = onNavigateToTutorial,
                )
            }
            item {
                SettingsRow(
                    title = stringResource(R.string.settings_oss_licenses),
                    desc = stringResource(R.string.settings_oss_licenses_desc),
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    onClick = onNavigateToOssLicenses,
                )
            }
            item {
                Text(
                    text = stringResource(R.string.settings_version, versionName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                )
            }
        }
    }

    if (showPageSizeDialog) {
        ListPageSizeDialog(
            currentPageSize = listPageSize,
            onDismiss = { showPageSizeDialog = false },
            onSelectPreset = { size ->
                viewModel.setListPageSize(size)
                showPageSizeDialog = false
            },
            onConfirmCustom = { size ->
                viewModel.setListPageSize(size)
                showPageSizeDialog = false
            },
        )
    }
}

@Composable
private fun ListPageSizeDialog(
    currentPageSize: Int,
    onDismiss: () -> Unit,
    onSelectPreset: (Int) -> Unit,
    onConfirmCustom: (Int) -> Unit,
) {
    val isPreset = currentPageSize in ListPaging.PRESET_PAGE_SIZES
    var customSelected by remember { mutableStateOf(!isPreset) }
    var customInput by remember {
        mutableStateOf(if (!isPreset) currentPageSize.toString() else "")
    }
    var showInvalid by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_list_page_size_dialog_title)) },
        text = {
            Column {
                ListPaging.PRESET_PAGE_SIZES.forEach { size ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = !customSelected && size == currentPageSize,
                                    onClick = { onSelectPreset(size) },
                                    role = Role.RadioButton,
                                ).padding(vertical = 8.dp),
                    ) {
                        RadioButton(
                            selected = !customSelected && size == currentPageSize,
                            onClick = null,
                        )
                        Text(
                            text = size.toString(),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = customSelected,
                                onClick = {
                                    customSelected = true
                                    showInvalid = false
                                    if (customInput.isEmpty() && !isPreset) {
                                        customInput = currentPageSize.toString()
                                    }
                                },
                                role = Role.RadioButton,
                            ).padding(vertical = 8.dp),
                ) {
                    RadioButton(
                        selected = customSelected,
                        onClick = null,
                    )
                    Text(
                        text = stringResource(R.string.settings_list_page_size_custom),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                if (customSelected) {
                    OutlinedTextField(
                        value = customInput,
                        onValueChange = {
                            customInput = it.filter { ch -> ch.isDigit() }.take(3)
                            showInvalid = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = {
                            Text(stringResource(R.string.settings_list_page_size_custom_hint))
                        },
                        isError = showInvalid,
                        supportingText =
                            if (showInvalid) {
                                {
                                    Text(stringResource(R.string.settings_list_page_size_custom_invalid))
                                }
                            } else {
                                null
                            },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
            }
        },
        confirmButton = {
            if (customSelected) {
                TextButton(
                    onClick = {
                        val parsed = customInput.toIntOrNull()
                        if (parsed != null && ListPaging.isValidPageSize(parsed)) {
                            onConfirmCustom(parsed)
                        } else {
                            showInvalid = true
                        }
                    },
                ) {
                    Text(stringResource(R.string.filter_date_ok))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.filter_date_cancel))
            }
        },
    )
}

@Composable
private fun SettingsRow(
    title: String,
    desc: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(desc) },
        leadingContent = icon,
        trailingContent = {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
            )
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}
