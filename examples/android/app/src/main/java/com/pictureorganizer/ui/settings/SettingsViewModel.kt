package com.pictureorganizer.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pictureorganizer.data.repository.UserPreferencesRepository
import com.pictureorganizer.util.list.ListPaging
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferences: UserPreferencesRepository,
) : ViewModel() {
    val importDuplicateAskEnabled: StateFlow<Boolean> =
        userPreferences.importDuplicateAskEnabled.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true,
        )

    val listPagingEnabled: StateFlow<Boolean> =
        userPreferences.listPagingEnabled.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true,
        )

    val listPageSize: StateFlow<Int> =
        userPreferences.listPageSize.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ListPaging.DEFAULT_PAGE_SIZE,
        )

    fun setImportDuplicateAskEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setImportDuplicateAskEnabled(enabled)
        }
    }

    fun setListPagingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setListPagingEnabled(enabled)
        }
    }

    fun setListPageSize(size: Int) {
        viewModelScope.launch {
            userPreferences.setListPageSize(size)
        }
    }

    class Factory(
        private val userPreferences: UserPreferencesRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(userPreferences) as T
    }
}
