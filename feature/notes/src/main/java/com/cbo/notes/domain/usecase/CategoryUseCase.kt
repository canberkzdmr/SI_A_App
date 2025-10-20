package com.cbo.notes.domain.usecase

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(userId: Int): Flow<List<Category>> {
        return categoryRepository.getCategoriesByUser(userId)
    }
}

class CreateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(category: Category): Result<Category> {
        val result = categoryRepository.insertCategory(category)
        result.fold(
            onSuccess = {
                return Result.success(it)
            },
            onFailure = { exception ->
                if (exception is SQLiteConstraintException) {
                    Log.d("CategoryUseCase", "Category ${category.name} already exists")
                    return Result.failure(Throwable("Category ${category.name} already exists"))
                } else {
                    Log.d("CategoryUseCase", "(CreateCategoryUseCase)An error occurred: ${exception.message?:"Error unknown!"}")
                    return Result.failure(exception)
                }
            }
        )
    }
}

class UpdateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(category: Category): Result<Category> {
        return categoryRepository.updateCategory(category)
    }
}

class DeleteCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(categoryId: Int): Result<Unit> {
        return categoryRepository.deleteCategory(categoryId)
    }
}

class GetCategoryByIdUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(categoryId: Int): Category? {
        return categoryRepository.getCategoryById(categoryId)
    }
}
