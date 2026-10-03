package com.pictureorganizer.model

data class ImageListItem(
    val id: String,
    val date: String = "",
    val description: String,
    val tags: List<String> = emptyList(),
    val placeholderColorArgb: Long = 0,
    val status: ImageStatus,
    val filePath: String = "",
    val importedAt: Long = 0L,
    val originalName: String? = null,
    val dateTakenMillis: Long? = null,
) {
    fun withStatus(newStatus: ImageStatus): ImageListItem =
        copy(
            status = newStatus,
            tags = userTagsOf(tags),
        )

    companion object {
        val STATUS_TAGS = setOf("待归档", "已归档", "回收站")

        val LEGACY_STATUS_TAGS = setOf("待处理", "已确认")

        val STATUS_DISPLAY_ALIASES =
            STATUS_TAGS +
                LEGACY_STATUS_TAGS +
                setOf(
                    "未分類",
                    "分類済み",
                    "未処理",
                    "確認済み",
                    "ゴミ箱",
                    "Unfiled",
                    "Filed",
                    "Pending",
                    "Confirmed",
                    "Trash",
                )

        fun userTagsOf(tags: List<String>): List<String> = tags.filter { it !in STATUS_TAGS && it !in LEGACY_STATUS_TAGS }

        fun isReservedStatusName(name: String): Boolean = name.trim() in STATUS_DISPLAY_ALIASES

        fun removeTagName(
            tags: List<String>,
            name: String,
        ): List<String> {
            val target = name.trim()
            if (target.isEmpty()) return tags
            return tags.filter { it != target }
        }

        fun countImagesWithTag(
            imageTagLists: List<List<String>>,
            name: String,
        ): Int {
            val target = name.trim()
            if (target.isEmpty()) return 0
            return imageTagLists.count { target in it }
        }

        fun renameTagName(
            tags: List<String>,
            oldName: String,
            newName: String,
        ): List<String> {
            val old = oldName.trim()
            val renamed = newName.trim()
            if (old.isEmpty() || renamed.isEmpty() || old == renamed || old !in tags) return tags
            return if (renamed in tags) {
                tags.filter { it != old }
            } else {
                tags.map { if (it == old) renamed else it }
            }
        }
    }
}
