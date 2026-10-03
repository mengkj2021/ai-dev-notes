package com.pictureorganizer.ui.renametemplate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pictureorganizer.R
import com.pictureorganizer.data.repository.RenameTemplateRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RenameTemplateManageViewModel(
    private val repository: RenameTemplateRepository,
) : ViewModel() {
    private val editor = MutableStateFlow(Editor())
    private val _effects = Channel<RenameTemplateManageUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    val uiState: StateFlow<RenameTemplateManageUiState> =
        combine(
            repository.observeAll(),
            editor,
        ) { templates, ed ->
            RenameTemplateManageUiState(
                templates = templates,
                confirmDeleteId = ed.confirmDeleteId,
                isBusy = ed.isBusy,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RenameTemplateManageUiState(),
        )

    fun onEvent(event: RenameTemplateManageUiEvent) {
        when (event) {
            is RenameTemplateManageUiEvent.SetDefault -> setDefault(event.id)
            is RenameTemplateManageUiEvent.RequestDelete ->
                editor.update {
                    it.copy(confirmDeleteId = event.id)
                }
            RenameTemplateManageUiEvent.ConfirmDelete -> confirmDelete()
            RenameTemplateManageUiEvent.CancelDelete ->
                editor.update {
                    it.copy(confirmDeleteId = null)
                }
            RenameTemplateManageUiEvent.NotifySaved ->
                viewModelScope.launch {
                    _effects.send(
                        RenameTemplateManageUiEffect.ShowMessage(R.string.tag_manage_saved),
                    )
                }
        }
    }

    private fun setDefault(id: String) {
        viewModelScope.launch {
            runCatching { repository.setDefault(id) }
                .onSuccess {
                    _effects.send(
                        RenameTemplateManageUiEffect.ShowMessage(R.string.tag_manage_default_set),
                    )
                }.onFailure {
                    _effects.send(
                        RenameTemplateManageUiEffect.ShowMessage(R.string.error_set_failed),
                    )
                }
        }
    }

    private fun confirmDelete() {
        val id = editor.value.confirmDeleteId ?: return
        viewModelScope.launch {
            editor.update { it.copy(isBusy = true, confirmDeleteId = null) }
            runCatching { repository.delete(id) }
                .onSuccess {
                    _effects.send(
                        RenameTemplateManageUiEffect.ShowMessage(
                            R.string.rename_template_deleted,
                        ),
                    )
                }.onFailure {
                    _effects.send(
                        RenameTemplateManageUiEffect.ShowMessage(R.string.error_delete_failed),
                    )
                }
            editor.update { it.copy(isBusy = false) }
        }
    }

    private data class Editor(
        val confirmDeleteId: String? = null,
        val isBusy: Boolean = false,
    )

    class Factory(
        private val repository: RenameTemplateRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = RenameTemplateManageViewModel(repository) as T
    }
}
