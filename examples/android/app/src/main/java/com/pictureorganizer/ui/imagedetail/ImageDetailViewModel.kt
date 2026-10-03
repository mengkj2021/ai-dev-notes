package com.pictureorganizer.ui.imagedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pictureorganizer.R
import com.pictureorganizer.data.repository.ImageRepository
import com.pictureorganizer.data.repository.RenameTemplateRepository
import com.pictureorganizer.data.repository.TagRepository
import com.pictureorganizer.model.ImageListItem
import com.pictureorganizer.model.ImageStatus
import com.pictureorganizer.model.Tag
import com.pictureorganizer.model.tagsFromTagTemplateOverwrite
import com.pictureorganizer.util.file.AppFileManager
import com.pictureorganizer.util.file.RenamePatternApplier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ImageDetailViewModel(
    initialImageId: String,
    private val repository: ImageRepository,
    private val tagRepository: TagRepository,
    private val renameTemplateRepository: RenameTemplateRepository,
    private val fileManager: AppFileManager,
) : ViewModel() {
    private val currentId = MutableStateFlow(initialImageId)
    private val editor = MutableStateFlow(EditorState())
    private val busy = MutableStateFlow(false)

    private val _effects = Channel<ImageDetailUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val currentItemFlow =
        currentId.flatMapLatest { id ->
            repository.observeItem(id)
        }

    private val siblingsFlow =
        currentItemFlow.flatMapLatest { item ->
            if (item == null) {
                flowOf(emptyList())
            } else {
                repository.observeItems(item.status)
            }
        }

    private val imageBundle =
        combine(
            currentId,
            currentItemFlow,
            siblingsFlow,
        ) { id, item, siblings ->
            Triple(id, item, siblings)
        }

    private val tagsAndTemplates =
        combine(
            tagRepository.observeTags(),
            tagRepository.observeTemplates(),
            renameTemplateRepository.observeAll(),
        ) { libraryTags, templates, renameTemplates ->
            Triple(libraryTags, templates, renameTemplates)
        }

    val uiState: StateFlow<ImageDetailUiState> =
        combine(
            imageBundle,
            editor,
            busy,
            tagsAndTemplates,
        ) { bundle, ed, isBusy, tagBundle ->
            val (id, item, siblings) = bundle
            val (libraryTags, templates, renameTemplates) = tagBundle
            val fileName =
                item?.fileNameFromPath()?.ifEmpty { item.description }.orEmpty()
            val parts = splitFileName(fileName)
            val stemDraft =
                ed.renameStemDraft.takeIf { ed.renameTouched } ?: parts.stem
            ImageDetailUiState(
                currentId = id,
                current = item,
                siblings = siblings,
                renameStemDraft = stemDraft,
                renameExtension = parts.extension,
                tagDraft = ed.tagDraft,
                editingUserTagIndex = ed.editingUserTagIndex,
                libraryTags = libraryTags,
                templates = templates,
                renameTemplates = renameTemplates,
                isBusy = isBusy,
                notFound = item == null,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ImageDetailUiState(currentId = initialImageId),
        )

    fun absoluteFile(item: ImageListItem): File = fileManager.absoluteFile(item.filePath)

    fun onEvent(event: ImageDetailUiEvent) {
        when (event) {
            is ImageDetailUiEvent.SelectSibling -> selectSibling(event.id)
            is ImageDetailUiEvent.RenameDraftChanged ->
                editor.update {
                    it.copy(renameStemDraft = event.value, renameTouched = true)
                }
            ImageDetailUiEvent.RenameFocusLost -> restoreRenameStemIfBlank()
            ImageDetailUiEvent.SaveRename -> saveRename()
            is ImageDetailUiEvent.TagDraftChanged ->
                editor.update {
                    it.copy(tagDraft = event.value)
                }
            is ImageDetailUiEvent.StartEditTag -> startEditTag(event.userTagIndex)
            ImageDetailUiEvent.CancelEditTag ->
                editor.update {
                    it.copy(tagDraft = "", editingUserTagIndex = null)
                }
            ImageDetailUiEvent.SaveTag -> saveTag()
            is ImageDetailUiEvent.DeleteTag -> deleteTag(event.userTagIndex)
            is ImageDetailUiEvent.ToggleLibraryTag -> toggleLibraryTag(event.name)
            is ImageDetailUiEvent.ApplyTemplate -> applyTemplate(event.templateId)
            is ImageDetailUiEvent.ApplyRenameTemplate -> applyRenameTemplate(event.templateId)
            is ImageDetailUiEvent.MoveTo -> moveCurrentTo(event.status)
            ImageDetailUiEvent.DeleteCurrent -> deleteCurrent()
        }
    }

    private fun moveCurrentTo(target: ImageStatus) {
        val state = uiState.value
        val item = state.current ?: return
        if (item.status == target || state.isBusy) return
        val siblings = state.siblings
        val fromId = item.id
        val fromStatus = item.status
        val nextId = nextSiblingIdAfterRemoval(siblings, fromId)

        val goBack = shouldReturnAfterRemoval(siblings, fromId)
        viewModelScope.launch {
            busy.value = true
            runCatching {
                repository.moveItems(setOf(fromId), fromStatus, target)
            }.onSuccess {
                editor.value = EditorState()
                if (goBack || nextId == null) {
                    _effects.send(ImageDetailUiEffect.NavigateBack)
                } else {
                    currentId.value = nextId
                    _effects.send(ImageDetailUiEffect.ShowMessage(R.string.detail_move_ok))
                }
            }.onFailure {
                _effects.send(ImageDetailUiEffect.ShowMessage(R.string.detail_move_failed))
            }
            busy.value = false
        }
    }

    private fun deleteCurrent() {
        val state = uiState.value
        val item = state.current ?: return
        if (item.status != ImageStatus.NoModify || state.isBusy) return
        val id = item.id
        viewModelScope.launch {
            busy.value = true
            runCatching {
                repository.deleteItems(setOf(id))
            }.onSuccess {
                _effects.send(ImageDetailUiEffect.NavigateBack)
            }.onFailure {
                _effects.send(ImageDetailUiEffect.ShowMessage(R.string.detail_delete_failed))
            }
            busy.value = false
        }
    }

    private fun selectSibling(id: String) {
        if (id == currentId.value) return
        currentId.value = id
        editor.value = EditorState()
    }

    private fun startEditTag(userTagIndex: Int) {
        val item = uiState.value.current ?: return
        val userTags = ImageListItem.userTagsOf(item.tags)
        val tag = userTags.getOrNull(userTagIndex) ?: return
        editor.update {
            it.copy(tagDraft = tag, editingUserTagIndex = userTagIndex)
        }
    }

    private fun restoreRenameStemIfBlank() {
        if (!editor.value.renameTouched) return
        if (editor.value.renameStemDraft
                .trim()
                .isNotEmpty()
        ) {
            return
        }
        editor.update { it.copy(renameTouched = false, renameStemDraft = "") }
    }

    private fun saveRename() {
        val id = currentId.value
        val state = uiState.value
        val stem = state.renameStemDraft.trim()
        if (stem.isEmpty()) {
            viewModelScope.launch {
                _effects.send(ImageDetailUiEffect.ShowMessage(R.string.detail_rename_empty))
            }
            return
        }

        val name =
            if (state.renameExtension.isEmpty()) {
                stem
            } else {
                "$stem.${state.renameExtension}"
            }
        viewModelScope.launch {
            busy.value = true
            runCatching { repository.rename(id, name) }
                .onSuccess {
                    editor.update { it.copy(renameTouched = false) }
                    _effects.send(ImageDetailUiEffect.ShowMessage(R.string.detail_rename_ok))
                }.onFailure {
                    _effects.send(
                        ImageDetailUiEffect.ShowMessage(R.string.error_rename_failed),
                    )
                }
            busy.value = false
        }
    }

    private fun saveTag() {
        val item = uiState.value.current ?: return
        val draft = editor.value.tagDraft.trim()
        if (draft.isEmpty()) {
            viewModelScope.launch {
                _effects.send(ImageDetailUiEffect.ShowMessage(R.string.detail_tag_empty))
            }
            return
        }
        if (ImageListItem.isReservedStatusName(draft)) {
            viewModelScope.launch {
                _effects.send(ImageDetailUiEffect.ShowMessage(R.string.detail_tag_reserved))
            }
            return
        }
        val userTags = ImageListItem.userTagsOf(item.tags).toMutableList()
        val editIndex = editor.value.editingUserTagIndex
        if (editIndex != null) {
            if (editIndex !in userTags.indices) return
            userTags[editIndex] = draft
        } else {
            if (draft in userTags) {
                viewModelScope.launch {
                    _effects.send(ImageDetailUiEffect.ShowMessage(R.string.detail_tag_exists))
                }
                return
            }
            userTags.add(draft)
        }
        persistTags(item, userTags, clearEditor = true, ensureLibraryNames = listOf(draft))
    }

    private fun deleteTag(userTagIndex: Int) {
        val item = uiState.value.current ?: return
        val userTags = ImageListItem.userTagsOf(item.tags).toMutableList()
        if (userTagIndex !in userTags.indices) return
        userTags.removeAt(userTagIndex)
        persistTags(item, userTags, clearEditor = true)
    }

    private fun toggleLibraryTag(name: String) {
        val item = uiState.value.current ?: return
        val trimmed = name.trim()
        if (trimmed.isEmpty() || ImageListItem.isReservedStatusName(trimmed)) return
        val userTags = ImageListItem.userTagsOf(item.tags).toMutableList()
        if (trimmed in userTags) {
            userTags.removeAll { it == trimmed }
        } else {
            userTags.add(trimmed)
        }
        persistTags(item, userTags, clearEditor = false)
    }

    private fun applyTemplate(templateId: String) {
        val item = uiState.value.current ?: return
        val template = uiState.value.templates.find { it.id == templateId } ?: return

        val next = tagsFromTagTemplateOverwrite(template.tagNames)
        val current = ImageListItem.userTagsOf(item.tags)
        if (next == current) {
            viewModelScope.launch {
                _effects.send(ImageDetailUiEffect.ShowMessage(R.string.detail_template_no_change))
            }
            return
        }
        persistTags(item, next, clearEditor = false)
    }

    private fun applyRenameTemplate(templateId: String) {
        val item = uiState.value.current ?: return
        val template = uiState.value.renameTemplates.find { it.id == templateId } ?: return
        val currentFull =
            buildFullFileName(uiState.value.renameStemDraft, uiState.value.renameExtension)
                .ifBlank {
                    item.fileNameFromPath().ifBlank { item.description }
                }
        val next = RenamePatternApplier.applyForItem(template.pattern, item, currentFull)

        val locked = lockExtension(next, uiState.value.renameExtension)
        val stem = splitFileName(locked).stem
        editor.update { it.copy(renameStemDraft = stem, renameTouched = true) }
        viewModelScope.launch {
            busy.value = true
            runCatching { repository.rename(item.id, locked) }
                .onSuccess {
                    editor.update { it.copy(renameTouched = false) }
                    _effects.send(ImageDetailUiEffect.ShowMessage(R.string.detail_rename_ok))
                }.onFailure {
                    _effects.send(
                        ImageDetailUiEffect.ShowMessage(R.string.error_rename_failed),
                    )
                }
            busy.value = false
        }
    }

    private fun persistTags(
        item: ImageListItem,
        userTags: List<String>,
        clearEditor: Boolean,
        ensureLibraryNames: List<String> = emptyList(),
    ) {
        viewModelScope.launch {
            busy.value = true
            runCatching { repository.updateTags(item.id, userTags) }
                .onSuccess { exifOk ->

                    ensureLibraryNames.forEach { raw ->
                        ensureTagInLibrary(raw)
                    }
                    if (clearEditor) {
                        editor.value =
                            EditorState(
                                renameStemDraft = editor.value.renameStemDraft,
                                renameTouched = editor.value.renameTouched,
                            )
                    }
                    if (exifOk) {
                        _effects.send(ImageDetailUiEffect.ShowMessage(R.string.detail_tags_ok))
                    } else {
                        _effects.send(ImageDetailUiEffect.ShowMessage(R.string.detail_exif_warn))
                    }
                }.onFailure {
                    _effects.send(
                        ImageDetailUiEffect.ShowMessage(R.string.error_tag_save_failed),
                    )
                }
            busy.value = false
        }
    }

    private suspend fun ensureTagInLibrary(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || ImageListItem.isReservedStatusName(trimmed)) return
        val exists = tagRepository.getTags().any { it.name == trimmed }
        if (exists) return
        runCatching {
            tagRepository.insertTag(
                Tag(
                    id = "",
                    name = trimmed,
                ),
            )
        }
    }

    private data class EditorState(
        val renameStemDraft: String = "",
        val renameTouched: Boolean = false,
        val tagDraft: String = "",
        val editingUserTagIndex: Int? = null,
    )

    class Factory(
        private val imageId: String,
        private val repository: ImageRepository,
        private val tagRepository: TagRepository,
        private val renameTemplateRepository: RenameTemplateRepository,
        private val fileManager: AppFileManager,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ImageDetailViewModel(
                imageId,
                repository,
                tagRepository,
                renameTemplateRepository,
                fileManager,
            ) as T
    }
}

