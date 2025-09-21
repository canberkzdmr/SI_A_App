package com.cbo.notes.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.model.Tag
import com.cbo.notes.domain.usecase.CreateNoteUseCase
import com.cbo.notes.domain.usecase.CreateTagUseCase
import com.cbo.notes.domain.usecase.GetCategoriesUseCase
import com.cbo.notes.domain.usecase.GetNoteByIdUseCase
import com.cbo.notes.domain.usecase.GetTagsUseCase
import com.cbo.notes.domain.usecase.UpdateNoteUseCase
import com.cbo.core.session.UserSession
import com.cbo.ui.snackbar.SnackbarManager
import com.cbo.ui.snackbar.SnackbarMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
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
    private val createTagUseCase: CreateTagUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private val noteId: Int = savedStateHandle.get<Int>("noteId") ?: 0
    private val isEditing = noteId != 0

    private val _uiState = MutableStateFlow(EditNoteUiState())
    val uiState: StateFlow<EditNoteUiState> = _uiState.asStateFlow()

    private val _navigationEvents = Channel<NavigationEvent>()
    val navigationEvents = _navigationEvents.receiveAsFlow()

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

                        _navigationEvents.trySend(NavigationEvent.NavigateBack)
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

    // Tag creation methods
    fun showCreateTagDialog() {
        _uiState.update { 
            it.copy(
                showCreateTagDialog = true,
                newTagName = "",
                newTagColor = null
            ) 
        }
    }

    fun hideCreateTagDialog() {
        _uiState.update { 
            it.copy(
                showCreateTagDialog = false,
                newTagName = "",
                newTagColor = null
            ) 
        }
    }

    fun updateNewTagName(name: String) {
        _uiState.update { it.copy(newTagName = name) }
    }

    fun updateNewTagColor(color: String?) {
        _uiState.update { it.copy(newTagColor = color) }
    }

    fun createTag() {
        val currentState = _uiState.value
        if (currentState.newTagName.isBlank()) {
            viewModelScope.launch {
                snackbarManager.showMessage(SnackbarMessage.Warning("Tag name cannot be empty"))
            }
            return
        }

        // Check if tag already exists
        val existingTag = currentState.availableTags.find { 
            it.name.equals(currentState.newTagName.trim(), ignoreCase = true) 
        }
        if (existingTag != null) {
            viewModelScope.launch {
                snackbarManager.showMessage(SnackbarMessage.Warning("Tag '${currentState.newTagName}' already exists"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingTag = true) }

            val currentUser = userSession.currentUser.first()
            currentUser?.let { user ->
                val newTag = Tag(
                    userId = user.id,
                    name = currentState.newTagName.trim(),
                    color = currentState.newTagColor
                )

                createTagUseCase(newTag).fold(
                    onSuccess = { createdTag ->
                        // Add the new tag to available tags and select it
                        _uiState.update { state ->
                            state.copy(
                                availableTags = state.availableTags + createdTag,
                                selectedTags = state.selectedTags + createdTag,
                                showCreateTagDialog = false,
                                isCreatingTag = false,
                                newTagName = "",
                                newTagColor = null,
                                hasUnsavedChanges = true
                            )
                        }
                        snackbarManager.showMessage(SnackbarMessage.Success("Tag '${createdTag.name}' created successfully"))
                    },
                    onFailure = { throwable ->
                        _uiState.update { it.copy(isCreatingTag = false) }
                        snackbarManager.showMessage(SnackbarMessage.Error("Failed to create tag: ${throwable.message}"))
                    }
                )
            }
        }
    }

    // Tag input field methods
    fun updateTagInputText(text: String) {
        _uiState.update { it.copy(tagInputText = text) }
    }

    fun createTagFromInput() {
        val currentState = _uiState.value
        val tagName = currentState.tagInputText.trim()
        
        if (tagName.isBlank()) {
            return // Don't show error for empty input, just ignore
        }

        // Check if tag already exists
        val existingTag = currentState.availableTags.find { 
            it.name.equals(tagName, ignoreCase = true) 
        }
        if (existingTag != null) {
            // If tag exists, just select it instead of creating a new one
            if (!currentState.selectedTags.contains(existingTag)) {
                toggleTag(existingTag)
            }
            _uiState.update { it.copy(tagInputText = "") }
            return
        }

        viewModelScope.launch {
            val currentUser = userSession.currentUser.first()
            currentUser?.let { user ->
                val newTag = Tag(
                    userId = user.id,
                    name = tagName,
                    color = null // Default to no color for quick-created tags
                )

                createTagUseCase(newTag).fold(
                    onSuccess = { createdTag ->
                        // Add the new tag to available tags and select it
                        _uiState.update { state ->
                            state.copy(
                                availableTags = state.availableTags + createdTag,
                                selectedTags = state.selectedTags + createdTag,
                                tagInputText = "",
                                hasUnsavedChanges = true
                            )
                        }
                        snackbarManager.showMessage(SnackbarMessage.Success("Tag '${createdTag.name}' created"))
                    },
                    onFailure = { throwable ->
                        snackbarManager.showMessage(SnackbarMessage.Error("Failed to create tag: ${throwable.message}"))
                    }
                )
            }
        }
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
    val errorMessage: String? = null,
    // Tag creation state
    val showCreateTagDialog: Boolean = false,
    val isCreatingTag: Boolean = false,
    val newTagName: String = "",
    val newTagColor: String? = null,
    // Tag input field state
    val tagInputText: String = ""
)

sealed class NavigationEvent {
    object NavigateBack : NavigationEvent()
}
