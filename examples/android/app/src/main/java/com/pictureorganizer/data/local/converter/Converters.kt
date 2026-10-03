package com.pictureorganizer.data.local.converter

import androidx.room.TypeConverter
import org.json.JSONArray

class Converters {
    @TypeConverter
    fun fromTagsJson(json: String): List<String> {
        if (json.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(json)
            List(array.length()) { index -> array.getString(index) }
        }.getOrElse { emptyList() }
    }

    @TypeConverter
    fun toTagsJson(tags: List<String>): String {
        val array = JSONArray()
        tags.forEach { array.put(it) }
        return array.toString()
    }
}
