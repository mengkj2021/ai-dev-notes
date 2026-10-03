package com.pictureorganizer.ui.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pictureorganizer.data.repository.TagRepository
import com.pictureorganizer.model.TagFilterCriteria
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FilterViewModel(
    tagRepository: TagRepository,
    initialCriteria: TagFilterCriteria,
) : ViewModel() {
    private val _effects = Channel<FilterUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val draft = MutableStateFlow(initialCriteria)

    val uiState: StateFlow<FilterUiState> =
        combine(
            tagRepository.observeTags(),
            draft,
        ) { tags, criteria ->
            FilterUiState(
                availableTags = tags.map { it.name },
                selectedTagNames = criteria.selectedTagNames,
                includeUntagged = criteria.includeUntagged,
                nameContains = criteria.nameContains,
                sort = criteria.sort,
                dateTakenFromEpochDay = criteria.dateTakenFromEpochDay,
                dateTakenToEpochDay = criteria.dateTakenToEpochDay,
                importedAtFromEpochDay = criteria.importedAtFromEpochDay,
                importedAtToEpochDay = criteria.importedAtToEpochDay,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue =
                FilterUiState(
                    selectedTagNames = initialCriteria.selectedTagNames,
                    includeUntagged = initialCriteria.includeUntagged,
                    nameContains = initialCriteria.nameContains,
                    sort = initialCriteria.sort,
                    dateTakenFromEpochDay = initialCriteria.dateTakenFromEpochDay,
                    dateTakenToEpochDay = initialCriteria.dateTakenToEpochDay,
                    importedAtFromEpochDay = initialCriteria.importedAtFromEpochDay,
                    importedAtToEpochDay = initialCriteria.importedAtToEpochDay,
                ),
        )

    fun onEvent(event: FilterUiEvent) {
        when (event) {
            is FilterUiEvent.ToggleTag -> {
                draft.update { current ->
                    val next =
                        if (event.name in current.selectedTagNames) {
                            current.selectedTagNames - event.name
                        } else {
                            current.selectedTagNames + event.name
                        }
                    current.copy(selectedTagNames = next)
                }
            }
            FilterUiEvent.ToggleUntagged -> {
                draft.update { it.copy(includeUntagged = !it.includeUntagged) }
            }
            is FilterUiEvent.NameContainsChanged -> {
                draft.update { it.copy(nameContains = event.value) }
            }
            is FilterUiEvent.SortChanged -> {
                draft.update { it.copy(sort = event.sort) }
            }
            is FilterUiEvent.DateTakenFromChanged -> {
                draft.update { it.copy(dateTakenFromEpochDay = event.epochDay) }
            }
            is FilterUiEvent.DateTakenToChanged -> {
                draft.update { it.copy(dateTakenToEpochDay = event.epochDay) }
            }
            is FilterUiEvent.ImportedAtFromChanged -> {
                draft.update { it.copy(importedAtFromEpochDay = event.epochDay) }
            }
            is FilterUiEvent.ImportedAtToChanged -> {
                draft.update { it.copy(importedAtToEpochDay = event.epochDay) }
            }
            FilterUiEvent.Apply -> {
                viewModelScope.launch {
                    _effects.send(FilterUiEffect.ApplyAndClose(draft.value))
                }
            }
            FilterUiEvent.ClearAndApply -> {
                viewModelScope.launch {
                    draft.value = TagFilterCriteria()
                    _effects.send(FilterUiEffect.ApplyAndClose(TagFilterCriteria()))
                }
            }
        }
    }

    class Factory(
        private val tagRepository: TagRepository,
        private val initialCriteria: TagFilterCriteria,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = FilterViewModel(tagRepository, initialCriteria) as T
    }
}
