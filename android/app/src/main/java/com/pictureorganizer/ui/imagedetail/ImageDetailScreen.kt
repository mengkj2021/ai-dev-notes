package com.pictureorganizer.ui.imagedetail

import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pictureorganizer.PictureOrganizerApplication
import com.pictureorganizer.R
import com.pictureorganizer.model.ImageListItem
import com.pictureorganizer.model.ImageStatus
import com.pictureorganizer.ui.common.BackNavIconButton
import com.pictureorganizer.ui.common.LogScreenLifecycle
import com.pictureorganizer.ui.common.showSnackbarReplacing
import com.pictureorganizer.ui.main.statusNameRes
import com.pictureorganizer.util.image.ExifDateTaken
import java.io.File

private const val TAP_SUPPRESS_AFTER_TRANSFORM_MS = 350L

private const val LIBRARY_TAG_COLLAPSE_THRESHOLD = 10

/** Bug15：主图纵向最低高度，避免编辑区内容过高时被挤到不可浏览。 */
private val DetailMainImageMinHeight = 200.dp

/** 缩略条固定高度（含底 padding），与 SiblingThumbStrip 调用处一致。 */
private val DetailSiblingThumbStripHeight = 88.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ImageDetailScreen(
    imageId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    visibleSiblingCount: Int = 0,
) {
    LogScreenLifecycle("ImageDetail")
    val context = LocalContext.current
    val app = context.applicationContext as PictureOrganizerApplication
    val viewModel: ImageDetailViewModel =
        viewModel(
            key = imageId,
            factory =
                ImageDetailViewModel.Factory(
                    imageId = imageId,
                    repository = app.imageRepository,
                    tagRepository = app.tagRepository,
                    renameTemplateRepository = app.renameTemplateRepository,
                    fileManager = app.fileManager,
                    visibleSiblingCount = visibleSiblingCount,
                ),
        )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ImageDetailUiEffect.ShowMessage -> {
                    snackbarHostState.showSnackbarReplacing(context.getString(effect.messageResId))
                }
                is ImageDetailUiEffect.ShowMessageText -> {
                    snackbarHostState.showSnackbarReplacing(effect.text)
                }
                ImageDetailUiEffect.NavigateBack -> onBack()
            }
        }
    }

    val title =
        state.current?.let { item ->
            item.filePath.substringAfterLast('/').ifEmpty { item.description }
        } ?: stringResource(R.string.detail_title)

    var moreMenuExpanded by remember { mutableStateOf(false) }
    var pendingDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing.exclude(WindowInsets.ime),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = title, maxLines = 1) },
                navigationIcon = {
                    BackNavIconButton(
                        onBack = onBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                },
                actions = {
                    if (state.current != null) {
                        Box {
                            IconButton(
                                onClick = { moreMenuExpanded = true },
                                enabled = !state.isBusy,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = stringResource(R.string.action_more),
                                )
                            }
                            DropdownMenu(
                                expanded = moreMenuExpanded,
                                onDismissRequest = { moreMenuExpanded = false },
                            ) {
                                detailMoveTargets(state.current!!.status).forEach { target ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                stringResource(
                                                    R.string.detail_move_to_fmt,
                                                    stringResource(target.statusNameRes()),
                                                ),
                                            )
                                        },
                                        enabled = !state.isBusy,
                                        onClick = {
                                            moreMenuExpanded = false
                                            viewModel.onEvent(ImageDetailUiEvent.MoveTo(target))
                                        },
                                    )
                                }
                                if (state.current!!.status == ImageStatus.NoModify) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.action_delete)) },
                                        enabled = !state.isBusy,
                                        onClick = {
                                            moreMenuExpanded = false
                                            pendingDeleteConfirm = true
                                        },
                                    )
                                }
                            }
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            state.notFound && state.current == null && !state.isBusy -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.detail_missing))
                }
            }
            else -> {
                val current = state.current
                var forceShowEditor by remember(state.currentId) { mutableStateOf(false) }
                var imageScaled by remember(state.currentId) { mutableStateOf(false) }
                val showEditor = !imageScaled || forceShowEditor
                // Bug15：先量可用高度，给编辑区 heightIn(max)，否则 verticalScroll 无界、主图被固有高度挤扁。
                BoxWithConstraints(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .imePadding(),
                ) {
                    val maxEditorHeight =
                        (maxHeight - DetailMainImageMinHeight - DetailSiblingThumbStripHeight)
                            .coerceAtLeast(120.dp)
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (current != null) {
                            ZoomableImage(
                                file = viewModel.absoluteFile(current),
                                placeholderColor = Color(current.placeholderColorArgb),
                                contentKey = current.id,
                                onScaledChanged = { scaled ->
                                    imageScaled = scaled

                                    forceShowEditor = false
                                },
                                onTransform = {
                                    if (forceShowEditor) {
                                        forceShowEditor = false
                                    }
                                },
                                onSingleTap = {
                                    if (imageScaled) {
                                        forceShowEditor = !forceShowEditor
                                    }
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .heightIn(min = DetailMainImageMinHeight),
                            )
                        } else {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .heightIn(min = DetailMainImageMinHeight),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        if (showEditor) {
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = maxEditorHeight)
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                val dateTakenText =
                                    current?.dateTakenMillis?.let { millis ->
                                        stringResource(
                                            R.string.detail_date_taken,
                                            ExifDateTaken.formatForDisplay(millis),
                                        )
                                    } ?: stringResource(R.string.detail_date_taken_unknown)
                                Text(
                                    text = dateTakenText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                // F52：文件体积只读；缩略条切换随 current 重读；缺失/不可读 → 未知文案
                                val fileSizeBytes =
                                    remember(current?.id, current?.filePath) {
                                        val item = current ?: return@remember null
                                        val file = viewModel.absoluteFile(item)
                                        if (!file.isFile) return@remember null
                                        file.length().takeIf { it >= 0L }
                                    }
                                val fileSizeText =
                                    if (fileSizeBytes != null) {
                                        stringResource(
                                            R.string.detail_file_size,
                                            Formatter.formatShortFileSize(context, fileSizeBytes),
                                        )
                                    } else {
                                        stringResource(R.string.detail_file_size_unknown)
                                    }
                                Text(
                                    text = fileSizeText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = stringResource(R.string.detail_rename),
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    OutlinedTextField(
                                        value = state.renameStemDraft,
                                        onValueChange = {
                                            viewModel.onEvent(ImageDetailUiEvent.RenameDraftChanged(it))
                                        },
                                        modifier =
                                            Modifier
                                                .weight(1f)
                                                .onFocusChanged { focus ->
                                                    if (!focus.isFocused) {
                                                        viewModel.onEvent(ImageDetailUiEvent.RenameFocusLost)
                                                    }
                                                },
                                        singleLine = true,
                                        enabled = !state.isBusy,
                                        suffix = {
                                            if (state.renameExtension.isNotEmpty()) {
                                                Text(
                                                    text = ".${state.renameExtension}",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        },
                                    )
                                    Button(
                                        onClick = { viewModel.onEvent(ImageDetailUiEvent.SaveRename) },
                                        enabled = !state.isBusy,
                                    ) {
                                        Text(stringResource(R.string.detail_rename_save))
                                    }
                                }
                                var renameTemplateMenuExpanded by remember { mutableStateOf(false) }
                                Box {
                                    TextButton(
                                        onClick = { renameTemplateMenuExpanded = true },
                                        enabled = !state.isBusy && state.renameTemplates.isNotEmpty(),
                                    ) {
                                        Text(stringResource(R.string.detail_apply_rename_template))
                                    }
                                    DropdownMenu(
                                        expanded = renameTemplateMenuExpanded,
                                        onDismissRequest = { renameTemplateMenuExpanded = false },
                                    ) {
                                        state.renameTemplates.forEach { template ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        if (template.isDefault) {
                                                            stringResource(
                                                                R.string.detail_template_default_fmt,
                                                                template.name,
                                                            )
                                                        } else {
                                                            template.name
                                                        },
                                                    )
                                                },
                                                onClick = {
                                                    renameTemplateMenuExpanded = false
                                                    viewModel.onEvent(
                                                        ImageDetailUiEvent.ApplyRenameTemplate(template.id),
                                                    )
                                                },
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = stringResource(R.string.detail_tags),
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                val userTags =
                                    current
                                        ?.let { ImageListItem.userTagsOf(it.tags) }
                                        .orEmpty()
                                val libraryNames = state.libraryTags.map { it.name }.toSet()
                                var showLibraryPicker by remember { mutableStateOf(false) }
                                val libraryPickerSheetState =
                                    rememberModalBottomSheetState(skipPartiallyExpanded = true)
                                val collapseLibrary =
                                    state.libraryTags.size > LIBRARY_TAG_COLLAPSE_THRESHOLD
                                if (state.libraryTags.isNotEmpty()) {
                                    Text(
                                        text = stringResource(R.string.detail_tag_library),
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                    val libraryChips =
                                        if (collapseLibrary) {
                                            state.libraryTags.filter { it.name in userTags }
                                        } else {
                                            state.libraryTags
                                        }
                                    if (libraryChips.isNotEmpty()) {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            libraryChips.forEach { tag ->
                                                FilterChip(
                                                    selected = tag.name in userTags,
                                                    onClick = {
                                                        viewModel.onEvent(
                                                            ImageDetailUiEvent.ToggleLibraryTag(tag.name),
                                                        )
                                                    },
                                                    enabled = !state.isBusy,
                                                    label = { Text(tag.name) },
                                                )
                                            }
                                        }
                                    }
                                    if (collapseLibrary) {
                                        OutlinedButton(
                                            onClick = { showLibraryPicker = true },
                                            enabled = !state.isBusy,
                                        ) {
                                            Text(stringResource(R.string.detail_tag_pick_library))
                                        }
                                    }
                                }
                                if (showLibraryPicker) {
                                    ModalBottomSheet(
                                        onDismissRequest = { showLibraryPicker = false },
                                        sheetState = libraryPickerSheetState,
                                    ) {
                                        Column(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp)
                                                    .padding(bottom = 32.dp)
                                                    .verticalScroll(rememberScrollState()),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Text(
                                                text = stringResource(R.string.detail_tag_pick_library_title),
                                                style = MaterialTheme.typography.titleMedium,
                                            )
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                            ) {
                                                state.libraryTags.forEach { tag ->
                                                    FilterChip(
                                                        selected = tag.name in userTags,
                                                        onClick = {
                                                            viewModel.onEvent(
                                                                ImageDetailUiEvent.ToggleLibraryTag(tag.name),
                                                            )
                                                        },
                                                        enabled = !state.isBusy,
                                                        label = { Text(tag.name) },
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                val customTags =
                                    userTags
                                        .withIndex()
                                        .filter { (_, name) -> name !in libraryNames }
                                if (customTags.isNotEmpty()) {
                                    Text(
                                        text = stringResource(R.string.detail_tag_custom),
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        customTags.forEach { (index, tag) ->
                                            InputChip(
                                                selected = state.editingUserTagIndex == index,
                                                onClick = {
                                                    viewModel.onEvent(ImageDetailUiEvent.StartEditTag(index))
                                                },
                                                label = { Text(tag) },
                                                trailingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = stringResource(R.string.detail_tag_delete),
                                                        modifier =
                                                            Modifier
                                                                .size(18.dp)
                                                                .clickable {
                                                                    viewModel.onEvent(
                                                                        ImageDetailUiEvent.DeleteTag(index),
                                                                    )
                                                                },
                                                    )
                                                },
                                            )
                                        }
                                    }
                                }
                                var templateMenuExpanded by remember { mutableStateOf(false) }
                                Box {
                                    OutlinedButton(
                                        onClick = { templateMenuExpanded = true },
                                        enabled = !state.isBusy && state.templates.isNotEmpty(),
                                    ) {
                                        Text(stringResource(R.string.detail_apply_template))
                                    }
                                    DropdownMenu(
                                        expanded = templateMenuExpanded,
                                        onDismissRequest = { templateMenuExpanded = false },
                                    ) {
                                        state.templates.forEach { template ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        if (template.isDefault) {
                                                            stringResource(
                                                                R.string.detail_template_default_fmt,
                                                                template.name,
                                                            )
                                                        } else {
                                                            template.name
                                                        },
                                                    )
                                                },
                                                onClick = {
                                                    templateMenuExpanded = false
                                                    viewModel.onEvent(
                                                        ImageDetailUiEvent.ApplyTemplate(template.id),
                                                    )
                                                },
                                            )
                                        }
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    OutlinedTextField(
                                        value = state.tagDraft,
                                        onValueChange = {
                                            viewModel.onEvent(ImageDetailUiEvent.TagDraftChanged(it))
                                        },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        enabled = !state.isBusy,
                                        label = {
                                            Text(
                                                if (state.editingUserTagIndex != null) {
                                                    stringResource(R.string.detail_tag_edit)
                                                } else {
                                                    stringResource(R.string.detail_tag_add)
                                                },
                                            )
                                        },
                                    )
                                    Button(
                                        onClick = { viewModel.onEvent(ImageDetailUiEvent.SaveTag) },
                                        enabled = !state.isBusy,
                                    ) {
                                        Text(stringResource(R.string.detail_tag_save))
                                    }
                                    if (state.editingUserTagIndex != null) {
                                        OutlinedButton(
                                            onClick = { viewModel.onEvent(ImageDetailUiEvent.CancelEditTag) },
                                            enabled = !state.isBusy,
                                        ) {
                                            Text(stringResource(R.string.detail_tag_cancel))
                                        }
                                    }
                                }
                            }
                        }

                        SiblingThumbStrip(
                            siblings = state.siblings,
                            selectedId = state.currentId,
                            resolveFile = { viewModel.absoluteFile(it) },
                            onSelect = { viewModel.onEvent(ImageDetailUiEvent.SelectSibling(it)) },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(DetailSiblingThumbStripHeight)
                                    .padding(bottom = 8.dp),
                        )
                    }
                }
            }
        }
    }

    if (pendingDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { pendingDeleteConfirm = false },
            title = { Text(stringResource(R.string.action_delete)) },
            text = { Text(stringResource(R.string.detail_confirm_delete)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteConfirm = false
                        viewModel.onEvent(ImageDetailUiEvent.DeleteCurrent)
                    },
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteConfirm = false }) {
                    Text(stringResource(R.string.detail_tag_cancel))
                }
            },
        )
    }
}

