package com.pictureorganizer.data.repository

import com.pictureorganizer.data.local.JsonPrefsStore
import com.pictureorganizer.data.local.PrefsFile
import com.pictureorganizer.util.list.ListPaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class UserPreferencesRepository(
    private val store: JsonPrefsStore = JsonPrefsStore(),
) {
    private val mutex = Mutex()
    private val _prefs = MutableStateFlow(normalize(store.read()))
    val prefs: StateFlow<PrefsFile> = _prefs.asStateFlow()

    suspend fun isTutorialCompleted(): Boolean = mutex.withLock { _prefs.value.tutorialCompleted }

    suspend fun setTutorialCompleted(completed: Boolean = true) {
        update { it.copy(tutorialCompleted = completed) }
    }

    suspend fun isCompressOnImport(): Boolean = mutex.withLock { _prefs.value.compressOnImport }

    suspend fun setCompressOnImport(enabled: Boolean) {
        update { it.copy(compressOnImport = enabled) }
    }

    suspend fun isImportDuplicateAsk(): Boolean = mutex.withLock { _prefs.value.importDuplicateAsk }

    suspend fun setImportDuplicateAsk(enabled: Boolean) {
        update { it.copy(importDuplicateAsk = enabled) }
    }

    suspend fun getDefaultTagNames(): Set<String> = mutex.withLock { _prefs.value.defaultTagNames.toSet() }

    suspend fun setDefaultTagNames(names: Set<String>) {
        update {
            it.copy(
                defaultTagNames =
                    names
                        .map { n -> n.trim() }
                        .filter { n -> n.isNotEmpty() }
                        .distinct(),
            )
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
        setDefaultTagNames(current.map { if (it == old) new else it }.toSet())
    }

    suspend fun setListPagingEnabled(enabled: Boolean) {
        update { it.copy(listPagingEnabled = enabled) }
    }

    suspend fun setListPageSize(size: Int) {
        update { it.copy(listPageSize = ListPaging.normalizePageSize(size)) }
    }

    private suspend fun update(block: (PrefsFile) -> PrefsFile) {
        mutex.withLock {
            val next = normalize(block(_prefs.value))
            store.write(next)
            _prefs.value = next
        }
    }

    private fun normalize(prefs: PrefsFile): PrefsFile =
        prefs.copy(listPageSize = ListPaging.normalizePageSize(prefs.listPageSize))
}
