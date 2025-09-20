package com.cbo.notes.data.repository

import android.util.Log
import com.cbo.notes.data.mapper.CategoryEntityMapper
import com.cbo.core.database.dao.CategoryDao
import com.cbo.core.database.dao.NoteDao
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val noteDao: NoteDao,
    private val categoryEntityMapper: CategoryEntityMapper
) : CategoryRepository {

    override fun getCategoriesByUser(userId: Int): Flow<List<Category>> {
        return categoryDao.getCategoriesByUser(userId).map { entities ->
            entities.map { entity ->
                val notesCount = noteDao.getNotesCountByCategory(userId, entity.id)
                categoryEntityMapper.toDomain(entity, notesCount)
            }
        }
    }

    override suspend fun getCategoryById(categoryId: Int): Category? {
        return try {
            categoryDao.getCategoryById(categoryId)?.let { entity ->
                categoryEntityMapper.toDomain(entity)
            }
        } catch (e: Exception) {
            Log.e("CategoryRepositoryImpl", "Error getting category by id: ${e.message}")
            null
        }
    }

    override suspend fun getCategoryByName(userId: Int, name: String): Category? {
        return try {
            categoryDao.getCategoryByName(userId, name)?.let { entity ->
                categoryEntityMapper.toDomain(entity)
            }
        } catch (e: Exception) {
            Log.e("CategoryRepositoryImpl", "Error getting category by name: ${e.message}")
            null
        }
    }

    override suspend fun insertCategory(category: Category): Result<Category> {
        return try {
            val entity = categoryEntityMapper.toEntity(category)
            val insertedId: Long = categoryDao.insert(entity)
            val insertedCategory = category.copy(id = insertedId.toInt())
            Result.success(insertedCategory)
        } catch (e: Exception) {
            Log.e("CategoryRepositoryImpl", "Error inserting category: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateCategory(category: Category): Result<Category> {
        return try {
            val entity = categoryEntityMapper.toEntity(category)
            categoryDao.update(entity)
            Result.success(category)
        } catch (e: Exception) {
            Log.e("CategoryRepositoryImpl", "Error updating category: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun deleteCategory(categoryId: Int): Result<Unit> {
        return try {
            categoryDao.deleteById(categoryId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CategoryRepositoryImpl", "Error deleting category: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateCategorySortOrder(categoryId: Int, sortOrder: Int): Result<Unit> {
        return try {
            categoryDao.updateCategorySortOrder(categoryId, sortOrder)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CategoryRepositoryImpl", "Error updating category sort order: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateCategoryColor(categoryId: Int, color: String?): Result<Unit> {
        return try {
            categoryDao.updateCategoryColor(categoryId, color)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CategoryRepositoryImpl", "Error updating category color: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getCategoriesCount(userId: Int): Int {
        return try {
            categoryDao.getCategoriesCount(userId)
        } catch (e: Exception) {
            Log.e("CategoryRepositoryImpl", "Error getting categories count: ${e.message}")
            0
        }
    }

    override suspend fun deleteUnusedCategories(userId: Int): Result<Unit> {
        return try {
            categoryDao.deleteUnusedCategories(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CategoryRepositoryImpl", "Error deleting unused categories: ${e.message}")
            Result.failure(e)
        }
    }
}
