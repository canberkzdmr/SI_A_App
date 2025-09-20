package com.cbo.notes.domain.usecase

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
        return categoryRepository.insertCategory(category)
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
