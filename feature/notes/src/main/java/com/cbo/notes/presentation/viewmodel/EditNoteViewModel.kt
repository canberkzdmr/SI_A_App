package com.cbo.notes.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.model.Tag
import com.cbo.notes.domain.usecase.CreateNoteUseCase
import com.cbo.notes.domain.usecase.GetCategoriesUseCase
import com.cbo.notes.domain.usecase.GetNoteByIdUseCase
import com.cbo.notes.domain.usecase.GetTagsUseCase
import com.cbo.notes.domain.usecase.UpdateNoteUseCase
import com.cbo.core.session.UserSession
import com.cbo.ui.snackbar.SnackbarManager
import com.cbo.ui.snackbar.SnackbarMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditNoteViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val userSession: UserSession,
    private val createNoteUseCase: CreateNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getTagsUseCase: GetTagsUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private val noteId: Int = savedStateHandle.get<Int>("noteId") ?: 0
    private val isEditing = noteId != 0

    private val _uiState = MutableStateFlow(EditNoteUiState())
    val uiState: StateFlow<EditNoteUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val currentUser = userSession.currentUser.first()
            currentUser?.let { user ->
                _uiState.update { it.copy(isLoading = true) }

                try {
                    val categories = getCategoriesUseCase(user.id).first()
                    val tags = getTagsUseCase(user.id).first()

                    if (isEditing) {
                        val note = getNoteByIdUseCase(noteId)
                        if (note != null) {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    isLoading = false,
                                    title = note.title,
                                    content = note.content,
                                    selectedCategory = note.category,
                                    selectedTags = note.tags,
                                    availableCategories = categories,
                                    availableTags = tags,
                                    originalNote = note
                                )
                            }
                        } else {
                            _uiState.update { 
                                it.copy(isLoading = false, errorMessage = "Note not found")
                            }
                        }
                    } else {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                availableCategories = categories,
                                availableTags = tags
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { 
                        it.copy(isLoading = false, errorMessage = "Failed to load data: ${e.message}")
                    }
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title, hasUnsavedChanges = true) }
    }

    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content, hasUnsavedChanges = true) }
    }

    fun selectCategory(category: Category?) {
        _uiState.update { it.copy(selectedCategory = category, hasUnsavedChanges = true) }
    }

    fun toggleTag(tag: Tag) {
        val currentTags = _uiState.value.selectedTags.toMutableList()
        if (currentTags.contains(tag)) {
            currentTags.remove(tag)
        } else {
            currentTags.add(tag)
        }
        _uiState.update { it.copy(selectedTags = currentTags, hasUnsavedChanges = true) }
    }

    fun saveNote() {
        val currentState = _uiState.value
        if (currentState.title.isBlank()) {
            viewModelScope.launch {
                snackbarManager.showMessage(SnackbarMessage.Warning("Title cannot be empty"))
            }
            return
        }

        viewModelScope.launch {
            val currentUser = userSession.currentUser.first()
            currentUser?.let { user ->
                _uiState.update { it.copy(isSaving = true) }

                val noteToSave = if (isEditing) {
                    currentState.originalNote!!.copy(
                        title = currentState.title,
                        content = currentState.content,
                        category = currentState.selectedCategory,
                        tags = currentState.selectedTags
                    )
                } else {
                    Note(
                        userId = user.id,
                        title = currentState.title,
                        content = currentState.content,
                        category = currentState.selectedCategory,
                        tags = currentState.selectedTags
                    )
                }

                val result = if (isEditing) {
                    updateNoteUseCase(noteToSave)
                } else {
                    createNoteUseCase(noteToSave)
                }

                result.fold(
                    onSuccess = { savedNote ->
                        _uiState.update { 
                            it.copy(
                                isSaving = false, 
                                hasUnsavedChanges = false,
                                originalNote = savedNote
                            ) 
                        }
                        snackbarManager.showMessage(
                            SnackbarMessage.Success(if (isEditing) "Note updated" else "Note created")
                        )
                        // Navigate back could be handled in the UI
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isSaving = false) }
                        snackbarManager.showMessage(
                            SnackbarMessage.Error("Failed to save note: ${error.message}")
                        )
                    }
                )
            }
        }
    }

    fun discardChanges() {
        val originalNote = _uiState.value.originalNote
        if (originalNote != null) {
            _uiState.update { 
                it.copy(
                    title = originalNote.title,
                    content = originalNote.content,
                    selectedCategory = originalNote.category,
                    selectedTags = originalNote.tags,
                    hasUnsavedChanges = false
                )
            }
        } else {
            _uiState.update { 
                it.copy(
                    title = "",
                    content = "",
                    selectedCategory = null,
                    selectedTags = emptyList(),
                    hasUnsavedChanges = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

data class EditNoteUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val title: String = "",
    val content: String = "",
    val selectedCategory: Category? = null,
    val selectedTags: List<Tag> = emptyList(),
    val availableCategories: List<Category> = emptyList(),
    val availableTags: List<Tag> = emptyList(),
    val hasUnsavedChanges: Boolean = false,
    val originalNote: Note? = null,
    val errorMessage: String? = null
)
