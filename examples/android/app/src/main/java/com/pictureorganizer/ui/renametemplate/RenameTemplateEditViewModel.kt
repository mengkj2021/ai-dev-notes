package com.pictureorganizer.ui.renametemplate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pictureorganizer.R
import com.pictureorganizer.data.repository.RenameTemplateRepository
import com.pictureorganizer.model.RenameTemplate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RenameTemplateEditViewModel(
    private val repository: RenameTemplateRepository,
    private val templateId: String?,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RenameTemplateEditUiState(isLoading = templateId != null))
    val uiState: StateFlow<RenameTemplateEditUiState> = _uiState.asStateFlow()

    private val _effects = Channel<RenameTemplateEditUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        if (templateId != null) {
            viewModelScope.launch {
                val existing = repository.getById(templateId)
                if (existing == null) {
                    _effects.send(RenameTemplateEditUiEffect.NotFound)
                    _uiState.update { it.copy(isLoading = false) }
                } else {
                    _uiState.update {
                        it.copy(
                            editingId = existing.id,
                            name = existing.name,
                            pattern = existing.pattern,
                            isDefault = existing.isDefault,
                            isLoading = false,
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: RenameTemplateEditUiEvent) {
        when (event) {
            is RenameTemplateEditUiEvent.NameChanged ->
                _uiState.update {
                    it.copy(name = event.value, errorMessageResId = null)
                }
            is RenameTemplateEditUiEvent.PatternChanged ->
                _uiState.update {
                    it.copy(pattern = event.value, errorMessageResId = null)
                }
            is RenameTemplateEditUiEvent.DefaultChanged ->
                _uiState.update { it.copy(isDefault = event.value) }
            RenameTemplateEditUiEvent.Save -> save()
        }
    }

    private fun save() {
        val state = _uiState.value
        if (state.isBusy || state.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            runCatching {
                if (state.editingId == null) {
                    repository.insert(
                        RenameTemplate(
                            id = "",
                            name = state.name,
                            pattern = state.pattern,
                            isDefault = state.isDefault,
                        ),
                    )
                } else {
                    val existing =
                        repository.getById(state.editingId)
                            ?: error("模板不存在")
                    repository.update(
                        existing.copy(
                            name = state.name,
                            pattern = state.pattern,
                            isDefault = state.isDefault,
                        ),
                    )
                }
            }.onSuccess {
                _uiState.update { it.copy(isBusy = false) }
                _effects.send(RenameTemplateEditUiEffect.Saved)
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        errorMessageResId = R.string.error_save_failed,
                    )
                }
            }
        }
    }

    class Factory(
        private val repository: RenameTemplateRepository,
        private val templateId: String?,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = RenameTemplateEditViewModel(repository, templateId) as T
    }
}
