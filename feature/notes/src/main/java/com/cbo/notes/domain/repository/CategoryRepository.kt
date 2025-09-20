package com.cbo.notes.domain.repository

import com.cbo.notes.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategoriesByUser(userId: Int): Flow<List<Category>>
    suspend fun getCategoryById(categoryId: Int): Category?
    suspend fun getCategoryByName(userId: Int, name: String): Category?
    
    suspend fun insertCategory(category: Category): Result<Category>
    suspend fun updateCategory(category: Category): Result<Category>
    suspend fun deleteCategory(categoryId: Int): Result<Unit>
    
    suspend fun updateCategorySortOrder(categoryId: Int, sortOrder: Int): Result<Unit>
    suspend fun updateCategoryColor(categoryId: Int, color: String?): Result<Unit>
    
    suspend fun getCategoriesCount(userId: Int): Int
    suspend fun deleteUnusedCategories(userId: Int): Result<Unit>
}
