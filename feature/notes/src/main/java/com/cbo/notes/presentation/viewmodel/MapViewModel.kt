package com.cbo.notes.presentation.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.model.Tag
import com.cbo.notes.domain.usecase.GetCategoriesUseCase
import com.cbo.notes.domain.usecase.GetNotesUseCase
import com.cbo.notes.domain.usecase.GetTagsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val isLoading: Boolean = true,
    val allLocationNotes: List<Note> = emptyList(), // Sadece lokasyonu olan notlar (ham)
    val filteredNotes: List<Note> = emptyList(), // Filtrelenmiş notlar (haritada gösterilecek)
    val categories: List<Category> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val selectedCategories: Set<Int> = emptySet(),
    val selectedTags: Set<Int> = emptySet(),
    val isNearbyFilterEnabled: Boolean = false,
    val currentLocation: Location? = null,
    val selectedNote: Note? = null // Bottom sheet'te önizleme için
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getNotesUseCase: GetNotesUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getTagsUseCase: GetTagsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                getNotesUseCase(),
                getCategoriesUseCase(),
                getTagsUseCase()
            ) { notes, categories, tags ->
                Triple(notes, categories, tags)
            }.catch { throwable ->
                _uiState.update { it.copy(isLoading = false) }
            }.collect { (notes, categories, tags) ->
                // Sadece lokasyonu olan, silinmemiş ve arşivlenmemiş notları filtrele
                val locationNotes = notes.filter { 
                    !it.isDeleted && !it.isArchived && 
                    it.reminderLatitude != null && 
                    it.reminderLongitude != null 
                }

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        categories = categories,
                        tags = tags,
                        allLocationNotes = locationNotes
                    )
                }
                applyFilters()
            }
        }
    }

    fun toggleCategory(category: Category) {
        _uiState.update { state ->
            val currentSelected = state.selectedCategories.toMutableSet()
            if (currentSelected.contains(category.id)) {
                currentSelected.remove(category.id)
            } else {
                currentSelected.add(category.id)
            }
            state.copy(selectedCategories = currentSelected)
        }
        applyFilters()
    }

    fun toggleTag(tag: Tag) {
        _uiState.update { state ->
            val currentSelected = state.selectedTags.toMutableSet()
            if (currentSelected.contains(tag.id)) {
                currentSelected.remove(tag.id)
            } else {
                currentSelected.add(tag.id)
            }
            state.copy(selectedTags = currentSelected)
        }
        applyFilters()
    }

    fun toggleNearbyFilter() {
        _uiState.update { state ->
            state.copy(isNearbyFilterEnabled = !state.isNearbyFilterEnabled)
        }
        applyFilters()
    }

    fun updateCurrentLocation(location: Location?) {
        _uiState.update { state ->
            state.copy(currentLocation = location)
        }
        if (_uiState.value.isNearbyFilterEnabled) {
            applyFilters()
        }
    }

    fun selectNote(note: Note?) {
        _uiState.update { it.copy(selectedNote = note) }
    }

    fun clearFilters() {
        _uiState.update { state ->
            state.copy(
                selectedCategories = emptySet(),
                selectedTags = emptySet(),
                isNearbyFilterEnabled = false
            )
        }
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        var filtered = state.allLocationNotes

        // Kategori Filtresi
        if (state.selectedCategories.isNotEmpty()) {
            filtered = filtered.filter { note ->
                val catId = note.category?.id
                catId != null && state.selectedCategories.contains(catId)
            }
        }

        // Tag Filtresi
        if (state.selectedTags.isNotEmpty()) {
            filtered = filtered.filter { note ->
                note.tags.any { tag -> state.selectedTags.contains(tag.id) }
            }
        }

        // Yakınımdakiler Filtresi (Mevcut konuma göre ~5km / 5000 metre)
        if (state.isNearbyFilterEnabled && state.currentLocation != null) {
            filtered = filtered.filter { note ->
                if (note.reminderLatitude != null && note.reminderLongitude != null) {
                    val noteLoc = Location("").apply {
                        latitude = note.reminderLatitude
                        longitude = note.reminderLongitude
                    }
                    val distance = state.currentLocation.distanceTo(noteLoc) // Metre cinsinden
                    distance <= 5000f
                } else {
                    false
                }
            }
        }

        _uiState.update { it.copy(filteredNotes = filtered) }
    }
}
