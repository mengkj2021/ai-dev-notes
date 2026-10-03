package com.pictureorganizer.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pictureorganizer.R
import com.pictureorganizer.data.repository.ImageRepository
import com.pictureorganizer.data.repository.RenameTemplateRepository
import com.pictureorganizer.data.repository.TagRepository
import com.pictureorganizer.data.repository.UserPreferencesRepository
import com.pictureorganizer.model.ImageListItem
import com.pictureorganizer.model.ImageListSort
import com.pictureorganizer.model.ImageStatus
import com.pictureorganizer.model.RenameTemplate
import com.pictureorganizer.model.TagFilterCriteria
import com.pictureorganizer.model.matchesFilter
import com.pictureorganizer.model.sortedByFilter
import com.pictureorganizer.util.file.BatchRenamePlanner
import com.pictureorganizer.util.file.RenamePatternApplier
import com.pictureorganizer.util.list.ListPaging
import com.pictureorganizer.util.log.AppLog
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: ImageRepository,
    private val tagRepository: TagRepository,
    private val renameTemplateRepository: RenameTemplateRepository,
    private val userPreferences: UserPreferencesRepository,
) : ViewModel() {
    private val _effects = Channel<MainUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val listUiState = MutableStateFlow(ListUiState())
    private val batchRenameState = MutableStateFlow(BatchRenameUi())

    val uiState: StateFlow<MainUiState> =
        combine(
            repository.observeItems(ImageStatus.Pending),
            repository.observeItems(ImageStatus.Confirmed),
            repository.observeItems(ImageStatus.NoModify),
            tagRepository.observeTags(),
            combine(
                listUiState,
                renameTemplateRepository.observeAll(),
                batchRenameState,
                combine(
                    userPreferences.listPagingEnabled,
                    userPreferences.listPageSize,
                ) { enabled, size -> enabled to size },
            ) { list, templates, batch, paging ->
                MainCombineExtras(
                    list = list,
                    templates = templates,
                    batch = batch,
                    pagingEnabled = paging.first,
                    pageSize = paging.second,
                )
            },
        ) { pending, confirmed, noModify, tags, extras ->
            val tabFilter = extras.list.filterFor(extras.list.selectedTab)
            val source =
                when (extras.list.selectedTab) {
                    MainTab.Pending -> pending
                    MainTab.Confirmed -> confirmed
                    MainTab.NoModify -> noModify
                }
            val criteria =
                TagFilterCriteria(
                    selectedTagNames = tabFilter.selectedTagNames,
                    includeUntagged = tabFilter.includeUntagged,
                    nameContains = tabFilter.nameContains,
                    sort = tabFilter.sort,
                    dateTakenFromEpochDay = tabFilter.dateTakenFromEpochDay,
                    dateTakenToEpochDay = tabFilter.dateTakenToEpochDay,
                    importedAtFromEpochDay = tabFilter.importedAtFromEpochDay,
                    importedAtToEpochDay = tabFilter.importedAtToEpochDay,
                )
            val filtered =
                source
                    .filter { it.matchesFilter(criteria) }
                    .sortedByFilter(criteria.sort)
            val page =
                ListPaging.slice(
                    items = filtered,
                    pagingEnabled = extras.pagingEnabled,
                    pageSize = extras.pageSize,
                    pageIndex = tabFilter.pageIndex,
                )

            MainUiState(
                selectedTab = extras.list.selectedTab,
                isEditMode = extras.list.isEditMode,
                selectedIds = extras.list.selectedIds,
                availableTags = tags.map { it.name },
                filter = criteria,
                pageItems = page.pageItems,
                pageIndex = page.pageIndex,
                totalCount = page.totalCount,
                totalPages = page.totalPages,
                pagingEnabled = page.pagingEnabled,
                renameTemplates = extras.templates,
                renameFailures = extras.batch.failures,
                isBatchRenaming = extras.batch.isBusy,
                trashItemCount = noModify.size,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainUiState(),
        )

    init {
        viewModelScope.launch {
            combine(
                userPreferences.listPagingEnabled,
                userPreferences.listPageSize,
            ) { enabled, size -> enabled to size }
                .distinctUntilChanged()
                .drop(1)
                .collect { resetAllPageIndices() }
        }
    }

    fun onEvent(event: MainUiEvent) {
        when (event) {
            is MainUiEvent.SelectTab -> selectTab(event.tab)
            MainUiEvent.ToggleEditMode -> toggleEditMode()
            MainUiEvent.SelectAll -> selectAll()
            is MainUiEvent.ToggleSelect -> toggleSelect(event.id)
            is MainUiEvent.MoveSelectedTo -> moveSelectedTo(event.status)
            is MainUiEvent.ApplyRenameTemplate -> applyRenameTemplate(event.templateId)
            is MainUiEvent.RetryRenameFailure -> retryRenameFailure(event.id)
            MainUiEvent.RetryAllRenameFailures -> retryAllRenameFailures()
            MainUiEvent.DismissRenameFailures -> dismissRenameFailures()
            MainUiEvent.ImportImages -> navigateToImport()
            MainUiEvent.DeleteSelected -> deleteSelected()
            MainUiEvent.EmptyTrash -> emptyTrash()
            is MainUiEvent.SetTagFilter -> setTagFilter(event.criteria, event.targetTab)
            MainUiEvent.ClearTagFilter -> clearTagFilter()
            MainUiEvent.PrevPage -> changePage(-1)
            MainUiEvent.NextPage -> changePage(1)
        }
    }

    private fun selectTab(tab: MainTab) {
        val from = listUiState.value.selectedTab
        AppLog.d("Main", "SelectTab $from -> $tab")
        listUiState.value =
            listUiState.value.copy(
                selectedTab = tab,
                isEditMode = false,
                selectedIds = emptySet(),
            )
        dismissRenameFailures()
    }

    private fun toggleEditMode() {
        val current = listUiState.value
        listUiState.value =
            if (current.isEditMode) {
                current.copy(isEditMode = false, selectedIds = emptySet())
            } else {
                current.copy(isEditMode = true)
            }
    }

    private fun selectAll() {
        val state = uiState.value
        if (!state.isEditMode) return
        listUiState.value =
            listUiState.value.copy(
                selectedIds = state.pageItems.map { it.id }.toSet(),
            )
    }

    private fun toggleSelect(id: String) {
        val current = listUiState.value
        if (!current.isEditMode) return
        val newIds =
            if (id in current.selectedIds) {
                current.selectedIds - id
            } else {
                current.selectedIds + id
            }
        listUiState.value = current.copy(selectedIds = newIds)
    }

    private fun setTagFilter(
        criteria: TagFilterCriteria,
        targetTab: MainTab? = null,
    ) {
        val current = listUiState.value
        val tab = targetTab ?: current.selectedTab
        AppLog.d(
            "Main",
            "SetTagFilter target=$tab (param=$targetTab selected=${current.selectedTab}) " +
                "active=${criteria.isActive} tags=${criteria.selectedTagNames.size} " +
                "untagged=${criteria.includeUntagged} qBlank=${criteria.nameContains.isBlank()} " +
                "dateTaken=${criteria.hasDateTakenRange} importedAt=${criteria.hasImportedAtRange}",
        )
        listUiState.value =
            current
                .withFilter(
                    tab,
                    TabFilterState(
                        selectedTagNames = criteria.selectedTagNames,
                        includeUntagged = criteria.includeUntagged,
                        nameContains = criteria.nameContains,
                        sort = criteria.sort,
                        dateTakenFromEpochDay = criteria.dateTakenFromEpochDay,
                        dateTakenToEpochDay = criteria.dateTakenToEpochDay,
                        importedAtFromEpochDay = criteria.importedAtFromEpochDay,
                        importedAtToEpochDay = criteria.importedAtToEpochDay,
                        pageIndex = 0,
                    ),
                ).copy(selectedIds = emptySet())
    }

    private fun clearTagFilter() {
        setTagFilter(TagFilterCriteria())
    }

    private fun changePage(delta: Int) {
        val state = uiState.value
        if (!state.pagingEnabled) return
        val newIndex = (state.pageIndex + delta).coerceIn(0, state.totalPages - 1)
        if (newIndex == state.pageIndex) return
        val current = listUiState.value
        val filter = current.filterFor(current.selectedTab).copy(pageIndex = newIndex)
        listUiState.value =
            current
                .withFilter(current.selectedTab, filter)
                .copy(selectedIds = emptySet())
    }

    private fun resetAllPageIndices() {
        val current = listUiState.value
        listUiState.value =
            current.copy(
                pendingFilter = current.pendingFilter.copy(pageIndex = 0),
                confirmedFilter = current.confirmedFilter.copy(pageIndex = 0),
                noModifyFilter = current.noModifyFilter.copy(pageIndex = 0),
                selectedIds = emptySet(),
            )
    }

    private fun moveSelectedTo(targetStatus: ImageStatus) {
        val state = uiState.value
        if (!state.isEditMode || state.selectedIds.isEmpty()) return
        val fromStatus = statusForTab(state.selectedTab)
        if (fromStatus == targetStatus) return
        val ids = state.selectedIds
        viewModelScope.launch {
            repository.moveItems(ids, fromStatus, targetStatus)
            listUiState.value = listUiState.value.copy(selectedIds = emptySet())
        }
    }

    private fun applyRenameTemplate(templateId: String) {
        val state = uiState.value
        if (!state.isEditMode || state.selectedIds.isEmpty() || state.isBatchRenaming) return
        val template = state.renameTemplates.find { it.id == templateId } ?: return
        val ids = state.selectedIds
        val fromStatus = statusForTab(state.selectedTab)
        viewModelScope.launch {
            batchRenameState.update { it.copy(isBusy = true, lastTemplateId = templateId) }
            val selected =
                repository
                    .getItems(fromStatus)
                    .filter { it.id in ids }
            val failures = renameBatch(template, selected)
            listUiState.value = listUiState.value.copy(selectedIds = emptySet())
            batchRenameState.update {
                it.copy(isBusy = false, failures = failures, lastTemplateId = templateId)
            }
            if (failures.isEmpty()) {
                _effects.send(MainUiEffect.ShowSnackbar(R.string.batch_rename_ok))
            }
        }
    }

    private fun retryRenameFailure(id: String) {
        val batch = batchRenameState.value
        if (batch.isBusy) return
        val templateId = batch.lastTemplateId ?: return
        val failure = batch.failures.find { it.id == id } ?: return
        viewModelScope.launch {
            val template = renameTemplateRepository.getById(templateId) ?: return@launch
            batchRenameState.update { it.copy(isBusy = true) }
            val item = repository.getItem(failure.id)
            val remaining =
                if (item == null) {
                    batch.failures.map {
                        if (it.id == id) {
                            failure.copy(
                                reasonResId = R.string.error_image_missing,
                                reasonDetail = null,
                            )
                        } else {
                            it
                        }
                    }
                } else {
                    val result = renameOne(template, item)
                    if (result == null) {
                        batch.failures.filterNot { it.id == id }
                    } else {
                        batch.failures.map { if (it.id == id) result else it }
                    }
                }
            batchRenameState.update { it.copy(isBusy = false, failures = remaining) }
            if (remaining.isEmpty()) {
                _effects.send(MainUiEffect.ShowSnackbar(R.string.batch_rename_ok))
            }
        }
    }

    private fun retryAllRenameFailures() {
        val batch = batchRenameState.value
        if (batch.isBusy || batch.failures.isEmpty()) return
        val templateId = batch.lastTemplateId ?: return
        val ids = batch.failures.map { it.id }
        viewModelScope.launch {
            val template = renameTemplateRepository.getById(templateId) ?: return@launch
            batchRenameState.update { it.copy(isBusy = true) }
            val items = ids.mapNotNull { repository.getItem(it) }
            val failures = renameBatch(template, items)
            batchRenameState.update {
                it.copy(isBusy = false, failures = failures, lastTemplateId = templateId)
            }
            if (failures.isEmpty()) {
                _effects.send(MainUiEffect.ShowSnackbar(R.string.batch_rename_ok))
            }
        }
    }

    private fun dismissRenameFailures() {
        batchRenameState.update { it.copy(failures = emptyList()) }
    }

    private suspend fun renameBatch(
        template: RenameTemplate,
        items: List<ImageListItem>,
    ): List<BatchRenameFailure> {
        val plan = BatchRenamePlanner.plan(template.pattern, items)
        val failures =
            plan.collisions
                .map {
                    BatchRenameFailure(
                        id = it.id,
                        displayName = it.displayName,
                        reasonResId = R.string.batch_rename_error_collision,
                        reasonDetail = it.targetFileName,
                    )
                }.toMutableList()
        for (action in plan.renames) {
            val item = items.find { it.id == action.id } ?: continue
            renameOne(template, item, action.targetFileName)?.let { failures += it }
        }
        return failures
    }

    private suspend fun renameOne(
        template: RenameTemplate,
        item: ImageListItem,
        plannedTarget: String? = null,
    ): BatchRenameFailure? {
        val current = BatchRenamePlanner.fileNameOf(item)
        val target =
            plannedTarget
                ?: RenamePatternApplier.applyForItem(template.pattern, item, current)
        if (target == current) return null
        return runCatching { repository.rename(item.id, target) }
            .fold(
                onSuccess = { null },
                onFailure = { e ->
                    BatchRenameFailure(
                        id = item.id,
                        displayName = current,
                        reasonResId = R.string.error_rename_failed,
                        reasonDetail = e.message,
                    )
                },
            )
    }

    private fun deleteSelected() {
        val state = uiState.value
        if (!state.isEditMode || state.selectedIds.isEmpty()) return
        if (state.selectedTab != MainTab.NoModify) return
        val ids = state.selectedIds
        viewModelScope.launch {
            repository.deleteItems(ids)
            listUiState.value =
                listUiState.value.copy(
                    isEditMode = false,
                    selectedIds = emptySet(),
                )
        }
    }

    private fun emptyTrash() {
        if (uiState.value.selectedTab != MainTab.NoModify) return
        viewModelScope.launch {
            val ids =
                repository
                    .observeItems(ImageStatus.NoModify)
                    .first()
                    .map { it.id }
                    .toSet()
            if (ids.isEmpty()) return@launch
            repository.deleteItems(ids)
            listUiState.value =
                listUiState.value.copy(
                    isEditMode = false,
                    selectedIds = emptySet(),
                )
        }
    }

    private fun navigateToImport() {
        viewModelScope.launch {
            _effects.send(MainUiEffect.NavigateToImport)
        }
    }

    private fun statusForTab(tab: MainTab): ImageStatus =
        when (tab) {
            MainTab.Pending -> ImageStatus.Pending
            MainTab.Confirmed -> ImageStatus.Confirmed
            MainTab.NoModify -> ImageStatus.NoModify
        }

    private data class MainCombineExtras(
        val list: ListUiState,
        val templates: List<RenameTemplate>,
        val batch: BatchRenameUi,
        val pagingEnabled: Boolean,
        val pageSize: Int,
    )

    private data class BatchRenameUi(
        val failures: List<BatchRenameFailure> = emptyList(),
        val lastTemplateId: String? = null,
        val isBusy: Boolean = false,
    )

    private data class TabFilterState(
        val selectedTagNames: Set<String> = emptySet(),
        val includeUntagged: Boolean = false,
        val nameContains: String = "",
        val sort: ImageListSort = ImageListSort.ImportedAtDesc,
        val dateTakenFromEpochDay: Long? = null,
        val dateTakenToEpochDay: Long? = null,
        val importedAtFromEpochDay: Long? = null,
        val importedAtToEpochDay: Long? = null,
        val pageIndex: Int = 0,
    )

    private data class ListUiState(
        val selectedTab: MainTab = MainTab.Pending,
        val isEditMode: Boolean = false,
        val selectedIds: Set<String> = emptySet(),
        val pendingFilter: TabFilterState = TabFilterState(),
        val confirmedFilter: TabFilterState = TabFilterState(),
        val noModifyFilter: TabFilterState = TabFilterState(),
    ) {
        fun filterFor(tab: MainTab): TabFilterState =
            when (tab) {
                MainTab.Pending -> pendingFilter
                MainTab.Confirmed -> confirmedFilter
                MainTab.NoModify -> noModifyFilter
            }

        fun withFilter(
            tab: MainTab,
            filter: TabFilterState,
        ): ListUiState =
            when (tab) {
                MainTab.Pending -> copy(pendingFilter = filter)
                MainTab.Confirmed -> copy(confirmedFilter = filter)
                MainTab.NoModify -> copy(noModifyFilter = filter)
            }
    }

    class Factory(
        private val repository: ImageRepository,
        private val tagRepository: TagRepository,
        private val renameTemplateRepository: RenameTemplateRepository,
        private val userPreferences: UserPreferencesRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MainViewModel(repository, tagRepository, renameTemplateRepository, userPreferences) as T
    }
}
