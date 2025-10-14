package com.cbo.notes.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.usecase.CreateCategoryUseCase
import com.cbo.notes.domain.usecase.DeleteCategoryUseCase
import com.cbo.notes.domain.usecase.GetCategoriesUseCase
import com.cbo.notes.domain.usecase.UpdateCategoryUseCase
import com.cbo.core.session.UserSession
import com.cbo.ui.snackbar.SnackbarManager
import com.cbo.ui.snackbar.SnackbarMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val userSession: UserSession,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val currentUser = userSession.currentUser.first()
            currentUser?.let { user ->
                _uiState.update { it.copy(isLoading = true) }

                getCategoriesUseCase(user.id)
                    .catch { throwable ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                errorMessage = "Failed to load categories: ${throwable.message}"
                            ) 
                        }
                    }
                    .collect { categories ->
                        _uiState.update { 
                            it.copy(isLoading = false, categories = categories) 
                        }
                    }
            }
        }
    }

    fun showInfoDialog() {
        _uiState.update {
            it.copy(
                showInfoDialog = true,
            )
        }
    }

    fun showCreateCategoryDialog() {
        _uiState.update { 
            it.copy(
                showCreateDialog = true,
                editingCategory = null,
                dialogTitle = "",
                dialogColor = null,
                dialogDescription = ""
            ) 
        }
    }

    fun showEditCategoryDialog(category: Category) {
        _uiState.update { 
            it.copy(
                showCreateDialog = true,
                editingCategory = category,
                dialogTitle = category.name,
                dialogColor = category.color,
                dialogDescription = category.description ?: ""
            ) 
        }
    }

    fun hideDialog() {
        _uiState.update { 
            it.copy(
                showInfoDialog = false,
                showCreateDialog = false,
                editingCategory = null,
                dialogTitle = "",
                dialogColor = null,
                dialogDescription = ""
            ) 
        }
    }

    fun updateDialogTitle(title: String) {
        _uiState.update { it.copy(dialogTitle = title) }
    }

    fun updateDialogDescription(description: String) {
        _uiState.update { it.copy(dialogDescription = description) }
    }

    fun updateDialogColor(color: String?) {
        _uiState.update { it.copy(dialogColor = color) }
    }

    fun saveCategory() {
        val currentState = _uiState.value
        if (currentState.dialogTitle.isBlank()) {
            viewModelScope.launch {
                snackbarManager.showMessage(SnackbarMessage.Warning("Category name cannot be empty"))
            }
            return
        }

        viewModelScope.launch {
            val currentUser = userSession.currentUser.first()
            currentUser?.let { user ->
                _uiState.update { it.copy(isCreating = true) }

                val categoryToSave = if (currentState.editingCategory != null) {
                    currentState.editingCategory.copy(
                        name = currentState.dialogTitle,
                        description = currentState.dialogDescription.takeIf { it.isNotBlank() },
                        color = currentState.dialogColor
                    )
                } else {
                    Category(
                        userId = user.id,
                        name = currentState.dialogTitle,
                        description = currentState.dialogDescription.takeIf { it.isNotBlank() },
                        color = currentState.dialogColor,
                        sortOrder = currentState.categories.size
                    )
                }

                val result = if (currentState.editingCategory != null) {
                    updateCategoryUseCase(categoryToSave)
                } else {
                    createCategoryUseCase(categoryToSave)
                }

                result.fold(
                    onSuccess = {
                        _uiState.update { 
                            it.copy(
                                isCreating = false,
                                showCreateDialog = false,
                                editingCategory = null,
                                dialogTitle = "",
                                dialogColor = null,
                                dialogDescription = ""
                            ) 
                        }
                        snackbarManager.showMessage(
                            SnackbarMessage.Success(
                                if (currentState.editingCategory != null) "Category updated" else "Category created"
                            )
                        )
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isCreating = false) }
                        snackbarManager.showMessage(
                            SnackbarMessage.Error("Failed to save category: ${error.message}")
                        )
                    }
                )
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            deleteCategoryUseCase(category.id).fold(
                onSuccess = {
                    snackbarManager.showMessage(SnackbarMessage.Success("Category deleted"))
                },
                onFailure = { error ->
                    snackbarManager.showMessage(
                        SnackbarMessage.Error("Failed to delete category: ${error.message}")
                    )
                }
            )
            hideDialog()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

data class CategoriesUiState(
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val categories: List<Category> = emptyList(),
    val showInfoDialog: Boolean = false,
    val showCreateDialog: Boolean = false,
    val editingCategory: Category? = null,
    val dialogTitle: String = "",
    val dialogDescription: String = "",
    val dialogColor: String? = null,
    val errorMessage: String? = null
)
