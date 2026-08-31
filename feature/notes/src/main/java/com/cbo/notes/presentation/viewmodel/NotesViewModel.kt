package com.cbo.notes.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.core.domain.model.ViewMode
import com.cbo.notes.R
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
import com.cbo.notes.domain.usecase.GetNotesViewModeUseCase
import com.cbo.notes.domain.usecase.SetNotesViewModeUseCase
import com.cbo.ui.snackbar.SnackbarManager
import com.cbo.ui.snackbar.SnackbarMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
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
    private val snackbarManager: SnackbarManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        val lockedCategoryId = savedStateHandle.get<Int>("categoryId")?.takeIf { it != -1 }
        val lockedTagId = savedStateHandle.get<Int>("tagId")?.takeIf { it != -1 }
        
        _uiState.update { 
            it.copy(
                lockedCategoryId = lockedCategoryId,
                lockedTagId = lockedTagId
            )
        }
        
        Log.d("NotesViewModel", "init - lockedCategoryId: $lockedCategoryId, lockedTagId: $lockedTagId")
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                getNotesUseCase(),
                getCategoriesUseCase(),
                getTagsUseCase(),
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
                val viewMode = getNotesViewModeUseCase()
                Log.d("NotesViewModel", "Notes View Mode retrieved -> $viewMode")
                
                _uiState.update { currentState ->
                    val filteredCategories = if (currentState.lockedCategoryId != null) {
                        categories.filter { it.id == currentState.lockedCategoryId }
                    } else if (currentState.lockedTagId != null) {
                        val tagNotes = notes.filter { note -> note.tags.any { it.id == currentState.lockedTagId } }
                        val tagCategoryIds = tagNotes.mapNotNull { it.category?.id }.toSet()
                        categories.filter { it.id in tagCategoryIds }
                    } else {
                        categories
                    }

                    val filteredTags = if (currentState.lockedTagId != null) {
                        tags.filter { it.id == currentState.lockedTagId }
                    } else if (currentState.lockedCategoryId != null) {
                        val categoryNotes = notes.filter { it.category?.id == currentState.lockedCategoryId }
                        val categoryTagIds = categoryNotes.flatMap { note -> note.tags.map { it.id } }.toSet()
                        tags.filter { it.id in categoryTagIds }
                    } else {
                        tags
                    }

                    // Initialize selected categories and tags based on locks if they are empty
                    val selectedCategories = if (currentState.lockedCategoryId != null && currentState.selectedCategories.isEmpty()) {
                        filteredCategories.filter { it.id == currentState.lockedCategoryId }
                    } else {
                        currentState.selectedCategories
                    }
                    
                    val selectedTags = if (currentState.lockedTagId != null && currentState.selectedTags.isEmpty()) {
                        filteredTags.filter { it.id == currentState.lockedTagId }
                    } else {
                        currentState.selectedTags
                    }

                    currentState.copy(
                        isLoading = false,
                        notes = notes,
                        categories = filteredCategories,
                        tags = filteredTags,
                        selectedCategories = selectedCategories,
                        selectedTags = selectedTags,
                        filteredNotes = filterNotes(
                            notes,
                            currentState.searchQuery,
                            selectedCategories,
                            selectedTags,
                            currentState.filterPinned,
                            currentState.filterFavorites
                        ),
                        viewMode = viewMode.getOrNull() ?: ViewMode.LIST
                    )
                }
            }
        }
    }

    fun searchNotes(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    /**
     * Toggle a category selection. If already selected, it will be removed.
     * If not selected, it will be added to the selection.
     */
    fun toggleCategory(category: Category) {
        val currentState = _uiState.value
        if (category.id == currentState.lockedCategoryId) return // Prevent toggling locked category
        
        val currentCategories = currentState.selectedCategories.toMutableList()
        if (currentCategories.any { it.id == category.id }) {
            currentCategories.removeAll { it.id == category.id }
        } else {
            currentCategories.add(category)
        }
        _uiState.update { it.copy(selectedCategories = currentCategories) }
        applyFilters()
    }

    /**
     * Set the selected categories directly (for bulk operations)
     */
    fun filterByCategories(categories: List<Category>) {
        _uiState.update { it.copy(selectedCategories = categories) }
        applyFilters()
    }

    /**
     * Toggle a tag selection. If already selected, it will be removed.
     * If not selected, it will be added to the selection.
     */
    fun toggleTag(tag: Tag) {
        val currentState = _uiState.value
        if (tag.id == currentState.lockedTagId) return // Prevent toggling locked tag
        
        val currentTags = currentState.selectedTags.toMutableList()
        if (currentTags.any { it.id == tag.id }) {
            currentTags.removeAll { it.id == tag.id }
        } else {
            currentTags.add(tag)
        }
        _uiState.update { it.copy(selectedTags = currentTags) }
        applyFilters()
    }

    fun filterByTags(tags: List<Tag>) {
        _uiState.update { it.copy(selectedTags = tags) }
        applyFilters()
    }

    /**
     * Toggle the pinned filter on/off
     */
    fun toggleFilterPinned() {
        val newValue = !_uiState.value.filterPinned
        _uiState.update { it.copy(filterPinned = newValue) }
        applyFilters()
    }

    /**
     * Toggle the favorites filter on/off
     */
    fun toggleFilterFavorites() {
        val newValue = !_uiState.value.filterFavorites
        _uiState.update { it.copy(filterFavorites = newValue) }
        applyFilters()
    }

    /**
     * Apply all current filters to the notes list
     */
    private fun applyFilters() {
        val currentState = _uiState.value
        val filteredNotes = filterNotes(
            currentState.notes,
            currentState.searchQuery,
            currentState.selectedCategories,
            currentState.selectedTags,
            currentState.filterPinned,
            currentState.filterFavorites
        )
        _uiState.update { it.copy(filteredNotes = filteredNotes) }
    }

    fun clearFilters() {
        _uiState.update { 
            val preservedCategories = it.categories.filter { cat -> cat.id == it.lockedCategoryId }
            val preservedTags = it.tags.filter { tag -> tag.id == it.lockedTagId }
            it.copy(
                searchQuery = "", 
                selectedCategories = preservedCategories, 
                selectedTags = preservedTags,
                filterPinned = false,
                filterFavorites = false
            ) 
        }
        applyFilters()
    }

    fun toggleNotePin(noteId: Int) {
        viewModelScope.launch {
            val note = _uiState.value.notes.find { it.id == noteId } ?: return@launch
            toggleNotePinnedUseCase(noteId, !note.isPinned).fold(
                onSuccess = {
                    snackbarManager.showMessage(
                        SnackbarMessage.Success(
                            messageRes = if (!note.isPinned) R.string.note_pinned else R.string.note_unpinned
                        )
                    )
                },
                onFailure = {
                    snackbarManager.showMessage(
                        SnackbarMessage.Error(messageRes = R.string.failed_to_update_note)
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
                        SnackbarMessage.Success(
                            messageRes = if (!note.isFavorite) R.string.added_to_favorites else R.string.removed_from_favorites
                        )
                    )
                },
                onFailure = {
                    snackbarManager.showMessage(
                        SnackbarMessage.Error(messageRes = R.string.failed_to_update_note)
                    )
                }
            )
        }
    }

    fun archiveNote(noteId: Int) {
        viewModelScope.launch {
            archiveNoteUseCase(noteId, true).fold(
                onSuccess = {
                    snackbarManager.showMessage(
                        SnackbarMessage.Success(messageRes = R.string.note_archived)
                    )
                },
                onFailure = {
                    snackbarManager.showMessage(
                        SnackbarMessage.Error(messageRes = R.string.failed_to_archive_note)
                    )
                }
            )
        }
    }

    fun deleteNote(noteId: Int) {
        viewModelScope.launch {
            deleteNoteUseCase(noteId).fold(
                onSuccess = {
                    snackbarManager.showMessage(
                        SnackbarMessage.Success(messageRes = R.string.note_deleted)
                    )
                },
                onFailure = {
                    snackbarManager.showMessage(
                        SnackbarMessage.Error(messageRes = R.string.failed_to_delete_note)
                    )
                }
            )
        }
    }

    fun changeViewMode(viewMode: ViewMode) {
        viewModelScope.launch {
            Log.d("NotesViewModel", "ViewMode Changed to -> $viewMode")
            setNotesViewModeUseCase.invoke(viewMode)
            _uiState.update { it.copy(viewMode = viewMode) }
        }
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
        selectedCategories: List<Category>,
        selectedTags: List<Tag>,
        filterPinned: Boolean,
        filterFavorites: Boolean
    ): List<Note> {
        var filtered = notes

        // Filter by search query (always applied as AND with other filters)
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { note ->
                note.title.contains(searchQuery, ignoreCase = true) ||
                note.content.contains(searchQuery, ignoreCase = true)
            }
        }

        // Filter by categories, tags, pinned, and favorites using OR logic
        // A note matches if it satisfies ANY of the active filter criteria
        val hasFilters = selectedCategories.isNotEmpty() || selectedTags.isNotEmpty() || filterPinned || filterFavorites
        if (hasFilters) {
            filtered = filtered.filter { note ->
                val matchesCategory = selectedCategories.isNotEmpty() && 
                    selectedCategories.any { category -> note.category?.id == category.id }
                val matchesTag = selectedTags.isNotEmpty() && 
                    selectedTags.any { selectedTag -> note.tags.any { noteTag -> noteTag.id == selectedTag.id } }
                val matchesPinned = filterPinned && note.isPinned
                val matchesFavorite = filterFavorites && note.isFavorite
                
                // OR logic: match if note satisfies ANY active filter
                matchesCategory || matchesTag || matchesPinned || matchesFavorite
            }
        }

        // Apply relevance-based sorting when filters or search are active
        val hasActiveFilters = searchQuery.isNotBlank() || hasFilters
        return if (hasActiveFilters) {
            sortByRelevance(filtered, searchQuery, selectedCategories, selectedTags, filterPinned, filterFavorites)
        } else {
            sortNotes(filtered, _uiState.value.sortOrder)
        }
    }

    /**
     * Sorts notes by relevance score (best match first).
     * Score is calculated based on:
     * - Search query matches: +2 for title match, +1 for content match
     * - Category matches: +1 for each matching category
     * - Tag matches: +1 for each matching tag
     * - Pinned/Favorites: +1 if filter is active and note matches
     * Pinned notes always appear first within their relevance tier.
     */
    private fun sortByRelevance(
        notes: List<Note>,
        searchQuery: String,
        selectedCategories: List<Category>,
        selectedTags: List<Tag>,
        filterPinned: Boolean,
        filterFavorites: Boolean
    ): List<Note> {
        return notes.sortedWith(
            compareByDescending<Note> { it.isPinned }
                .thenByDescending { note -> 
                    calculateRelevanceScore(note, searchQuery, selectedCategories, selectedTags, filterPinned, filterFavorites) 
                }
                .thenByDescending { it.updatedAt } // Tiebreaker: most recently updated
        )
    }

    /**
     * Calculates a relevance score for a note based on how well it matches the filters.
     */
    private fun calculateRelevanceScore(
        note: Note,
        searchQuery: String,
        selectedCategories: List<Category>,
        selectedTags: List<Tag>,
        filterPinned: Boolean,
        filterFavorites: Boolean
    ): Int {
        var score = 0

        // Search query scoring
        if (searchQuery.isNotBlank()) {
            if (note.title.contains(searchQuery, ignoreCase = true)) {
                score += 2 // Title match is more relevant
            }
            if (note.content.contains(searchQuery, ignoreCase = true)) {
                score += 1
            }
        }

        // Category scoring: +1 for each matching category
        if (selectedCategories.isNotEmpty() && note.category != null) {
            val categoryMatchCount = selectedCategories.count { it.id == note.category?.id }
            score += categoryMatchCount
        }

        // Tag scoring: +1 for each matching tag
        if (selectedTags.isNotEmpty()) {
            val tagMatchCount = selectedTags.count { selectedTag ->
                note.tags.any { noteTag -> noteTag.id == selectedTag.id }
            }
            score += tagMatchCount
        }

        // Pinned/Favorites scoring
        if (filterPinned && note.isPinned) score += 1
        if (filterFavorites && note.isFavorite) score += 1

        return score
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
    val selectedCategories: List<Category> = emptyList(),
    val selectedTags: List<Tag> = emptyList(),
    val filterPinned: Boolean = false,
    val filterFavorites: Boolean = false,
    val lockedCategoryId: Int? = null,
    val lockedTagId: Int? = null,
    val viewMode: ViewMode = ViewMode.LIST,
    val sortOrder: SortOrder = SortOrder.UPDATED_DESC,
    val errorMessage: String? = null
)
