package com.pictureorganizer.ui.exportzip

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.pictureorganizer.PictureOrganizerApplication
import com.pictureorganizer.R
import com.pictureorganizer.model.ImageListItem
import com.pictureorganizer.model.TagFilterCriteria
import com.pictureorganizer.ui.common.BackNavIconButton
import com.pictureorganizer.ui.common.LogScreenLifecycle
import com.pictureorganizer.ui.common.showSnackbarReplacing
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExportZipScreen(
    viewModel: ExportZipViewModel,
    onBack: () -> Unit,
    onNavigateToFilter: (TagFilterCriteria) -> Unit,
    onNavigateToExportManage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LogScreenLifecycle("ExportZip")
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val untaggedLabel = stringResource(R.string.filter_untagged)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ExportZipUiEffect.ShowMessage ->
                    snackbarHostState.showSnackbarReplacing(context.getString(effect.messageResId))
                is ExportZipUiEffect.ShowMessageText ->
                    snackbarHostState.showSnackbarReplacing(effect.text)
                is ExportZipUiEffect.ShareFile -> {
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

    BackHandler {
        if (!state.isExporting) onBack()
    }

    val summaryLabels =
        buildList {
            addAll(state.filter.selectedTagNames.sorted())
            if (state.filter.includeUntagged) add(untaggedLabel)
            val q = state.filter.nameContains.trim()
            if (q.isNotEmpty()) add(stringResource(R.string.filter_name_contains) + ": $q")
            if (state.filter.hasDateTakenRange) {
                val unset = stringResource(R.string.filter_date_taken_unset)
                state.filter.dateTakenRangeLabel(unset)?.let { range ->
                    add(stringResource(R.string.filter_date_taken_summary, range))
                }
            }
            if (state.filter.hasImportedAtRange) {
                val unset = stringResource(R.string.filter_date_taken_unset)
                state.filter.importedAtRangeLabel(unset)?.let { range ->
                    add(stringResource(R.string.filter_imported_at_summary, range))
                }
            }
        }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeDrawing.exclude(WindowInsets.ime),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.export_zip_title)) },
                navigationIcon = {
                    BackNavIconButton(
                        onBack = onBack,
                        contentDescription = stringResource(R.string.action_back),
                        enabled = !state.isExporting,
                    )
                },
            )
        },
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 88.dp),
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ExportZipHeader(
                    state = state,
                    summaryLabels = summaryLabels,
                    onNavigateToFilter = onNavigateToFilter,
                    onNavigateToExportManage = onNavigateToExportManage,
                    onEvent = viewModel::onEvent,
                )
            }
            if (state.previewItems.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = stringResource(R.string.export_zip_preview_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            } else {
                items(state.previewItems, key = { it.id }) { item ->
                    ExportPreviewThumb(
                        item = item,
                        enabled = !state.isExporting,
                        onExclude = {
                            viewModel.onEvent(ExportZipUiEvent.ExcludeImage(item.id))
                        },
                    )
                }
            }
            if (state.results.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = stringResource(R.string.export_zip_results),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                    )
                }
                items(
                    items = state.results,
                    key = { it.absolutePath },
                    span = { GridItemSpan(maxLineSpan) },
                ) { result ->
                    ListItem(
                        headlineContent = { Text(result.label) },
                        supportingContent = {
                            Text(
                                stringResource(
                                    R.string.export_zip_result_item,
                                    result.fileName,
                                    result.entryCount,
                                ),
                            )
                        },
                        trailingContent = {
                            TextButton(onClick = { viewModel.requestShare(result) }) {
                                Text(stringResource(R.string.export_zip_share))
                            }
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExportZipHeader(
    state: ExportZipUiState,
    summaryLabels: List<String>,
    onNavigateToFilter: (TagFilterCriteria) -> Unit,
    onNavigateToExportManage: () -> Unit,
    onEvent: (ExportZipUiEvent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text =
                stringResource(
                    R.string.export_zip_stats,
                    state.confirmedCount,
                    state.matchedCount,
                ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = { onNavigateToFilter(state.filter) },
                enabled = !state.isExporting,
            ) {
                Text(stringResource(R.string.action_filter))
            }
            if (state.isFilterActive) {
                TextButton(
                    onClick = { onEvent(ExportZipUiEvent.ClearFilter) },
                    enabled = !state.isExporting,
                ) {
                    Text(stringResource(R.string.action_filter_clear))
                }
            }
        }
        if (summaryLabels.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                summaryLabels.forEach { label ->
                    AssistChip(
                        onClick = { onNavigateToFilter(state.filter) },
                        label = { Text(label) },
                    )
                }
            }
        } else {
            Text(
                text = stringResource(R.string.export_zip_no_filter_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        state.plannedPacks.singleOrNull()?.let { pack ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.export_zip_filenames),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(R.string.export_zip_filename_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            OutlinedTextField(
                value = pack.stem,
                onValueChange = {
                    onEvent(ExportZipUiEvent.ZipStemChanged(pack.label, it))
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                label = { Text(stringResource(R.string.export_zip_filename_label)) },
                singleLine = true,
                enabled = !state.isExporting,
                suffix = {
                    Text(
                        text = ".zip",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onEvent(ExportZipUiEvent.StartExport) },
            enabled = !state.isExporting && state.matchedCount > 0,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.export_zip_start))
        }
        TextButton(
            onClick = onNavigateToExportManage,
            enabled = !state.isExporting,
            modifier = Modifier.padding(top = 4.dp),
        ) {
            Text(stringResource(R.string.export_zip_manage))
        }

        if (state.isExporting) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator()
                Text(
                    text =
                        stringResource(
                            R.string.export_zip_progress,
                            state.progressCurrent,
                            state.progressTotal,
                        ),
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun ExportPreviewThumb(
    item: ImageListItem,
    enabled: Boolean,
    onExclude: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp)),
    ) {
        PreviewThumbnail(item = item)
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .size(36.dp)
                    .clickable(enabled = enabled, onClick = onExclude),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.export_zip_exclude),
                tint = Color.White,
                modifier =
                    Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(3.dp),
            )
        }
    }
}

@Composable
private fun PreviewThumbnail(
    item: ImageListItem,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val placeholder = Color(item.placeholderColorArgb)
    val file = rememberImageFile(item.filePath)

    SubcomposeAsyncImage(
        model =
            ImageRequest
                .Builder(context)
                .data(file)
                .crossfade(true)
                .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize().background(placeholder),
        loading = {
            PlaceholderThumb(placeholder)
        },
        error = {
            PlaceholderThumb(placeholder)
        },
    )
}

@Composable
private fun PlaceholderThumb(color: Color) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun rememberImageFile(relativePath: String): File? {
    val context = LocalContext.current
    if (relativePath.isBlank()) return null
    val app = context.applicationContext as? PictureOrganizerApplication
    return if (app != null) {
        app.fileManager.absoluteFile(relativePath)
    } else {
        null
    }
}
