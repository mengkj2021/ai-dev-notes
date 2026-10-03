package com.pictureorganizer.ui.importimages

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pictureorganizer.PictureOrganizerApplication
import com.pictureorganizer.R
import com.pictureorganizer.ui.common.LogScreenLifecycle
import com.pictureorganizer.ui.common.showSnackbarReplacing
import com.pictureorganizer.util.image.ImageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    onBack: (onPopFailed: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    LogScreenLifecycle("Import")
    val context = LocalContext.current
    val app = context.applicationContext as PictureOrganizerApplication
    val viewModel: ImportViewModel =
        viewModel(
            factory =
                ImportViewModel.Factory(
                    repository = app.imageRepository,
                    imageManager = ImageManager(context, app.fileManager),
                    userPreferences = app.userPreferencesRepository,
                    tagRepository = app.tagRepository,
                    fileManager = app.fileManager,
                ),
        )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ImportUiEffect.ImportCompleted -> {
                    snackbarHostState.showSnackbarReplacing(
                        message =
                            context.getString(
                                R.string.import_completed,
                                effect.successCount,
                            ),
                    )
                }
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    var lifecycleState by remember(lifecycleOwner) {
        mutableStateOf(lifecycleOwner.lifecycle.currentState)
    }
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, _ ->
                lifecycleState = lifecycleOwner.lifecycle.currentState
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val isResumed = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)
    val iconBackEnabled = !state.isImporting && isResumed

    var closing by remember { mutableStateOf(false) }

    fun requestBack() {
        if (state.isImporting || closing || !isResumed) return
        closing = true
        onBack { closing = false }
    }

    // 导入中必须始终启用并吞掉；enabled=false 会把事件交回 Navigation（F10 / 错题本）
    BackHandler {
        if (state.isImporting) return@BackHandler
        requestBack()
    }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia(),
        ) { uris: List<Uri> ->
            viewModel.importUris(uris)
        }

    val filesLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenMultipleDocuments(),
        ) { uris: List<Uri> ->
            viewModel.importUris(uris)
        }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.import_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = { requestBack() },
                        enabled = iconBackEnabled,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.import_duplicate_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.import_compress_switch),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = state.compressEnabled,
                        onCheckedChange = viewModel::setCompressEnabled,
                        enabled = !state.isImporting,
                    )
                }
                Text(
                    text = stringResource(R.string.import_compress_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 24.dp),
                )
                Button(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly,
                            ),
                        )
                    },
                    enabled = !state.isImporting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.import_from_gallery))
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        filesLauncher.launch(arrayOf("image/*"))
                    },
                    enabled = !state.isImporting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.import_from_files))
                }
            }

            if (state.isImporting) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text =
                                stringResource(
                                    R.string.import_progress,
                                    state.current,
                                    state.total,
                                ),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        }
    }

    val duplicatePrompt = state.duplicatePrompt
    if (duplicatePrompt != null) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(R.string.import_duplicate_title)) },
            text = {
                Column {
                    Text(
                        stringResource(
                            R.string.import_duplicate_message,
                            duplicatePrompt.originalName,
                        ),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.import_duplicate_settings_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.answerDuplicate(DuplicateAskDecision.Import) },
                ) {
                    Text(stringResource(R.string.import_duplicate_import))
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = { viewModel.answerDuplicate(DuplicateAskDecision.Skip) },
                    ) {
                        Text(stringResource(R.string.import_duplicate_skip))
                    }
                    TextButton(
                        onClick = {
                            viewModel.answerDuplicate(DuplicateAskDecision.SkipAskingRest)
                        },
                    ) {
                        Text(stringResource(R.string.import_duplicate_skip_asking_rest))
                    }
                }
            },
        )
    }

    if (state.failedItems.isNotEmpty() && !state.isImporting) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissFailures() },
            title = {
                Text(stringResource(R.string.import_failures_title, state.failedItems.size))
            },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    items(state.failedItems, key = { it.uri.toString() }) { item ->
                        ListItem(
                            headlineContent = { Text(item.displayName) },
                            supportingContent = {
                                Text(
                                    buildString {
                                        append(stringResource(item.errorKind.messageResId))
                                        item.errorDetail?.let { detail ->
                                            append('\n')
                                            append(detail)
                                        }
                                    },
                                )
                            },
                            trailingContent = {
                                TextButton(onClick = { viewModel.retryItem(item.uri) }) {
                                    Text(stringResource(R.string.import_failure_retry))
                                }
                            },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissFailures() }) {
                    Text(stringResource(R.string.import_failure_done))
                }
            },
            dismissButton = {
                if (state.failedItems.size > 1) {
                    TextButton(onClick = { viewModel.retryAll() }) {
                        Text(stringResource(R.string.import_failure_retry_all))
                    }
                }
            },
        )
    }
}
