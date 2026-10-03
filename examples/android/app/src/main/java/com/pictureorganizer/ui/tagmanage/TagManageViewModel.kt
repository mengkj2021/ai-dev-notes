package com.pictureorganizer.ui.tagmanage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pictureorganizer.R
import com.pictureorganizer.data.repository.ImageRepository
import com.pictureorganizer.data.repository.TagRepository
import com.pictureorganizer.data.repository.UserPreferencesRepository
import com.pictureorganizer.model.Tag
import com.pictureorganizer.model.TagNameSaveFailure
import com.pictureorganizer.model.TagTemplate
import com.pictureorganizer.model.classifyTagNameSaveFailure
import com.pictureorganizer.model.classifyTemplateNameSaveFailure
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class TagManageViewModel(
    private val tagRepository: TagRepository,
    private val imageRepository: ImageRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val editor = MutableStateFlow(EditorUi())
    private val _effects = Channel<TagManageUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    val uiState: StateFlow<TagManageUiState> =
        combine(
            tagRepository.observeTags(),
            tagRepository.observeTemplates(),
            editor,
        ) { tags, templates, ed ->
            TagManageUiState(
                selectedTab = ed.selectedTab,
                tags = tags,
                templates = templates,
                tagDialog = ed.tagDialog,
                templateDialog = ed.templateDialog,
                confirmDeleteTagId = ed.confirmDeleteTagId,
                cascadeDeleteTag = ed.cascadeDeleteTag,
                cascadeRenameTag = ed.cascadeRenameTag,
                confirmDeleteTemplateId = ed.confirmDeleteTemplateId,
                isBusy = ed.isBusy,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TagManageUiState(),
        )

    fun onEvent(event: TagManageUiEvent) {
        when (event) {
            is TagManageUiEvent.SelectTab -> editor.update { it.copy(selectedTab = event.tab) }
            TagManageUiEvent.OpenAddTag ->
                editor.update {
                    it.copy(tagDialog = TagDialogState())
                }
            is TagManageUiEvent.OpenEditTag ->
                editor.update {
                    it.copy(tagDialog = TagDialogState(editingId = event.tag.id, name = event.tag.name))
                }
            is TagManageUiEvent.TagNameChanged ->
                editor.update { ed ->
                    ed.copy(
                        tagDialog =
                            ed.tagDialog?.copy(
                                name = event.value,
                                errorMessageResId = null,
                            ),
                    )
                }
            TagManageUiEvent.SaveTag -> saveTag()
            TagManageUiEvent.DismissTagDialog -> editor.update { it.copy(tagDialog = null) }
            is TagManageUiEvent.RequestDeleteTag ->
                editor.update {
                    it.copy(confirmDeleteTagId = event.id, cascadeDeleteTag = null)
                }
            TagManageUiEvent.ConfirmDeleteTag -> confirmDeleteTag()
            TagManageUiEvent.CancelDeleteTag -> editor.update { it.copy(confirmDeleteTagId = null) }
            TagManageUiEvent.ConfirmCascadeDeleteTag -> confirmCascadeDeleteTag()
            TagManageUiEvent.CancelCascadeDeleteTag -> editor.update { it.copy(cascadeDeleteTag = null) }
            TagManageUiEvent.ConfirmCascadeRenameTag -> confirmCascadeRenameTag()
            TagManageUiEvent.CancelCascadeRenameTag -> editor.update { it.copy(cascadeRenameTag = null) }

            TagManageUiEvent.OpenAddTemplate ->
                editor.update {
                    it.copy(templateDialog = TemplateDialogState())
                }
            is TagManageUiEvent.OpenEditTemplate ->
                editor.update {
                    it.copy(
                        templateDialog =
                            TemplateDialogState(
                                editingId = event.template.id,
                                name = event.template.name,
                                selectedTagNames = event.template.tagNames.toSet(),
                                isDefault = event.template.isDefault,
                            ),
                    )
                }
            is TagManageUiEvent.TemplateNameChanged ->
                editor.update { ed ->
                    ed.copy(
                        templateDialog =
                            ed.templateDialog?.copy(
                                name = event.value,
                                errorMessageResId = null,
                            ),
                    )
                }
            is TagManageUiEvent.ToggleTemplateTag ->
                editor.update { ed ->
                    val dialog = ed.templateDialog ?: return@update ed
                    val next =
                        if (event.name in dialog.selectedTagNames) {
                            dialog.selectedTagNames - event.name
                        } else {
                            dialog.selectedTagNames + event.name
                        }
                    ed.copy(templateDialog = dialog.copy(selectedTagNames = next))
                }
            is TagManageUiEvent.TemplateDefaultChanged ->
                editor.update { ed ->
                    ed.copy(templateDialog = ed.templateDialog?.copy(isDefault = event.value))
                }
            TagManageUiEvent.SaveTemplate -> saveTemplate()
            TagManageUiEvent.DismissTemplateDialog -> editor.update { it.copy(templateDialog = null) }
            is TagManageUiEvent.SetDefaultTemplate -> setDefault(event.id)
            is TagManageUiEvent.RequestDeleteTemplate ->
                editor.update {
                    it.copy(confirmDeleteTemplateId = event.id)
                }
            TagManageUiEvent.ConfirmDeleteTemplate -> confirmDeleteTemplate()
            TagManageUiEvent.CancelDeleteTemplate ->
                editor.update {
                    it.copy(confirmDeleteTemplateId = null)
                }
        }
    }

    private fun saveTag() {
        val dialog = editor.value.tagDialog ?: return
        val name = dialog.name.trim()
        viewModelScope.launch {
            val nameTaken =
                tagRepository.getTags().any { tag ->
                    tag.name == name && tag.id != dialog.editingId
                }
            val classified = classifyTagNameSaveFailure(name, nameTaken)
            if (classified != null) {
                editor.update {
                    it.copy(
                        tagDialog =
                            dialog.copy(errorMessageResId = classified.toMessageResId()),
                    )
                }
                return@launch
            }
            editor.update { it.copy(isBusy = true) }
            runCatching {
                if (dialog.editingId == null) {
                    tagRepository.insertTag(
                        Tag(id = UUID.randomUUID().toString(), name = name),
                    )
                    SaveTagOutcome.Added
                } else {
                    val existing =
                        tagRepository.getTag(dialog.editingId)
                            ?: error("标签不存在")
                    if (existing.name == name) {
                        SaveTagOutcome.Unchanged
                    } else {
                        val count = imageRepository.countImagesWithTag(existing.name)
                        if (count > 0) {
                            SaveTagOutcome.NeedsCascadeRename(
                                tagId = existing.id,
                                oldName = existing.name,
                                newName = name,
                                imageCount = count,
                            )
                        } else {
                            performTagRename(existing.id, existing.name, name)
                            SaveTagOutcome.RenamedDirect
                        }
                    }
                }
            }.onSuccess { outcome ->
                when (outcome) {
                    SaveTagOutcome.Added,
                    SaveTagOutcome.Unchanged,
                    SaveTagOutcome.RenamedDirect,
                    -> {
                        editor.update { it.copy(tagDialog = null) }
                        _effects.send(TagManageUiEffect.ShowMessage(R.string.tag_manage_saved))
                    }
                    is SaveTagOutcome.NeedsCascadeRename ->
                        editor.update {
                            it.copy(
                                tagDialog = null,
                                cascadeRenameTag =
                                    CascadeRenameTagState(
                                        tagId = outcome.tagId,
                                        oldName = outcome.oldName,
                                        newName = outcome.newName,
                                        imageCount = outcome.imageCount,
                                    ),
                            )
                        }
                }
            }.onFailure { err ->
                val resId =
                    when {
                        err is IllegalArgumentException &&
                            err.message.orEmpty().contains("已存在") ->
                            R.string.tag_manage_name_duplicate
                        err is IllegalArgumentException &&
                            err.message.orEmpty().contains("保留字") ->
                            R.string.tag_manage_name_reserved
                        err is IllegalArgumentException &&
                            err.message.orEmpty().contains("不能为空") ->
                            R.string.tag_manage_name_empty
                        else -> R.string.error_save_failed
                    }
                editor.update { ed ->
                    ed.copy(
                        tagDialog =
                            ed.tagDialog?.copy(errorMessageResId = resId),
                    )
                }
            }
            editor.update { it.copy(isBusy = false) }
        }
    }

    private fun TagNameSaveFailure.toMessageResId(): Int =
        when (this) {
            TagNameSaveFailure.Empty -> R.string.tag_manage_name_empty
            TagNameSaveFailure.Reserved -> R.string.tag_manage_name_reserved
            TagNameSaveFailure.Duplicate -> R.string.tag_manage_name_duplicate
        }

    private fun TagNameSaveFailure.toTemplateMessageResId(): Int =
        when (this) {
            TagNameSaveFailure.Empty -> R.string.tag_manage_name_empty
            TagNameSaveFailure.Reserved -> R.string.tag_manage_name_empty
            TagNameSaveFailure.Duplicate -> R.string.tag_manage_template_name_duplicate
        }

    private fun confirmDeleteTag() {
        val id = editor.value.confirmDeleteTagId ?: return
        viewModelScope.launch {
            editor.update { it.copy(isBusy = true, confirmDeleteTagId = null) }
            runCatching {
                val tag = tagRepository.getTag(id) ?: error("标签不存在")
                val count = imageRepository.countImagesWithTag(tag.name)
                if (count == 0) {
                    tagRepository.deleteTag(id)
                    userPreferencesRepository.removeDefaultTagName(tag.name)
                    DeleteOutcome.DeletedDirect
                } else {
                    DeleteOutcome.NeedsCascade(tag.id, tag.name, count)
                }
            }.onSuccess { outcome ->
                when (outcome) {
                    DeleteOutcome.DeletedDirect ->
                        _effects.send(TagManageUiEffect.ShowMessage(R.string.tag_manage_tag_deleted))
                    is DeleteOutcome.NeedsCascade ->
                        editor.update {
                            it.copy(
                                cascadeDeleteTag =
                                    CascadeDeleteTagState(
                                        tagId = outcome.tagId,
                                        tagName = outcome.tagName,
                                        imageCount = outcome.imageCount,
                                    ),
                            )
                        }
                }
            }.onFailure {
                _effects.send(
                    TagManageUiEffect.ShowMessage(R.string.error_delete_failed),
                )
            }
            editor.update { it.copy(isBusy = false) }
        }
    }

    private suspend fun performTagRename(
        tagId: String,
        oldName: String,
        newName: String,
    ): Int {
        val exifFailures = imageRepository.renameTagInAllImages(oldName, newName)
        val tag = tagRepository.getTag(tagId) ?: error("标签不存在")
        tagRepository.updateTag(tag.copy(name = newName))
        userPreferencesRepository.renameDefaultTagName(oldName, newName)
        return exifFailures
    }

    private fun confirmCascadeRenameTag() {
        val cascade = editor.value.cascadeRenameTag ?: return
        viewModelScope.launch {
            editor.update { it.copy(isBusy = true, cascadeRenameTag = null) }
            runCatching {
                performTagRename(cascade.tagId, cascade.oldName, cascade.newName)
            }.onSuccess { exifFailures ->
                _effects.send(TagManageUiEffect.ShowMessage(R.string.tag_manage_saved))
                if (exifFailures > 0) {
                    _effects.send(
                        TagManageUiEffect.ShowMessage(
                            R.string.tag_manage_exif_partial_fail,
                            listOf(exifFailures),
                        ),
                    )
                }
            }.onFailure {
                _effects.send(
                    TagManageUiEffect.ShowMessage(R.string.error_save_failed),
                )
            }
            editor.update { it.copy(isBusy = false) }
        }
    }

    private fun confirmCascadeDeleteTag() {
        val cascade = editor.value.cascadeDeleteTag ?: return
        viewModelScope.launch {
            editor.update { it.copy(isBusy = true, cascadeDeleteTag = null) }
            runCatching {
                val exifFailures = imageRepository.removeTagFromAllImages(cascade.tagName)
                tagRepository.deleteTag(cascade.tagId)
                userPreferencesRepository.removeDefaultTagName(cascade.tagName)
                exifFailures
            }.onSuccess { exifFailures ->
                _effects.send(TagManageUiEffect.ShowMessage(R.string.tag_manage_tag_deleted))
                if (exifFailures > 0) {
                    _effects.send(
                        TagManageUiEffect.ShowMessage(
                            R.string.tag_manage_exif_partial_fail,
                            listOf(exifFailures),
                        ),
                    )
                }
            }.onFailure {
                _effects.send(
                    TagManageUiEffect.ShowMessage(R.string.error_delete_failed),
                )
            }
            editor.update { it.copy(isBusy = false) }
        }
    }

    private fun saveTemplate() {
        val dialog = editor.value.templateDialog ?: return
        val name = dialog.name.trim()
        viewModelScope.launch {
            val nameTaken =
                tagRepository.getTemplates().any { template ->
                    template.name == name && template.id != dialog.editingId
                }
            val classified = classifyTemplateNameSaveFailure(name, nameTaken)
            if (classified != null) {
                editor.update {
                    it.copy(
                        templateDialog =
                            dialog.copy(errorMessageResId = classified.toTemplateMessageResId()),
                    )
                }
                return@launch
            }
            editor.update { it.copy(isBusy = true) }
            runCatching {
                val tagNames = dialog.selectedTagNames.toList().sorted()
                if (dialog.editingId == null) {
                    tagRepository.insertTemplate(
                        TagTemplate(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            tagNames = tagNames,
                            isDefault = dialog.isDefault,
                        ),
                    )
                } else {
                    val existing =
                        tagRepository.getTemplate(dialog.editingId)
                            ?: error("模板不存在")
                    tagRepository.updateTemplate(
                        existing.copy(
                            name = name,
                            tagNames = tagNames,
                            isDefault = dialog.isDefault,
                        ),
                    )
                }
            }.onSuccess {
                editor.update { it.copy(templateDialog = null) }
                _effects.send(TagManageUiEffect.ShowMessage(R.string.tag_manage_saved))
            }.onFailure { err ->
                val resId =
                    when {
                        err is IllegalArgumentException &&
                            err.message.orEmpty().contains("已存在") ->
                            R.string.tag_manage_template_name_duplicate
                        err is IllegalArgumentException &&
                            err.message.orEmpty().contains("不能为空") ->
                            R.string.tag_manage_name_empty
                        else -> R.string.error_save_failed
                    }
                editor.update { ed ->
                    ed.copy(
                        templateDialog =
                            ed.templateDialog?.copy(errorMessageResId = resId),
                    )
                }
            }
            editor.update { it.copy(isBusy = false) }
        }
    }

    private fun setDefault(id: String) {
        viewModelScope.launch {
            editor.update { it.copy(isBusy = true) }
            runCatching { tagRepository.setDefaultTemplate(id) }
                .onSuccess {
                    _effects.send(TagManageUiEffect.ShowMessage(R.string.tag_manage_default_set))
                }.onFailure {
                    _effects.send(
                        TagManageUiEffect.ShowMessage(R.string.error_set_failed),
                    )
                }
            editor.update { it.copy(isBusy = false) }
        }
    }

    private fun confirmDeleteTemplate() {
        val id = editor.value.confirmDeleteTemplateId ?: return
        viewModelScope.launch {
            editor.update { it.copy(isBusy = true, confirmDeleteTemplateId = null) }
            runCatching { tagRepository.deleteTemplate(id) }
                .onSuccess {
                    _effects.send(TagManageUiEffect.ShowMessage(R.string.tag_manage_template_deleted))
                }.onFailure {
                    _effects.send(
                        TagManageUiEffect.ShowMessage(R.string.error_delete_failed),
                    )
                }
            editor.update { it.copy(isBusy = false) }
        }
    }

    private sealed interface DeleteOutcome {
        data object DeletedDirect : DeleteOutcome

        data class NeedsCascade(
            val tagId: String,
            val tagName: String,
            val imageCount: Int,
        ) : DeleteOutcome
    }

    private sealed interface SaveTagOutcome {
        data object Added : SaveTagOutcome

        data object Unchanged : SaveTagOutcome

        data object RenamedDirect : SaveTagOutcome

        data class NeedsCascadeRename(
            val tagId: String,
            val oldName: String,
            val newName: String,
            val imageCount: Int,
        ) : SaveTagOutcome
    }

    private data class EditorUi(
        val selectedTab: TagManageTab = TagManageTab.Tags,
        val tagDialog: TagDialogState? = null,
        val templateDialog: TemplateDialogState? = null,
        val confirmDeleteTagId: String? = null,
        val cascadeDeleteTag: CascadeDeleteTagState? = null,
        val cascadeRenameTag: CascadeRenameTagState? = null,
        val confirmDeleteTemplateId: String? = null,
        val isBusy: Boolean = false,
    )

    class Factory(
        private val tagRepository: TagRepository,
        private val imageRepository: ImageRepository,
        private val userPreferencesRepository: UserPreferencesRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TagManageViewModel(tagRepository, imageRepository, userPreferencesRepository) as T
    }
}
