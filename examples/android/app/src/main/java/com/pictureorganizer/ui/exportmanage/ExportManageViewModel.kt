package com.pictureorganizer.ui.exportmanage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pictureorganizer.R
import com.pictureorganizer.util.file.AppFileManager
import com.pictureorganizer.util.file.ExportZipNames
import com.pictureorganizer.util.file.ZipRenamePlan
import com.pictureorganizer.util.file.ZipStemError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ExportManageViewModel(
    private val fileManager: AppFileManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExportManageUiState())
    val uiState: StateFlow<ExportManageUiState> = _uiState.asStateFlow()

    private val _effects = Channel<ExportManageUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        refresh()
    }

    fun onEvent(event: ExportManageUiEvent) {
        when (event) {
            ExportManageUiEvent.Refresh -> refresh()
            is ExportManageUiEvent.Share -> share(event.absolutePath)
            is ExportManageUiEvent.RequestDelete ->
                _uiState.update {
                    it.copy(confirmDeletePath = event.absolutePath, renamePath = null)
                }
            ExportManageUiEvent.ConfirmDelete -> confirmDelete()
            ExportManageUiEvent.CancelDelete ->
                _uiState.update { it.copy(confirmDeletePath = null) }
            is ExportManageUiEvent.RequestRename -> requestRename(event.absolutePath)
            is ExportManageUiEvent.RenameDraftChanged ->
                _uiState.update { it.copy(renameStemDraft = event.stem) }
            ExportManageUiEvent.ConfirmRename -> confirmRename()
            ExportManageUiEvent.CancelRename ->
                _uiState.update { it.copy(renamePath = null, renameStemDraft = "") }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val files =
                withContext(Dispatchers.IO) {
                    listExportedZips(fileManager.exportsDir())
                }
            _uiState.update {
                it.copy(
                    files = files,
                    isLoading = false,
                    confirmDeletePath =
                        it.confirmDeletePath?.takeIf { path ->
                            files.any { item -> item.absolutePath == path }
                        },
                    renamePath =
                        it.renamePath?.takeIf { path ->
                            files.any { item -> item.absolutePath == path }
                        },
                )
            }
        }
    }

    private fun share(absolutePath: String) {
        viewModelScope.launch {
            val file = File(absolutePath)
            if (!file.isFile || !isUnderExports(file)) {
                _effects.send(ExportManageUiEffect.ShowMessage(R.string.export_manage_missing))
                refresh()
                return@launch
            }
            _effects.send(
                ExportManageUiEffect.ShareFile(
                    absolutePath = file.absolutePath,
                    fileName = file.name,
                ),
            )
        }
    }

    private fun requestRename(absolutePath: String) {
        val item = _uiState.value.files.find { it.absolutePath == absolutePath } ?: return
        _uiState.update {
            it.copy(
                renamePath = absolutePath,
                renameStemDraft = ExportZipNames.normalizeZipStem(item.fileName),
                confirmDeletePath = null,
            )
        }
    }

    private fun confirmDelete() {
        val path = _uiState.value.confirmDeletePath ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, confirmDeletePath = null) }
            val ok =
                withContext(Dispatchers.IO) {
                    deleteExportZip(path)
                }
            if (ok) {
                _effects.send(ExportManageUiEffect.ShowMessage(R.string.export_manage_deleted))
            } else {
                _effects.send(ExportManageUiEffect.ShowMessage(R.string.error_delete_failed))
            }
            _uiState.update { it.copy(isBusy = false) }
            refresh()
        }
    }

    private fun confirmRename() {
        val path = _uiState.value.renamePath ?: return
        val draft = _uiState.value.renameStemDraft
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            val result =
                withContext(Dispatchers.IO) {
                    renameExportZip(path, draft)
                }
            when (result) {
                RenameResult.Ok -> {
                    _uiState.update { it.copy(renamePath = null, renameStemDraft = "", isBusy = false) }
                    _effects.send(ExportManageUiEffect.ShowMessage(R.string.export_manage_renamed))
                    refresh()
                }
                RenameResult.Unchanged -> {
                    _uiState.update { it.copy(renamePath = null, renameStemDraft = "", isBusy = false) }
                }
                is RenameResult.Message -> {
                    _uiState.update { it.copy(isBusy = false) }
                    _effects.send(ExportManageUiEffect.ShowMessage(result.messageResId))
                    if (result.refresh) refresh()
                }
            }
        }
    }

    private fun deleteExportZip(absolutePath: String): Boolean {
        val file = File(absolutePath)
        if (!file.isFile || !isUnderExports(file)) return false
        return file.delete()
    }

    private fun renameExportZip(
        absolutePath: String,
        rawStem: String,
    ): RenameResult {
        val source = File(absolutePath)
        if (!source.isFile || !isUnderExports(source)) {
            return RenameResult.Message(R.string.export_manage_missing, refresh = true)
        }
        val existing =
            fileManager
                .exportsDir()
                .listFiles()
                .orEmpty()
                .filter { it.isFile }
                .map { it.name }
                .toSet()
        return when (
            val plan =
                ExportZipNames.planZipRename(
                    currentFileName = source.name,
                    rawStem = rawStem,
                    existingFileNames = existing,
                )
        ) {
            is ZipRenamePlan.Invalid ->
                RenameResult.Message(
                    when (plan.error) {
                        ZipStemError.Empty -> R.string.export_zip_name_empty
                        ZipStemError.IllegalChars -> R.string.export_zip_name_illegal
                    },
                )
            ZipRenamePlan.Collision ->
                RenameResult.Message(R.string.export_manage_name_collision)
            ZipRenamePlan.Unchanged -> RenameResult.Unchanged
            is ZipRenamePlan.Ready -> {
                val dest = File(source.parentFile, plan.newFileName)
                if (!source.renameTo(dest)) {
                    RenameResult.Message(R.string.export_manage_rename_failed)
                } else {
                    RenameResult.Ok
                }
            }
        }
    }

    private fun isUnderExports(file: File): Boolean {
        val exports = fileManager.exportsDir().canonicalFile
        val target = runCatching { file.canonicalFile }.getOrNull() ?: return false
        return target.path.startsWith(exports.path + File.separator) || target.path == exports.path
    }

    private sealed interface RenameResult {
        data object Ok : RenameResult

        data object Unchanged : RenameResult

        data class Message(
            val messageResId: Int,
            val refresh: Boolean = false,
        ) : RenameResult
    }

    class Factory(
        private val fileManager: AppFileManager,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ExportManageViewModel(fileManager) as T
    }
}

internal fun listExportedZips(exportsDir: File): List<ExportedZipItem> {
    if (!exportsDir.isDirectory) return emptyList()
    return exportsDir
        .listFiles { file -> file.isFile && file.extension.equals("zip", ignoreCase = true) }
        .orEmpty()
        .map { file ->
            ExportedZipItem(
                fileName = file.name,
                absolutePath = file.absolutePath,
                sizeBytes = file.length(),
                lastModifiedMillis = file.lastModified(),
            )
        }.sortedByDescending { it.lastModifiedMillis }
}
