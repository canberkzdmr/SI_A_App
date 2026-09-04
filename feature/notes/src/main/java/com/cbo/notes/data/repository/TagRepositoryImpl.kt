package com.cbo.notes.data.repository

import com.cbo.core.logger.AppLogger
import com.cbo.notes.data.mapper.TagEntityMapper
import com.cbo.core.common.util.TagColorPalette
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
            AppLogger.e("Error getting tag by id: ${e.message}", e)
            null
        }
    }

    override suspend fun getTagByName(userId: Int, name: String): Tag? {
        return try {
            tagDao.getTagByName(userId, name)?.let { entity ->
                tagEntityMapper.toDomain(entity)
            }
        } catch (e: Exception) {
            AppLogger.e("Error getting tag by name: ${e.message}", e)
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
            val tagWithColor = if (tag.color.isNullOrBlank()) {
                tag.copy(color = TagColorPalette.pickForTagName(tag.name))
            } else tag
            val entity = tagEntityMapper.toEntity(tagWithColor)
            val insertedId: Long = tagDao.insert(entity)
            val insertedTag = tagWithColor.copy(id = insertedId.toInt())
            Result.success(insertedTag)
        } catch (e: Exception) {
            AppLogger.e("Error inserting tag: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateTag(tag: Tag): Result<Tag> {
        return try {
            val entity = tagEntityMapper.toEntity(tag)
            tagDao.update(entity)
            Result.success(tag)
        } catch (e: Exception) {
            AppLogger.e("Error updating tag: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteTag(tagId: Int): Result<Unit> {
        return try {
            tagDao.deleteById(tagId)
            Result.success(Unit)
        } catch (e: Exception) {
            AppLogger.e("Error deleting tag: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteTagList(tags: List<Tag>): Result<Unit> {
        return try {
            val entities = tags.map { tagEntityMapper.toEntity(it) }
            tagDao.deleteAll(entities)
            Result.success(Unit)
        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    override suspend fun updateTagColor(tagId: Int, color: String?): Result<Unit> {
        return try {
            tagDao.updateTagColor(tagId, color)
            Result.success(Unit)
        } catch (e: Exception) {
            AppLogger.e("Error updating tag color: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateTagUsageCount(tagId: Int): Result<Unit> {
        return try {
            tagDao.updateTagUsageCount(tagId)
            Result.success(Unit)
        } catch (e: Exception) {
            AppLogger.e("Error updating tag usage count: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getTagsCount(userId: Int): Int {
        return try {
            tagDao.getTagsCount(userId)
        } catch (e: Exception) {
            AppLogger.e("Error getting tags count: ${e.message}", e)
            0
        }
    }

    override suspend fun getTagsByNote(noteId: Int): List<Tag> {
        return try {
            tagDao.getTagsByNote(noteId).map { tagEntityMapper.toDomain(it) }
        } catch (e: Exception) {
            AppLogger.e("Error getting tags by note: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getMostUsedTags(userId: Int, limit: Int): List<Tag> {
        return try {
            tagDao.getMostUsedTags(userId, limit).map { tagEntityMapper.toDomain(it) }
        } catch (e: Exception) {
            AppLogger.e("Error getting most used tags: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun deleteUnusedTags(userId: Int): Result<Unit> {
        return try {
            tagDao.deleteUnusedTags(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            AppLogger.e("Error deleting unused tags: ${e.message}", e)
            Result.failure(e)
        }
    }
}
