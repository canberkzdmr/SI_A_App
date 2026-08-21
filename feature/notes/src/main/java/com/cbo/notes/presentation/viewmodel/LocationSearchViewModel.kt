package com.cbo.notes.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.notes.domain.model.LocationSearchResult
import com.cbo.notes.domain.repository.LocationSearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationSearchViewModel @Inject constructor(
    private val locationSearchRepository: LocationSearchRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<LocationSearchResult>>(emptyList())
    val searchResults: StateFlow<List<LocationSearchResult>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
        
        searchJob?.cancel()
        if (newQuery.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        // Debounce mekanizması: Kullanıcı yazarken her harfte API'ye gitmemesi için ufak bir bekleme (300ms)
        searchJob = viewModelScope.launch {
            delay(300)
            _isLoading.value = true
            
            val result = locationSearchRepository.searchLocation(newQuery)
            if (result.isSuccess) {
                _searchResults.value = result.getOrNull() ?: emptyList()
            }
            
            _isLoading.value = false
        }
    }

    /**
     * Seçilen yerin (sadece text'i olan) detaylarını (Enlem/Boylam) çekmek için kullanılır
     */
    fun onLocationSelected(placeId: String, onResult: (LocationSearchResult?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = locationSearchRepository.getLocationDetails(placeId)
            if (result.isSuccess) {
                onResult(result.getOrNull())
                // Arama sonuçlarını temizle
                _searchQuery.value = ""
                _searchResults.value = emptyList()
            } else {
                onResult(null)
            }
            _isLoading.value = false
        }
    }
}
