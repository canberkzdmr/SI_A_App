package com.cbo.notes.domain.repository

import com.cbo.notes.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun getTagsByUser(userId: Int): Flow<List<Tag>>
    suspend fun getTagById(tagId: Int): Tag?
    suspend fun getTagByName(userId: Int, name: String): Tag?
    fun searchTags(userId: Int, query: String): Flow<List<Tag>>
    
    suspend fun insertTag(tag: Tag): Result<Tag>
    suspend fun updateTag(tag: Tag): Result<Tag>
    suspend fun deleteTag(tagId: Int): Result<Unit>
    suspend fun deleteTagList(tags: List<Tag>): Result<Unit>
    
    suspend fun updateTagColor(tagId: Int, color: String?): Result<Unit>
    suspend fun updateTagUsageCount(tagId: Int): Result<Unit>
    
    suspend fun getTagsCount(userId: Int): Int
    suspend fun getTagsByNote(noteId: Int): List<Tag>
    suspend fun getMostUsedTags(userId: Int, limit: Int = 10): List<Tag>
    suspend fun deleteUnusedTags(userId: Int): Result<Unit>
}
