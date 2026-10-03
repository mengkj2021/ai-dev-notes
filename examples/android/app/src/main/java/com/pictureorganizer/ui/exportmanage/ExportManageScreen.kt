package com.pictureorganizer.ui.exportmanage

import android.content.Intent
import android.text.format.DateFormat
import android.text.format.Formatter
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pictureorganizer.R
import com.pictureorganizer.ui.common.BackNavIconButton
import com.pictureorganizer.ui.common.LogScreenLifecycle
import com.pictureorganizer.ui.common.showSnackbarReplacing
import java.io.File
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportManageScreen(
    viewModel: ExportManageViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LogScreenLifecycle("ExportManage")
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.onEvent(ExportManageUiEvent.Refresh)
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ExportManageUiEffect.ShowMessage ->
                    snackbarHostState.showSnackbarReplacing(context.getString(effect.messageResId))
                is ExportManageUiEffect.ShareFile -> {
                    val file = File(effect.absolutePath)
                    val uri =
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file,
                        )
                    val intent =
                        Intent(Intent.ACTION_SEND).apply {
                            type = "application/zip"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_SUBJECT, effect.fileName)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    context.startActivity(
                        Intent.createChooser(
                            intent,
                            context.getString(R.string.export_zip_share),
                        ),
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.export_manage_title)) },
                navigationIcon = {
                    BackNavIconButton(
                        onBack = onBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                },
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading && state.files.isEmpty() -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            state.files.isEmpty() -> {
                Text(
                    text = stringResource(R.string.export_manage_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(innerPadding)
                            .padding(24.dp),
                )
            }
            else -> {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                ) {
                    items(state.files, key = { it.absolutePath }) { item ->
                        val sizeLabel = Formatter.formatShortFileSize(context, item.sizeBytes)
                        val timeLabel =
                            DateFormat
                                .getMediumDateFormat(context)
                                .format(Date(item.lastModifiedMillis))
                        ListItem(
                            headlineContent = { Text(item.fileName) },
                            supportingContent = {
                                Text(
                                    stringResource(
                                        R.string.export_manage_item_meta,
                                        sizeLabel,
                                        timeLabel,
                                    ),
                                )
                            },
                            trailingContent = {
                                Row {
                                    TextButton(
                                        onClick = {
                                            viewModel.onEvent(
                                                ExportManageUiEvent.Share(item.absolutePath),
                                            )
                                        },
                                        enabled = !state.isBusy,
                                    ) {
                                        Text(stringResource(R.string.export_zip_share))
                                    }
                                    TextButton(
                                        onClick = {
                                            viewModel.onEvent(
                                                ExportManageUiEvent.RequestRename(item.absolutePath),
                                            )
                                        },
                                        enabled = !state.isBusy,
                                    ) {
                                        Text(stringResource(R.string.action_rename))
                                    }
                                    TextButton(
                                        onClick = {
                                            viewModel.onEvent(
                                                ExportManageUiEvent.RequestDelete(item.absolutePath),
                                            )
                                        },
                                        enabled = !state.isBusy,
                                    ) {
                                        Text(stringResource(R.string.action_delete))
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    state.confirmDeletePath?.let { path ->
        val name = state.files.find { it.absolutePath == path }?.fileName ?: File(path).name
        AlertDialog(
            onDismissRequest = {
                if (!state.isBusy) {
                    viewModel.onEvent(ExportManageUiEvent.CancelDelete)
                }
            },
            title = { Text(stringResource(R.string.action_delete)) },
            text = {
                Text(stringResource(R.string.export_manage_confirm_delete, name))
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onEvent(ExportManageUiEvent.ConfirmDelete) },
                    enabled = !state.isBusy,
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onEvent(ExportManageUiEvent.CancelDelete) },
                    enabled = !state.isBusy,
                ) {
                    Text(stringResource(R.string.detail_tag_cancel))
                }
            },
        )
    }

    state.renamePath?.let {
        AlertDialog(
            onDismissRequest = {
                if (!state.isBusy) {
                    viewModel.onEvent(ExportManageUiEvent.CancelRename)
                }
            },
            title = { Text(stringResource(R.string.action_rename)) },
            text = {
                OutlinedTextField(
                    value = state.renameStemDraft,
                    onValueChange = {
                        viewModel.onEvent(ExportManageUiEvent.RenameDraftChanged(it))
                    },
                    singleLine = true,
                    enabled = !state.isBusy,
                    suffix = {
                        Text(
                            text = ".zip",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onEvent(ExportManageUiEvent.ConfirmRename) },
                    enabled = !state.isBusy,
                ) {
                    Text(stringResource(R.string.detail_rename_save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onEvent(ExportManageUiEvent.CancelRename) },
                    enabled = !state.isBusy,
                ) {
                    Text(stringResource(R.string.detail_tag_cancel))
                }
            },
        )
    }
}
