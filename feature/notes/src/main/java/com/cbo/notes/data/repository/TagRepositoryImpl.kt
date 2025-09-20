package com.cbo.notes.data.repository

import android.util.Log
import com.cbo.notes.data.mapper.TagEntityMapper
import com.cbo.core.database.dao.TagDao
import com.cbo.notes.domain.model.Tag
import com.cbo.notes.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao,
    private val tagEntityMapper: TagEntityMapper
) : TagRepository {

    override fun getTagsByUser(userId: Int): Flow<List<Tag>> {
        return tagDao.getTagsByUser(userId).map { entities ->
            entities.map { tagEntityMapper.toDomain(it) }
        }
    }

    override suspend fun getTagById(tagId: Int): Tag? {
        return try {
            tagDao.getTagById(tagId)?.let { entity ->
                tagEntityMapper.toDomain(entity)
            }
        } catch (e: Exception) {
            Log.e("TagRepositoryImpl", "Error getting tag by id: ${e.message}")
            null
        }
    }

    override suspend fun getTagByName(userId: Int, name: String): Tag? {
        return try {
            tagDao.getTagByName(userId, name)?.let { entity ->
                tagEntityMapper.toDomain(entity)
            }
        } catch (e: Exception) {
            Log.e("TagRepositoryImpl", "Error getting tag by name: ${e.message}")
            null
        }
    }

    override fun searchTags(userId: Int, query: String): Flow<List<Tag>> {
        return tagDao.searchTags(userId, query).map { entities ->
            entities.map { tagEntityMapper.toDomain(it) }
        }
    }

    override suspend fun insertTag(tag: Tag): Result<Tag> {
        return try {
            val entity = tagEntityMapper.toEntity(tag)
            val insertedId: Long = tagDao.insert(entity)
            val insertedTag = tag.copy(id = insertedId.toInt())
            Result.success(insertedTag)
        } catch (e: Exception) {
            Log.e("TagRepositoryImpl", "Error inserting tag: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateTag(tag: Tag): Result<Tag> {
        return try {
            val entity = tagEntityMapper.toEntity(tag)
            tagDao.update(entity)
            Result.success(tag)
        } catch (e: Exception) {
            Log.e("TagRepositoryImpl", "Error updating tag: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun deleteTag(tagId: Int): Result<Unit> {
        return try {
            tagDao.deleteById(tagId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TagRepositoryImpl", "Error deleting tag: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateTagColor(tagId: Int, color: String?): Result<Unit> {
        return try {
            tagDao.updateTagColor(tagId, color)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TagRepositoryImpl", "Error updating tag color: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateTagUsageCount(tagId: Int): Result<Unit> {
        return try {
            tagDao.updateTagUsageCount(tagId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TagRepositoryImpl", "Error updating tag usage count: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getTagsCount(userId: Int): Int {
        return try {
            tagDao.getTagsCount(userId)
        } catch (e: Exception) {
            Log.e("TagRepositoryImpl", "Error getting tags count: ${e.message}")
            0
        }
    }

    override suspend fun getTagsByNote(noteId: Int): List<Tag> {
        return try {
            tagDao.getTagsByNote(noteId).map { tagEntityMapper.toDomain(it) }
        } catch (e: Exception) {
            Log.e("TagRepositoryImpl", "Error getting tags by note: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getMostUsedTags(userId: Int, limit: Int): List<Tag> {
        return try {
            tagDao.getMostUsedTags(userId, limit).map { tagEntityMapper.toDomain(it) }
        } catch (e: Exception) {
            Log.e("TagRepositoryImpl", "Error getting most used tags: ${e.message}")
            emptyList()
        }
    }

    override suspend fun deleteUnusedTags(userId: Int): Result<Unit> {
        return try {
            tagDao.deleteUnusedTags(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TagRepositoryImpl", "Error deleting unused tags: ${e.message}")
            Result.failure(e)
        }
    }
}
