package com.cbo.notes.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.core.domain.model.ViewMode
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
import com.cbo.core.session.UserSession
import com.cbo.notes.domain.usecase.GetNotesViewModeUseCase
import com.cbo.notes.domain.usecase.SetNotesViewModeUseCase
import com.cbo.ui.snackbar.SnackbarManager
import com.cbo.ui.snackbar.SnackbarMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.VisibleForTesting
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
    private val getNotesViewModeUseCase: GetNotesViewModeUseCase,
    private val setNotesViewModeUseCase: SetNotesViewModeUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    @VisibleForTesting
    var defaultDispatcher: CoroutineDispatcher = Dispatchers.Default

    // Filter States
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    private val _selectedTags = MutableStateFlow<List<Tag>>(emptyList())
    private val _sortOrder = MutableStateFlow(SortOrder.UPDATED_DESC)
    private val _viewMode = MutableStateFlow(ViewMode.LIST)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<NotesUiState> = userSession.currentUser
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(NotesUiState(isLoading = false))
            } else {
                // Initialize view mode
                _viewMode.value = getNotesViewModeUseCase().getOrNull() ?: ViewMode.LIST

                val filtersFlow = combine(
                    _searchQuery,
                    _selectedCategory,
                    _selectedTags,
                    _sortOrder,
                    _viewMode
                ) { query, category, tags, sort, viewMode ->
                    FilterState(query, category, tags, sort, viewMode)
                }

                combine(
                    getNotesUseCase(user.id),
                    getCategoriesUseCase(user.id),
                    getTagsUseCase(user.id),
                    filtersFlow
                ) { notes, categories, tags, filters ->
                    val filtered = withContext(defaultDispatcher) {
                        filterNotes(notes, filters.query, filters.category, filters.tags, filters.sort)
                    }
                    NotesUiState(
                        isLoading = false,
                        notes = notes,
                        filteredNotes = filtered,
                        categories = categories,
                        tags = tags,
                        searchQuery = filters.query,
                        selectedCategory = filters.category,
                        selectedTags = filters.tags,
                        sortOrder = filters.sort,
                        viewMode = filters.viewMode
                    )
                }
            }
        }
        .catch { e ->
            emit(NotesUiState(errorMessage = e.message))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotesUiState(isLoading = true)
        )

    fun searchNotes(query: String) {
        _searchQuery.value = query
    }

    fun filterByCategory(category: Category?) {
        _selectedCategory.value = category
    }

    fun filterByTags(tags: List<Tag>) {
        _selectedTags.value = tags
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = null
        _selectedTags.value = emptyList()
    }

    fun toggleNotePin(noteId: Int) {
        viewModelScope.launch {
            val note = uiState.value.notes.find { it.id == noteId } ?: return@launch
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
            val note = uiState.value.notes.find { it.id == noteId } ?: return@launch
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
        viewModelScope.launch {
            Log.d("NotesViewModel", "ViewMode Changed to -> $viewMode")
            setNotesViewModeUseCase.invoke(viewMode)
            _viewMode.value = viewMode
        }
    }

    fun changeSortOrder(sortOrder: SortOrder) {
        _sortOrder.value = sortOrder
    }

    private fun filterNotes(
        notes: List<Note>,
        searchQuery: String,
        selectedCategory: Category?,
        selectedTags: List<Tag>,
        sortOrder: SortOrder
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

        return sortNotes(filtered, sortOrder)
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

    private data class FilterState(
        val query: String,
        val category: Category?,
        val tags: List<Tag>,
        val sort: SortOrder,
        val viewMode: ViewMode
    )
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
