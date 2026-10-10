package com.pictureorganizer.ui.main

import com.pictureorganizer.data.repository.ImageRepository
import com.pictureorganizer.data.repository.RenameTemplateRepository
import com.pictureorganizer.data.repository.UserPreferencesRepository
import com.pictureorganizer.model.ImageListItem
import com.pictureorganizer.model.ImageStatus
import com.pictureorganizer.model.TagFilterCriteria
import com.pictureorganizer.util.file.BatchRenamePlanner
import com.pictureorganizer.util.list.ListPaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val selectedTab: MainTab = MainTab.Pending,
    val items: List<ImageListItem> = emptyList(),
    val pageItems: List<ImageListItem> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val editMode: Boolean = false,
    val filter: TagFilterCriteria = TagFilterCriteria(),
    val pageIndex: Int = 0,
    val totalPages: Int = 1,
    val totalCount: Int = 0,
    val pagingEnabled: Boolean = false,
    val pageSize: Int = ListPaging.DEFAULT_PAGE_SIZE,
    val statusMessage: String? = null,
) {
    val isFilterActive: Boolean get() = filter.isActive()
    val hasSelection: Boolean get() = selectedIds.isNotEmpty()
}

class MainViewModel(
    private val images: ImageRepository,
    private val renameTemplates: RenameTemplateRepository,
    private val prefs: UserPreferencesRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observeJob: Job? = null
    private var rawItems: List<ImageListItem> = emptyList()
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            prefs.prefs.collect { p ->
                _uiState.update {
                    it.copy(
                        pagingEnabled = p.listPagingEnabled,
                        pageSize = p.listPageSize,
                    )
                }
                publish()
            }
        }
        observeTab(MainTab.Pending)
    }

    fun onTabSelected(tab: MainTab) {
        _uiState.update {
            it.copy(selectedTab = tab, selectedIds = emptySet(), editMode = false, pageIndex = 0)
        }
        observeTab(tab)
    }

    fun applyFilter(filter: TagFilterCriteria) {
        _uiState.update { it.copy(filter = filter, pageIndex = 0) }
        publish()
    }

    fun setPage(index: Int) {
        _uiState.update { it.copy(pageIndex = index) }
        publish()
    }

    fun toggleEditMode() {
        _uiState.update {
            it.copy(editMode = !it.editMode, selectedIds = if (it.editMode) emptySet() else it.selectedIds)
        }
    }

    fun toggleSelection(id: String) {
        _uiState.update {
            val next = if (id in it.selectedIds) it.selectedIds - id else it.selectedIds + id
            it.copy(selectedIds = next, editMode = next.isNotEmpty() || it.editMode)
        }
    }

    fun selectAllVisible() {
        _uiState.update { state ->
            val ids = state.pageItems.map { it.id }.toSet()
            state.copy(selectedIds = ids, editMode = ids.isNotEmpty())
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedIds = emptySet(), editMode = false) }
    }

    fun moveSelected(to: ImageStatus) {
        val state = _uiState.value
        val ids = state.selectedIds
        if (ids.isEmpty()) return
        scope.launch {
            images.moveItems(ids, state.selectedTab.toStatus(), to)
            _uiState.update { it.copy(selectedIds = emptySet(), editMode = false) }
        }
    }

    fun deleteSelected() {
        val ids = _uiState.value.selectedIds
        if (ids.isEmpty()) return
        scope.launch {
            images.deleteItems(ids)
            _uiState.update { it.copy(selectedIds = emptySet(), editMode = false) }
        }
    }

    fun emptyTrash() {
        scope.launch {
            val trash = images.getItems(ImageStatus.NoModify)
            images.deleteItems(trash.map { it.id }.toSet())
        }
    }

    fun applyDefaultRenameTemplateToSelected() {
        val ids = _uiState.value.selectedIds
        if (ids.isEmpty()) return
        scope.launch {
            val template = renameTemplates.getDefault()
            if (template == null) {
                _uiState.update { it.copy(statusMessage = "没有重命名模板") }
                return@launch
            }
            val selected = _uiState.value.items.filter { it.id in ids }
            val plan = BatchRenamePlanner.plan(template.pattern, selected)
            plan.renames.forEach { action ->
                runCatching { images.rename(action.id, action.targetFileName) }
            }
            _uiState.update {
                it.copy(
                    selectedIds = emptySet(),
                    editMode = false,
                    statusMessage =
                        "已重命名 ${plan.renames.size} 项" +
                            if (plan.collisions.isNotEmpty()) "，冲突 ${plan.collisions.size}" else "",
                )
            }
        }
    }

    fun clearStatus() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun dispose() {
        scope.cancel()
    }

    private fun observeTab(tab: MainTab) {
        observeJob?.cancel()
        observeJob =
            scope.launch {
                images.observeItems(tab.toStatus()).collect { list ->
                    rawItems = list
                    publish()
                }
            }
    }

    private fun publish() {
        val state = _uiState.value
        val filtered = state.filter.apply(rawItems)
        val page =
            ListPaging.slice(
                items = filtered,
                pagingEnabled = state.pagingEnabled,
                pageSize = state.pageSize,
                pageIndex = state.pageIndex,
            )
        _uiState.update {
            it.copy(
                items = filtered,
                pageItems = page.pageItems,
                pageIndex = page.pageIndex,
                totalPages = page.totalPages,
                totalCount = page.totalCount,
            )
        }
    }
}
