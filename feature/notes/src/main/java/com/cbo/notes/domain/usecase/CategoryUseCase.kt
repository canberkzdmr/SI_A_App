package com.cbo.notes.domain.usecase

import android.database.sqlite.SQLiteConstraintException
import com.cbo.core.logger.AppLogger
import com.cbo.core.session.UserSession
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.repository.CategoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class GetCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val userSession: UserSession
) {
    operator fun invoke(): Flow<List<Category>> {
        return userSession.currentUser.flatMapLatest { user ->
            user?.let { categoryRepository.getCategoriesByUser(it.id) } ?: flowOf(emptyList())
        }
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
                    AppLogger.d("Category ${category.name} already exists")
                    return Result.failure(Throwable("Category ${category.name} already exists"))
                } else {
                    AppLogger.d("(CreateCategoryUseCase)An error occurred: ${exception.message?:"Error unknown!"}")
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
