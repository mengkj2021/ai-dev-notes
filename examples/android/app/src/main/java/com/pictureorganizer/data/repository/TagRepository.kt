package com.pictureorganizer.data.repository

import com.pictureorganizer.model.Tag
import com.pictureorganizer.model.TagTemplate
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun observeTags(): Flow<List<Tag>>

    fun observeTemplates(): Flow<List<TagTemplate>>

    suspend fun getTags(): List<Tag>

    suspend fun getTemplates(): List<TagTemplate>

    suspend fun getTag(id: String): Tag?

    suspend fun getTemplate(id: String): TagTemplate?

    suspend fun insertTag(tag: Tag)

    suspend fun updateTag(tag: Tag)

    suspend fun deleteTag(id: String)

    suspend fun insertTemplate(template: TagTemplate)

    suspend fun updateTemplate(template: TagTemplate)

    suspend fun deleteTemplate(id: String)

    suspend fun setDefaultTemplate(id: String)
}
