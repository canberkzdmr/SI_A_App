package com.cbo.notes.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.model.Tag
import com.cbo.notes.domain.usecase.ArchiveNoteUseCase
import com.cbo.notes.domain.usecase.DeleteNoteUseCase
import com.cbo.notes.domain.usecase.GetArchivedNotesUseCase
import com.cbo.notes.domain.usecase.GetCategoriesUseCase
import com.cbo.notes.domain.usecase.GetFavoriteNotesUseCase
import com.cbo.notes.domain.usecase.GetNotesUseCase
import com.cbo.notes.domain.usecase.GetTagsUseCase
import com.cbo.notes.domain.usecase.SearchNotesUseCase
import com.cbo.notes.domain.usecase.ToggleNoteFavoriteUseCase
import com.cbo.notes.domain.usecase.ToggleNotePinnedUseCase
import com.cbo.notes.presentation.SortOrder
import com.cbo.notes.presentation.ViewMode
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
class NotesViewModel @Inject constructor(
    private val userSession: UserSession,
    private val getNotesUseCase: GetNotesUseCase,
    private val getFavoriteNotesUseCase: GetFavoriteNotesUseCase,
    private val getArchivedNotesUseCase: GetArchivedNotesUseCase,
    private val searchNotesUseCase: SearchNotesUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getTagsUseCase: GetTagsUseCase,
    private val toggleNotePinnedUseCase: ToggleNotePinnedUseCase,
    private val toggleNoteFavoriteUseCase: ToggleNoteFavoriteUseCase,
    private val archiveNoteUseCase: ArchiveNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        Log.d("NotesViewModel", "init")
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val currentUser = userSession.currentUser.first()
            currentUser?.let { user ->
                Log.d("NotesViewModel", "current user ${user.username}(${user.id})")
                _uiState.update { it.copy(isLoading = true) }

                combine(
                    getNotesUseCase(user.id),
                    getCategoriesUseCase(user.id),
                    getTagsUseCase(user.id)
                ) { notes, categories, tags ->
                    Triple(notes, categories, tags)
                }.catch { throwable ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = "Failed to load notes: ${throwable.message}"
                        ) 
                    }
                }.collect { (notes, categories, tags) ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            notes = notes,
                            categories = categories,
                            tags = tags,
                            filteredNotes = filterNotes(notes, currentState.searchQuery, currentState.selectedCategory, currentState.selectedTags)
                        )
                    }
                }
            }
        }
    }

    fun searchNotes(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        val currentState = _uiState.value
        val filteredNotes = filterNotes(currentState.notes, query, currentState.selectedCategory, currentState.selectedTags)
        _uiState.update { it.copy(filteredNotes = filteredNotes) }
    }

    fun filterByCategory(category: Category?) {
        _uiState.update { it.copy(selectedCategory = category) }
        val currentState = _uiState.value
        val filteredNotes = filterNotes(currentState.notes, currentState.searchQuery, category, currentState.selectedTags)
        _uiState.update { it.copy(filteredNotes = filteredNotes) }
    }

    fun filterByTags(tags: List<Tag>) {
        _uiState.update { it.copy(selectedTags = tags) }
        val currentState = _uiState.value
        val filteredNotes = filterNotes(currentState.notes, currentState.searchQuery, currentState.selectedCategory, tags)
        _uiState.update { it.copy(filteredNotes = filteredNotes) }
    }

    fun clearFilters() {
        _uiState.update { 
            it.copy(
                searchQuery = "", 
                selectedCategory = null, 
                selectedTags = emptyList(),
                filteredNotes = it.notes
            ) 
        }
    }

    fun toggleNotePin(noteId: Int) {
        viewModelScope.launch {
            val note = _uiState.value.notes.find { it.id == noteId } ?: return@launch
            toggleNotePinnedUseCase(noteId, !note.isPinned).fold(
                onSuccess = {
                    snackbarManager.showMessage(
                        SnackbarMessage.Success(if (!note.isPinned) "Note pinned" else "Note unpinned")
                    )
                },
                onFailure = {
                    snackbarManager.showMessage(
                        SnackbarMessage.Error("Failed to update note")
                    )
                }
            )
        }
    }

    fun toggleNoteFavorite(noteId: Int) {
        viewModelScope.launch {
            val note = _uiState.value.notes.find { it.id == noteId } ?: return@launch
            toggleNoteFavoriteUseCase(noteId, !note.isFavorite).fold(
                onSuccess = {
                    snackbarManager.showMessage(
                        SnackbarMessage.Success(if (!note.isFavorite) "Added to favorites" else "Removed from favorites")
                    )
                },
                onFailure = {
                    snackbarManager.showMessage(
                        SnackbarMessage.Error("Failed to update note")
                    )
                }
            )
        }
    }

    fun archiveNote(noteId: Int) {
        viewModelScope.launch {
            archiveNoteUseCase(noteId, true).fold(
                onSuccess = {
                    snackbarManager.showMessage(SnackbarMessage.Success("Note archived"))
                },
                onFailure = {
                    snackbarManager.showMessage(SnackbarMessage.Error("Failed to archive note"))
                }
            )
        }
    }

    fun deleteNote(noteId: Int) {
        viewModelScope.launch {
            deleteNoteUseCase(noteId).fold(
                onSuccess = {
                    snackbarManager.showMessage(SnackbarMessage.Success("Note deleted"))
                },
                onFailure = {
                    snackbarManager.showMessage(SnackbarMessage.Error("Failed to delete note"))
                }
            )
        }
    }

    fun changeViewMode(viewMode: ViewMode) {
        _uiState.update { it.copy(viewMode = viewMode) }
    }

    fun changeSortOrder(sortOrder: SortOrder) {
        _uiState.update { it.copy(sortOrder = sortOrder) }
        val currentState = _uiState.value
        val sortedNotes = sortNotes(currentState.filteredNotes, sortOrder)
        _uiState.update { it.copy(filteredNotes = sortedNotes) }
    }

    private fun filterNotes(
        notes: List<Note>,
        searchQuery: String,
        selectedCategory: Category?,
        selectedTags: List<Tag>
    ): List<Note> {
        var filtered = notes

        // Filter by search query
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { note ->
                note.title.contains(searchQuery, ignoreCase = true) ||
                note.content.contains(searchQuery, ignoreCase = true)
            }
        }

        // Filter by category
        selectedCategory?.let { category ->
            filtered = filtered.filter { note ->
                note.category?.id == category.id
            }
        }

        // Filter by tags
        if (selectedTags.isNotEmpty()) {
            filtered = filtered.filter { note ->
                selectedTags.all { selectedTag ->
                    note.tags.any { noteTag -> noteTag.id == selectedTag.id }
                }
            }
        }

        return sortNotes(filtered, _uiState.value.sortOrder)
    }

    private fun sortNotes(notes: List<Note>, sortOrder: SortOrder): List<Note> {
        return when (sortOrder) {
            SortOrder.UPDATED_DESC -> notes.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt })
            SortOrder.UPDATED_ASC -> notes.sortedWith(compareByDescending<Note> { it.isPinned }.thenBy { it.updatedAt })
            SortOrder.CREATED_DESC -> notes.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.createdAt })
            SortOrder.CREATED_ASC -> notes.sortedWith(compareByDescending<Note> { it.isPinned }.thenBy { it.createdAt })
            SortOrder.TITLE_ASC -> notes.sortedWith(compareByDescending<Note> { it.isPinned }.thenBy { it.title })
            SortOrder.TITLE_DESC -> notes.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.title })
        }
    }
}

data class NotesUiState(
    val isLoading: Boolean = false,
    val notes: List<Note> = emptyList(),
    val filteredNotes: List<Note> = emptyList(),
    val categories: List<Category> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: Category? = null,
    val selectedTags: List<Tag> = emptyList(),
    val viewMode: ViewMode = ViewMode.LIST,
    val sortOrder: SortOrder = SortOrder.UPDATED_DESC,
    val errorMessage: String? = null
)
