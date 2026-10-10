package com.pictureorganizer.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SharedCatalog(
    private val store: JsonCatalogStore = JsonCatalogStore(),
) {
    private val mutex = Mutex()
    private val _state = MutableStateFlow(store.read())
    val state: StateFlow<CatalogFile> = _state.asStateFlow()

    suspend fun <T> withLock(block: suspend (CatalogFile) -> Pair<CatalogFile, T>): T =
        mutex.withLock {
            val (next, result) = block(_state.value)
            if (next != _state.value) {
                store.write(next)
                _state.value = next
            }
            result
        }

    suspend fun update(block: (CatalogFile) -> CatalogFile) {
        withLock { current ->
            block(current) to Unit
        }
    }
}
