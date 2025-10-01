package com.cbo.notes.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.core.session.UserSession
import com.cbo.notes.domain.model.Tag
import com.cbo.notes.domain.usecase.CreateTagUseCase
import com.cbo.notes.domain.usecase.DeleteTagListUseCase
import com.cbo.notes.domain.usecase.GetTagsUseCase
import com.cbo.notes.domain.usecase.UpdateTagUseCase
import com.cbo.notes.presentation.SortOrder
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
class TagsViewModel
    @Inject
    constructor(
        private val userSession: UserSession,
        private val getTagsUseCase: GetTagsUseCase,
        private val createTagUseCase: CreateTagUseCase,
        private val updateTagUseCase: UpdateTagUseCase,
        private val deleteTagListUseCase: DeleteTagListUseCase,
        private val snackbarManager: SnackbarManager,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(TagsUiState())
        val uiState: StateFlow<TagsUiState> = _uiState.asStateFlow()

        init {
            loadData()
        }

        private fun loadData() {
            viewModelScope.launch {
                val currentUser = userSession.currentUser.first()
                currentUser?.let { user ->
                    _uiState.update { it.copy(isLoading = true) }

                    getTagsUseCase(user.id)
                        .catch { throwable ->
                            SnackbarManager.showMessage(SnackbarMessage.Error("Failed to load tags"))
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "Failed to load tags: ${throwable.message}",
                                )
                            }
                        }.collect { tags ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    tags = tags,
                                )
                            }
                        }
                }
            }
        }

        fun updateTag() {
            viewModelScope.launch {
                _uiState.update { it.copy(isCreating = true) }
                val tag = _uiState.value.editingTag
                tag?.let {
                    updateTagUseCase.invoke(tag).fold(
                        onSuccess = {
                            _uiState.update {
                                it.copy(
                                    isCreating = false,
                                    showCreateDialog = false,
                                    editingTag = null,
                                    selectedTags = emptyList(),
                                )
                            }
                            snackbarManager.showMessage(SnackbarMessage.Success("Tag updated"))
                        },
                        onFailure = { error ->
                            _uiState.update {
                                it.copy(
                                    isCreating = false,
                                    showCreateDialog = false,
                                    editingTag = null,
                                    selectedTags = emptyList(),
                                )
                            }
                            snackbarManager.showMessage(SnackbarMessage.Error("Failed to update tag."))
                        },
                    )
                }
            }
        }

        fun saveTag() {
            if (_uiState.value.dialogTagName.isBlank()) {
                viewModelScope.launch {
                    snackbarManager.showMessage(SnackbarMessage.Warning("Tag name cannot be empty"))
                }
                return
            }

            val existingTag =
                _uiState.value.tags.find {
                    it.name.equals(_uiState.value.dialogTagName.trim(), ignoreCase = true)
                }

            if (existingTag != null) {
                viewModelScope.launch {
                    snackbarManager.showMessage(SnackbarMessage.Warning("Tag '#${_uiState.value.dialogTagName}' already exists"))
                }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isCreating = true) }
                val currentUser = userSession.currentUser.first()
                currentUser?.let { user ->
                    val newTag =
                        Tag(
                            userId = user.id,
                            name = _uiState.value.dialogTagName,
                            color = _uiState.value.dialogTagColor,
                        )
                    createTagUseCase(newTag).fold(
                        onSuccess = { createdTag ->
                            _uiState.update {
                                it.copy(
                                    isCreating = false,
                                    showCreateDialog = false,
                                    editingTag = null,
                                    dialogTagName = "",
                                    dialogTagColor = null,
                                    selectedTags = emptyList(),
                                )
                            }
                            snackbarManager.showMessage(SnackbarMessage.Success("Tag '#${createdTag.name}' created successfully"))
                        },
                        onFailure = { throwable ->
                            _uiState.update {
                                it.copy(
                                    isCreating = false,
                                    showCreateDialog = false,
                                    editingTag = null,
                                    dialogTagName = "",
                                    dialogTagColor = null,
                                    selectedTags = emptyList(),
                                )
                            }
                            snackbarManager.showMessage(SnackbarMessage.Error("Failed to create tag: ${throwable.message}"))
                        },
                    )
                }
            }
        }

        fun deleteSelectedTags() {
            viewModelScope.launch {
                _uiState.update { it.copy(isCreating = true) }
                val selectedTags = _uiState.value.selectedTags
                if (selectedTags.isNotEmpty()) {
                    deleteTagListUseCase.invoke(selectedTags).fold(
                        onSuccess = {
                            _uiState.update {
                                it.copy(
                                    isCreating = false,
                                    showCreateDialog = false,
                                    editingTag = null,
                                    selectedTags = emptyList(),
                                    viewMode = ViewMode.EDIT,
                                )
                            }
                            snackbarManager.showMessage(SnackbarMessage.Success("Tags deleted"))
                        },
                        onFailure = {
                            _uiState.update {
                                it.copy(
                                    isCreating = false,
                                    showCreateDialog = false,
                                    editingTag = null,
                                    selectedTags = emptyList(),
                                    viewMode = ViewMode.EDIT,
                                )
                            }
                            snackbarManager.showMessage(SnackbarMessage.Error("Failed to delete tags"))
                        },
                    )
                } else {
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            showCreateDialog = false,
                            editingTag = null,
                            selectedTags = emptyList(),
                            viewMode = ViewMode.EDIT,
                        )
                    }
                }
            }
        }

        fun changeViewMode(viewMode: ViewMode) {
            _uiState.update { it.copy(viewMode = viewMode, selectedTags = emptyList()) }
        }

        fun showCreateTagDialog() {
            Log.d("TagsViewModel", "ui state updated")
            _uiState.update {
                it.copy(
                    showCreateDialog = true,
                    editingTag = null,
                    isCreating = false,
                    dialogTagName = "",
                )
            }
        }

        fun hideCreateTagDialog() {
            Log.d("TagsViewModel", "ui state updated")
            _uiState.update {
                it.copy(
                    showCreateDialog = false,
                    editingTag = null,
                    isCreating = false,
                    dialogTagName = "",
                )
            }
        }

        fun showEditTagDialog(tag: Tag) {
            _uiState.update {
                it.copy(
                    showCreateDialog = true,
                    editingTag = tag,
                    dialogTagName = tag.name,
                )
            }
        }

        fun updateSelectedTags(tags: List<Tag>) {
            _uiState.update { it.copy(selectedTags = tags) }
        }

        fun updateTagName(name: String) {
            _uiState.update { it.copy(editingTag = it.editingTag?.copy(name = name), dialogTagName = name) }
        }

        fun updateTagColor(color: String?) {
            _uiState.update { it.copy(dialogTagColor = color) }
        }
    }

enum class ViewMode {
    EDIT,
    DELETE,
}

data class TagsUiState(
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val viewMode: ViewMode = ViewMode.EDIT,
    val tags: List<Tag> = emptyList(),
    val name: String = "",
    val color: String? = null,
    val usageCount: Int = 0,
    val selectedTags: List<Tag> = emptyList(),
    val showCreateDialog: Boolean = false,
    val editingTag: Tag? = null,
    val dialogTagName: String = "",
    val dialogTagColor: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val sortOrder: SortOrder = SortOrder.UPDATED_ASC,
    val errorMessage: String? = null,
)