private fun detailMoveTargets(current: ImageStatus): List<ImageStatus> =
    when (current) {
        ImageStatus.Pending -> listOf(ImageStatus.Confirmed, ImageStatus.NoModify)
        ImageStatus.Confirmed -> listOf(ImageStatus.Pending, ImageStatus.NoModify)
        ImageStatus.NoModify -> listOf(ImageStatus.Pending, ImageStatus.Confirmed)
    }

@Composable
private fun ZoomableImage(
    file: File,
    placeholderColor: Color,
    contentKey: String,
    onScaledChanged: (Boolean) -> Unit,
    onTransform: () -> Unit,
    onSingleTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var scale by remember(contentKey) { mutableFloatStateOf(1f) }
    var offset by remember(contentKey) { mutableStateOf(Offset.Zero) }

    val lastTransformUptimeMs = remember(contentKey) { longArrayOf(0L) }
    val currentOnTransform by rememberUpdatedState(onTransform)
    val currentOnSingleTap by rememberUpdatedState(onSingleTap)
    val currentOnScaledChanged by rememberUpdatedState(onScaledChanged)
    val context = LocalContext.current

    LaunchedEffect(scale > 1f) {
        currentOnScaledChanged(scale > 1f)
    }

    Box(
        modifier =
            modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .pointerInput(contentKey) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val transformed = zoom != 1f || pan != Offset.Zero
                        if (transformed) {
                            lastTransformUptimeMs[0] = System.currentTimeMillis()
                            currentOnTransform()
                        }
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        offset = if (scale > 1f) offset + pan else Offset.Zero
                    }
                }.pointerInput(contentKey) {
                    detectTapGestures(
                        onTap = {
                            val sinceTransform = System.currentTimeMillis() - lastTransformUptimeMs[0]
                            if (sinceTransform < TAP_SUPPRESS_AFTER_TRANSFORM_MS) {
                                return@detectTapGestures
                            }
                            currentOnSingleTap()
                        },
                        onDoubleTap = {
                            scale = 1f
                            offset = Offset.Zero
                        },
                    )
                },
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model =
                ImageRequest
                    .Builder(context)
                    .data(file)
                    .crossfade(true)
                    .build(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y,
                    ),
        )
        if (!file.exists()) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(placeholderColor.copy(alpha = 0.5f)),
            )
        }
    }
}

@Composable
private fun SiblingThumbStrip(
    siblings: List<ImageListItem>,
    selectedId: String,
    resolveFile: (ImageListItem) -> File,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(siblings, key = { it.id }) { item ->
            val selected = item.id == selectedId
            val shape = RoundedCornerShape(8.dp)
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(context)
                        .data(resolveFile(item))
                        .crossfade(true)
                        .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .size(72.dp)
                        .clip(shape)
                        .then(
                            if (selected) {
                                Modifier.border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = shape,
                                )
                            } else {
                                Modifier
                            },
                        ).background(Color(item.placeholderColorArgb))
                        .clickable { onSelect(item.id) },
            )
        }
    }
}
