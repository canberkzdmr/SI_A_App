package com.cbo.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.cbo.core.database.entity.CategoryEntity
import com.cbo.core.database.entity.CategoryWithNotes
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao : BaseDao<CategoryEntity> {
    
    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY sortOrder ASC, name ASC")
    fun getCategoriesByUser(userId: Int): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Int): CategoryEntity?

    @Query("SELECT * FROM categories WHERE userId = :userId AND name = :name")
    suspend fun getCategoryByName(userId: Int, name: String): CategoryEntity?

    @Transaction
    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY sortOrder ASC, name ASC")
    fun getCategoriesWithNotes(userId: Int): Flow<List<CategoryWithNotes>>

    @Transaction
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryWithNotes(categoryId: Int): CategoryWithNotes?

    @Query("UPDATE categories SET sortOrder = :sortOrder WHERE id = :categoryId")
    suspend fun updateCategorySortOrder(categoryId: Int, sortOrder: Int)

    @Query("UPDATE categories SET color = :color WHERE id = :categoryId")
    suspend fun updateCategoryColor(categoryId: Int, color: String?)

    @Query("SELECT COUNT(*) FROM categories WHERE userId = :userId")
    suspend fun getCategoriesCount(userId: Int): Int

    @Query("DELETE FROM categories WHERE userId = :userId AND id NOT IN (SELECT DISTINCT categoryId FROM notes WHERE categoryId IS NOT NULL AND userId = :userId)")
    suspend fun deleteUnusedCategories(userId: Int)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: Int)
}
