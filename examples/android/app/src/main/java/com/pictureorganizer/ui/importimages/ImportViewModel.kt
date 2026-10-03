package com.pictureorganizer.ui.importimages

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pictureorganizer.data.repository.ImageRepository
import com.pictureorganizer.data.repository.TagRepository
import com.pictureorganizer.data.repository.UserPreferencesRepository
import com.pictureorganizer.model.ImageListItem
import com.pictureorganizer.model.ImageStatus
import com.pictureorganizer.model.Tag
import com.pictureorganizer.util.file.AppFileManager
import com.pictureorganizer.util.image.ImageManager
import com.pictureorganizer.util.image.ImageTagMetadata
import com.pictureorganizer.util.image.ImportDuplicateLogic
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException

sealed interface ImportUiEffect {
    data class ImportCompleted(
        val successCount: Int,
    ) : ImportUiEffect
}

class ImportViewModel(
    private val repository: ImageRepository,
    private val imageManager: ImageManager,
    private val userPreferences: UserPreferencesRepository,
    private val tagRepository: TagRepository,
    private val fileManager: AppFileManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    private val _effects = Channel<ImportUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var pendingDuplicateDecision: CompletableDeferred<DuplicateAskDecision>? = null

    init {
        viewModelScope.launch {
            val enabled = userPreferences.isImportCompressEnabled()
            _uiState.update { it.copy(compressEnabled = enabled) }
        }
        viewModelScope.launch {
            cleanupPendingOrphans()
        }
    }

    fun setCompressEnabled(enabled: Boolean) {
        _uiState.update { it.copy(compressEnabled = enabled) }
        viewModelScope.launch {
            userPreferences.setImportCompressEnabled(enabled)
        }
    }

    fun answerDuplicate(decision: DuplicateAskDecision) {
        val deferred = pendingDuplicateDecision ?: return
        pendingDuplicateDecision = null
        _uiState.update { it.copy(duplicatePrompt = null) }
        deferred.complete(decision)
    }

    fun importUris(uris: List<Uri>) {
        if (uris.isEmpty() || _uiState.value.isImporting) return
        val compressEnabled = _uiState.value.compressEnabled
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isImporting = true,
                    current = 0,
                    total = uris.size,
                    failedItems = emptyList(),
                    duplicatePrompt = null,
                )
            }
            val failures = mutableListOf<FailedImportItem>()
            var successCount = 0
            var progress = 0

            fun bumpProgress() {
                progress++
                _uiState.update {
                    it.copy(current = progress, total = uris.size)
                }
            }

            suspend fun runOne(uri: Uri) {
                bumpProgress()
                importSingle(uri, compressEnabled)
                    .onSuccess { successCount++ }
                    .onFailure { e ->
                        failures +=
                            FailedImportItem(
                                uri = uri,
                                displayName = imageManager.displayNameOf(uri),
                                errorKind = errorKindOf(e),
                                errorDetail = errorDetailOf(e),
                            )
                    }
            }

            val askEnabled = userPreferences.isImportDuplicateAskEnabled()
            if (!askEnabled) {
                uris.forEach { runOne(it) }
            } else {
                data class NamedUri(
                    val uri: Uri,
                    val originalName: String,
                )
                val named =
                    withContext(Dispatchers.IO) {
                        uris.map { NamedUri(it, imageManager.displayNameOf(it)) }
                    }
                val libraryNormalized =
                    withContext(Dispatchers.IO) {
                        repository
                            .getStoredOriginalNames()
                            .map { ImportDuplicateLogic.normalize(it) }
                            .toSet()
                    }
                val partitioned =
                    ImportDuplicateLogic.partition(
                        candidates = named,
                        nameOf = { it.originalName },
                        libraryNormalized = libraryNormalized,
                    )
                partitioned.autoImport.forEach { runOne(it.uri) }

                var skipAsking = false
                for (item in partitioned.conflicts) {
                    if (skipAsking) {
                        runOne(item.uri)
                        continue
                    }
                    val decision = awaitDuplicateDecision(item.originalName)
                    when (decision) {
                        DuplicateAskDecision.Skip -> bumpProgress()
                        DuplicateAskDecision.Import -> runOne(item.uri)
                        DuplicateAskDecision.SkipAskingRest -> {
                            skipAsking = true
                            runOne(item.uri)
                        }
                    }
                }
            }

            _uiState.update {
                it.copy(
                    isImporting = false,
                    current = 0,
                    total = 0,
                    duplicatePrompt = null,
                )
            }
            if (failures.isEmpty()) {
                if (successCount > 0) {
                    _effects.send(ImportUiEffect.ImportCompleted(successCount))
                }
            } else {
                _uiState.update { it.copy(failedItems = failures) }
            }
        }
    }

    fun retryItem(uri: Uri) {
        if (_uiState.value.isImporting) return
        val target = _uiState.value.failedItems.firstOrNull { it.uri == uri } ?: return
        val compressEnabled = _uiState.value.compressEnabled
        viewModelScope.launch {
            _uiState.update {
                it.copy(isImporting = true, current = 1, total = 1)
            }
            importSingle(target.uri, compressEnabled)
                .onSuccess {
                    removeFailure(uri)
                }.onFailure { e ->
                    updateFailure(uri, e)
                }
            _uiState.update {
                it.copy(isImporting = false, current = 0, total = 0)
            }
            if (_uiState.value.failedItems.isEmpty()) {
                _effects.send(ImportUiEffect.ImportCompleted(1))
            }
        }
    }

    fun retryAll() {
        if (_uiState.value.isImporting) return
        val targets = _uiState.value.failedItems
        if (targets.isEmpty()) return
        val compressEnabled = _uiState.value.compressEnabled
        viewModelScope.launch {
            _uiState.update {
                it.copy(isImporting = true, current = 0, total = targets.size)
            }
            var done = 0
            var successCount = 0
            targets.forEach { item ->
                done++
                _uiState.update {
                    it.copy(current = done, total = targets.size)
                }
                importSingle(item.uri, compressEnabled)
                    .onSuccess {
                        removeFailure(item.uri)
                        successCount++
                    }.onFailure { e -> updateFailure(item.uri, e) }
            }
            _uiState.update {
                it.copy(isImporting = false, current = 0, total = 0)
            }
            if (_uiState.value.failedItems.isEmpty()) {
                _effects.send(ImportUiEffect.ImportCompleted(successCount))
            }
        }
    }

    fun dismissFailures() {
        _uiState.update { it.copy(failedItems = emptyList()) }
    }

    private suspend fun awaitDuplicateDecision(originalName: String): DuplicateAskDecision {
        val deferred = CompletableDeferred<DuplicateAskDecision>()
        pendingDuplicateDecision = deferred
        _uiState.update {
            it.copy(duplicatePrompt = DuplicatePrompt(originalName = originalName))
        }
        return deferred.await()
    }

    private fun removeFailure(uri: Uri) {
        _uiState.update { state ->
            state.copy(failedItems = state.failedItems.filterNot { it.uri == uri })
        }
    }

    private fun updateFailure(
        uri: Uri,
        e: Throwable,
    ) {
        _uiState.update { state ->
            state.copy(
                failedItems =
                    state.failedItems.map {
                        if (it.uri == uri) {
                            it.copy(
                                errorKind = errorKindOf(e),
                                errorDetail = errorDetailOf(e),
                            )
                        } else {
                            it
                        }
                    },
            )
        }
    }

    private suspend fun cleanupPendingOrphans() {
        withContext(Dispatchers.IO) {
            val knownPaths =
                ImageStatus.entries
                    .flatMap { status -> repository.getItems(status) }
                    .map { it.filePath }
            fileManager.cleanupPendingOrphans(knownPaths)
        }
    }

    private suspend fun importSingle(
        uri: Uri,
        compressEnabled: Boolean,
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val originalName = imageManager.displayNameOf(uri)

                val exifTags = imageManager.readUserTagsFromUri(uri)

                val dateTakenMillis = imageManager.readDateTakenMillisFromUri(uri)
                val prepared = imageManager.prepareForImport(uri, compressEnabled)
                val now = System.currentTimeMillis()
                val defaultTags = userPreferences.getDefaultTagNames().toList()
                val mergedTags = ImageTagMetadata.mergeImportTags(defaultTags, exifTags)
                val item =
                    ImageListItem(
                        id = prepared.id,
                        description = prepared.fileName,
                        status = ImageStatus.Pending,
                        tags = mergedTags,
                        originalName = originalName,
                        dateTakenMillis = dateTakenMillis,
                    )
                repository.insert(
                    item = item,
                    filePath = prepared.filePath,
                    fileName = prepared.fileName,
                    importedAt = now,
                )
                // 压缩/重编码会剥 Exif；回写可迁移副本，便于再导入（Bug16 / F7 遗留扩展）
                ImageTagMetadata.writeImportMetadata(
                    file = fileManager.absoluteFile(prepared.filePath),
                    tags = mergedTags,
                    dateTakenMillis = dateTakenMillis,
                )

                ensureTagsInLibrary(mergedTags)
            }
        }

    private suspend fun ensureTagsInLibrary(names: List<String>) {
        if (names.isEmpty()) return
        val existing = tagRepository.getTags().map { it.name }.toHashSet()
        names.forEach { name ->
            val trimmed = name.trim()
            if (trimmed.isEmpty() ||
                ImageListItem.isReservedStatusName(trimmed) ||
                trimmed in existing
            ) {
                return@forEach
            }
            runCatching {
                tagRepository.insertTag(Tag(id = "", name = trimmed))
                existing.add(trimmed)
            }
        }
    }

    private fun errorKindOf(e: Throwable): ImportErrorKind =
        when (e) {
            is SecurityException, is FileNotFoundException, is IOException -> ImportErrorKind.ReadFailed
            is IllegalStateException -> ImportErrorKind.DecodeFailed
            else -> ImportErrorKind.Unknown
        }

    private fun errorDetailOf(e: Throwable): String? = e.message?.takeIf { it.isNotBlank() } ?: e::class.java.simpleName

    class Factory(
        private val repository: ImageRepository,
        private val imageManager: ImageManager,
        private val userPreferences: UserPreferencesRepository,
        private val tagRepository: TagRepository,
        private val fileManager: AppFileManager,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ImportViewModel(
                repository,
                imageManager,
                userPreferences,
                tagRepository,
                fileManager,
            ) as T
    }
}
