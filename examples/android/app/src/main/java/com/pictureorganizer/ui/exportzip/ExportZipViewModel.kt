package com.pictureorganizer.ui.exportzip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pictureorganizer.R
import com.pictureorganizer.data.repository.ImageRepository
import com.pictureorganizer.model.ImageStatus
import com.pictureorganizer.model.SINGLE_EXPORT_PACK_LABEL
import com.pictureorganizer.model.TagFilterCriteria
import com.pictureorganizer.model.buildSingleExportPack
import com.pictureorganizer.model.excludingIds
import com.pictureorganizer.model.matchesFilter
import com.pictureorganizer.model.sortedByFilter
import com.pictureorganizer.util.file.AppFileManager
import com.pictureorganizer.util.file.ExportZipNames
import com.pictureorganizer.util.file.ZipExporter
import com.pictureorganizer.util.file.ZipNamePlan
import com.pictureorganizer.util.file.ZipStemError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExportZipViewModel(
    private val imageRepository: ImageRepository,
    private val fileManager: AppFileManager,
) : ViewModel() {
    private val filterState = MutableStateFlow(TagFilterCriteria())
    private val session = MutableStateFlow(SessionState())

    private val _effects = Channel<ExportZipUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    val uiState: StateFlow<ExportZipUiState> =
        combine(
            imageRepository.observeItems(ImageStatus.Confirmed),
            filterState,
            session,
        ) { items, filter, sess ->
            val remaining =
                items
                    .filter { it.matchesFilter(filter) }
                    .sortedByFilter(filter.sort)
                    .excludingIds(sess.excludedIds)
            val packItems = buildSingleExportPack(remaining)
            val dateToken = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
            val plannedPacks =
                if (packItems.isEmpty()) {
                    emptyList()
                } else {
                    listOf(
                        ExportZipPackDraft(
                            label = SINGLE_EXPORT_PACK_LABEL,
                            stem =
                                sess.stemOverrides[SINGLE_EXPORT_PACK_LABEL]
                                    ?: ExportZipNames.defaultSingleZipStem(dateToken),
                        ),
                    )
                }
            ExportZipUiState(
                filter = filter,
                confirmedCount = items.size,
                matchedCount = remaining.size,
                previewItems = remaining,
                isExporting = sess.isExporting,
                progressCurrent = sess.progressCurrent,
                progressTotal = sess.progressTotal,
                results = sess.results,
                errorMessage = sess.errorMessage,
                plannedPacks = plannedPacks,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExportZipUiState(),
        )

    fun onEvent(event: ExportZipUiEvent) {
        when (event) {
            is ExportZipUiEvent.SetFilter -> {
                filterState.value = event.filter
                session.update {
                    it.copy(
                        results = emptyList(),
                        errorMessage = null,
                        stemOverrides = emptyMap(),
                    )
                }
            }
            ExportZipUiEvent.ClearFilter -> {
                filterState.value = TagFilterCriteria()
                session.update {
                    it.copy(
                        results = emptyList(),
                        errorMessage = null,
                        stemOverrides = emptyMap(),
                    )
                }
            }
            ExportZipUiEvent.StartExport -> startExport()
            is ExportZipUiEvent.ExcludeImage -> {
                if (!session.value.isExporting) {
                    session.update { it.copy(excludedIds = it.excludedIds + event.imageId) }
                }
            }
            is ExportZipUiEvent.ZipStemChanged -> {
                if (!session.value.isExporting) {
                    session.update {
                        it.copy(
                            stemOverrides = it.stemOverrides + (event.label to event.stem),
                        )
                    }
                }
            }
            ExportZipUiEvent.ClearError -> session.update { it.copy(errorMessage = null) }
        }
    }

    fun requestShare(result: ExportZipResult) {
        viewModelScope.launch {
            _effects.send(ExportZipUiEffect.ShareFile(result.absolutePath, result.fileName))
        }
    }

    private fun startExport() {
        if (session.value.isExporting) return
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val all = imageRepository.getItems(ImageStatus.Confirmed)
                    val filter = filterState.value
                    val excludedIds = session.value.excludedIds
                    val remaining =
                        all
                            .filter { it.matchesFilter(filter) }
                            .sortedByFilter(filter.sort)
                            .excludingIds(excludedIds)
                    val packItems = buildSingleExportPack(remaining)
                    if (packItems.isEmpty()) {
                        error(fileManager.getString(R.string.error_export_no_images))
                    }
                    val dateToken = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                    val overrides = session.value.stemOverrides
                    val stem =
                        overrides[SINGLE_EXPORT_PACK_LABEL]
                            ?: ExportZipNames.defaultSingleZipStem(dateToken)
                    val existing =
                        fileManager
                            .exportsDir()
                            .list()
                            ?.toSet()
                            .orEmpty()
                    val zipName =
                        when (
                            val plan =
                                ExportZipNames.planZipFileNames(
                                    listOf(SINGLE_EXPORT_PACK_LABEL to stem),
                                    existing,
                                )
                        ) {
                            is ZipNamePlan.InvalidStem ->
                                error(zipStemErrorMessage(plan.error))
                            ZipNamePlan.DuplicateStems ->
                                error(fileManager.getString(R.string.export_zip_name_collision))
                            is ZipNamePlan.Ready -> plan.fileNamesByLabel.getValue(SINGLE_EXPORT_PACK_LABEL)
                        }
                    session.update {
                        it.copy(
                            isExporting = true,
                            progressCurrent = 0,
                            progressTotal = 1,
                            results = emptyList(),
                            errorMessage = null,
                        )
                    }
                    val zipFile = File(fileManager.exportsDir(), zipName)
                    val entries =
                        packItems.map { item ->
                            val name =
                                item.filePath.substringAfterLast('/').ifBlank {
                                    item.description
                                }
                            name to fileManager.absoluteFile(item.filePath)
                        }
                    session.update { it.copy(progressCurrent = 1) }
                    val count = ZipExporter.zipFiles(zipFile, entries)
                    if (count == 0) {
                        zipFile.delete()
                        error(fileManager.getString(R.string.error_export_empty))
                    }
                    listOf(
                        ExportZipResult(
                            label = fileManager.getString(R.string.export_zip_single_result_label),
                            fileName = zipName,
                            absolutePath = zipFile.absolutePath,
                            entryCount = count,
                        ),
                    )
                }
            }.onSuccess { results ->
                session.update {
                    it.copy(isExporting = false, results = results, progressCurrent = 0)
                }
                _effects.send(ExportZipUiEffect.ShowMessage(R.string.export_zip_done))
            }.onFailure { e ->
                val fallback = fileManager.getString(R.string.error_export_failed)
                session.update {
                    it.copy(
                        isExporting = false,
                        errorMessage = e.message ?: fallback,
                        progressCurrent = 0,
                    )
                }
                _effects.send(
                    ExportZipUiEffect.ShowMessageText(e.message ?: fallback),
                )
            }
        }
    }

    private fun zipStemErrorMessage(error: ZipStemError): String =
        when (error) {
            ZipStemError.Empty -> fileManager.getString(R.string.export_zip_name_empty)
            ZipStemError.IllegalChars -> fileManager.getString(R.string.export_zip_name_illegal)
        }

    private data class SessionState(
        val isExporting: Boolean = false,
        val progressCurrent: Int = 0,
        val progressTotal: Int = 0,
        val results: List<ExportZipResult> = emptyList(),
        val errorMessage: String? = null,
        val excludedIds: Set<String> = emptySet(),
        val stemOverrides: Map<String, String> = emptyMap(),
    )

    class Factory(
        private val imageRepository: ImageRepository,
        private val fileManager: AppFileManager,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ExportZipViewModel(imageRepository, fileManager) as T
    }
}
