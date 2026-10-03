package com.pictureorganizer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pictureorganizer.util.list.ListPaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences",
)

class UserPreferencesRepository(
    private val context: Context,
) {
    private val dataStore = context.userPreferencesDataStore

    val tutorialCompleted: Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[KEY_TUTORIAL_COMPLETED] ?: false
        }

    val defaultTagNames: Flow<Set<String>> =
        dataStore.data.map { prefs ->
            parseTagNames(prefs[KEY_DEFAULT_TAGS])
        }

    val importCompressEnabled: Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[KEY_IMPORT_COMPRESS] ?: true
        }

    val importDuplicateAskEnabled: Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[KEY_IMPORT_DUPLICATE_ASK] ?: true
        }

    val listPagingEnabled: Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[KEY_LIST_PAGING_ENABLED] ?: true
        }

    val listPageSize: Flow<Int> =
        dataStore.data.map { prefs ->
            ListPaging.normalizePageSize(prefs[KEY_LIST_PAGE_SIZE] ?: ListPaging.DEFAULT_PAGE_SIZE)
        }

    suspend fun isTutorialCompleted(): Boolean = tutorialCompleted.first()

    suspend fun setTutorialCompleted(completed: Boolean = true) {
        dataStore.edit { prefs ->
            prefs[KEY_TUTORIAL_COMPLETED] = completed
        }
    }

    suspend fun getDefaultTagNames(): Set<String> = defaultTagNames.first()

    suspend fun setDefaultTagNames(names: Set<String>) {
        dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_TAGS] =
                names
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .joinToString(SEPARATOR)
        }
    }

    suspend fun removeDefaultTagName(name: String) {
        val tag = name.trim()
        if (tag.isEmpty()) return
        val current = getDefaultTagNames()
        if (tag !in current) return
        setDefaultTagNames(current - tag)
    }

    suspend fun renameDefaultTagName(
        oldName: String,
        newName: String,
    ) {
        val old = oldName.trim()
        val new = newName.trim()
        if (old.isEmpty() || new.isEmpty() || old == new) return
        val current = getDefaultTagNames()
        if (old !in current) return
        val updated =
            current
                .map { if (it == old) new else it }
                .toSet()
        setDefaultTagNames(updated)
    }

    suspend fun isImportCompressEnabled(): Boolean = importCompressEnabled.first()

    suspend fun setImportCompressEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_IMPORT_COMPRESS] = enabled
        }
    }

    suspend fun isImportDuplicateAskEnabled(): Boolean = importDuplicateAskEnabled.first()

    suspend fun setImportDuplicateAskEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_IMPORT_DUPLICATE_ASK] = enabled
        }
    }

    suspend fun setListPagingEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_LIST_PAGING_ENABLED] = enabled
        }
    }

    suspend fun setListPageSize(size: Int) {
        dataStore.edit { prefs ->
            prefs[KEY_LIST_PAGE_SIZE] = ListPaging.normalizePageSize(size)
        }
    }

    companion object {
        private const val SEPARATOR = "\u001f"
        private val KEY_TUTORIAL_COMPLETED = booleanPreferencesKey("tutorial_completed")
        private val KEY_DEFAULT_TAGS = stringPreferencesKey("default_tag_names")
        private val KEY_IMPORT_COMPRESS = booleanPreferencesKey("import_compress_enabled")
        private val KEY_IMPORT_DUPLICATE_ASK = booleanPreferencesKey("import_duplicate_ask_enabled")
        private val KEY_LIST_PAGING_ENABLED = booleanPreferencesKey("list_paging_enabled")
        private val KEY_LIST_PAGE_SIZE = intPreferencesKey("list_page_size")

        private fun parseTagNames(raw: String?): Set<String> {
            if (raw.isNullOrBlank()) return emptySet()
            return raw
                .split(SEPARATOR)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toSet()
        }
    }
}