private fun ImageListItem.fileNameFromPath(): String {
    if (filePath.isNotEmpty()) return filePath.substringAfterLast('/')
    return description
}

private data class FileNameParts(
    val stem: String,
    val extension: String,
)

private fun splitFileName(fileName: String): FileNameParts {
    val trimmed = fileName.trim()
    if (trimmed.isEmpty()) return FileNameParts("", "")
    val dot = trimmed.lastIndexOf('.')
    if (dot <= 0 || dot == trimmed.lastIndex) {
        return FileNameParts(trimmed, "")
    }
    return FileNameParts(
        stem = trimmed.substring(0, dot),
        extension = trimmed.substring(dot + 1),
    )
}

private fun buildFullFileName(
    stem: String,
    extension: String,
): String {
    val s = stem.trim()
    if (s.isEmpty()) return ""
    return if (extension.isEmpty()) s else "$s.$extension"
}

private fun lockExtension(
    requested: String,
    lockedExtension: String,
): String {
    val stem = splitFileName(requested.trim()).stem.ifBlank { requested.trim() }
    return buildFullFileName(stem, lockedExtension).ifBlank { requested.trim() }
}

internal fun nextSiblingIdAfterRemoval(
    siblings: List<ImageListItem>,
    removedId: String,
): String? {
    val idx = siblings.indexOfFirst { it.id == removedId }
    val remaining = siblings.filter { it.id != removedId }
    if (remaining.isEmpty()) return null
    if (idx < 0) return remaining.first().id
    return if (idx < remaining.size) remaining[idx].id else remaining.last().id
}

/** S9：仅同状态全量无剩余时回一览（不再看筛选可见数）。 */
internal fun shouldReturnAfterRemoval(
    siblings: List<ImageListItem>,
    removedId: String,
): Boolean = nextSiblingIdAfterRemoval(siblings, removedId) == null
