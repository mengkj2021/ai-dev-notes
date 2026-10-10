package com.pictureorganizer.model

fun tagsFromTagTemplateOverwrite(templateTagNames: List<String>): List<String> {
    val result = LinkedHashSet<String>()
    for (raw in templateTagNames) {
        val name = raw.trim()
        if (name.isEmpty() || ImageListItem.isReservedStatusName(name)) continue
        result.add(name)
    }
    return result.toList()
}
