package com.pictureorganizer.data.local

import com.pictureorganizer.model.ImageStatus
import com.pictureorganizer.util.file.AppPaths
import com.pictureorganizer.util.file.decodeJsonOrDefault
import com.pictureorganizer.util.file.writeTextAtomically
import com.pictureorganizer.util.list.ListPaging
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class ImageRecord(
    val id: String,
    val filePath: String,
    val fileName: String,
    val description: String,
    val status: String,
    val importedAt: Long,
    val tags: List<String> = emptyList(),
    val originalName: String? = null,
    val dateTakenMillis: Long? = null,
)

@Serializable
data class ImagesFile(
    val images: List<ImageRecord> = emptyList(),
)

@Serializable
data class TagRecord(
    val id: String,
    val name: String,
    val sortOrder: Int = 0,
    val createdAt: Long = 0L,
)

@Serializable
data class TagTemplateRecord(
    val id: String,
    val name: String,
    val tagNames: List<String> = emptyList(),
    val isDefault: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = 0L,
)

@Serializable
data class RenameTemplateRecord(
    val id: String,
    val name: String,
    val pattern: String,
    val isDefault: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = 0L,
)

@Serializable
data class CatalogFile(
    val tags: List<TagRecord> = emptyList(),
    val tagTemplates: List<TagTemplateRecord> = emptyList(),
    val renameTemplates: List<RenameTemplateRecord> = emptyList(),
)

@Serializable
data class PrefsFile(
    val tutorialCompleted: Boolean = false,
    val compressOnImport: Boolean = true,
    val importDuplicateAsk: Boolean = true,
    val defaultTagNames: List<String> = emptyList(),
    /** 桌面默认关：LazyColumn 已虚拟化；超大库可在设置里打开。 */
    val listPagingEnabled: Boolean = false,
    val listPageSize: Int = ListPaging.DEFAULT_PAGE_SIZE,
)

private val jsonFormat = Json { prettyPrint = true; ignoreUnknownKeys = true }

class JsonImageStore(
    private val file: File = File(AppPaths.appRoot(), "images.json"),
) {
    @Synchronized
    fun readAll(): MutableList<ImageRecord> =
        decodeJsonOrDefault(file, "JsonImageStore", { mutableListOf() }) {
            jsonFormat.decodeFromString<ImagesFile>(it).images.toMutableList()
        }

    @Synchronized
    fun writeAll(images: List<ImageRecord>) {
        file.writeTextAtomically(jsonFormat.encodeToString(ImagesFile(images)))
    }
}

class JsonCatalogStore(
    private val file: File = File(AppPaths.appRoot(), "catalog.json"),
) {
    @Synchronized
    fun read(): CatalogFile =
        decodeJsonOrDefault(file, "JsonCatalogStore", { CatalogFile() }) {
            jsonFormat.decodeFromString(it)
        }

    @Synchronized
    fun write(catalog: CatalogFile) {
        file.writeTextAtomically(jsonFormat.encodeToString(catalog))
    }
}

class JsonPrefsStore(
    private val file: File = File(AppPaths.appRoot(), "prefs.json"),
) {
    @Synchronized
    fun read(): PrefsFile =
        decodeJsonOrDefault(file, "JsonPrefsStore", { PrefsFile() }) {
            jsonFormat.decodeFromString(it)
        }

    @Synchronized
    fun write(prefs: PrefsFile) {
        file.writeTextAtomically(jsonFormat.encodeToString(prefs))
    }
}

fun ImageStatus.toStorageKey(): String =
    when (this) {
        ImageStatus.Pending -> "Pending"
        ImageStatus.Confirmed -> "Confirmed"
        ImageStatus.NoModify -> "NoModify"
    }

fun String.toImageStatus(): ImageStatus =
    when (this) {
        "Confirmed" -> ImageStatus.Confirmed
        "NoModify" -> ImageStatus.NoModify
        else -> ImageStatus.Pending
    }
