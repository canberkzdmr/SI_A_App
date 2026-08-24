package com.cbo.notes.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.usecase.GetNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class CalendarUiState(
    val isLoading: Boolean = true,
    val reminderNotes: List<Note> = emptyList(),
    val notesByDate: Map<LocalDate, List<Note>> = emptyMap(),
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedDateNotes: List<Note> = emptyList()
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getNotesUseCase: GetNotesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            getNotesUseCase().catch { 
                _uiState.update { state -> state.copy(isLoading = false) }
            }.collect { notes ->
                // Sadece hatırlatıcısı olan (silinmemiş) notları al (Geçmiş veya gelecek hepsi)
                val reminders = notes.filter { 
                    !it.isDeleted && it.reminderTime != null 
                }
                
                // Notları LocalDate'e göre grupla
                val groupedNotes = reminders.groupBy { note ->
                    Instant.ofEpochMilli(note.reminderTime!!)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }

                _uiState.update { state ->
                    val newSelectedDateNotes = groupedNotes[state.selectedDate] ?: emptyList()
                    state.copy(
                        isLoading = false,
                        reminderNotes = reminders,
                        notesByDate = groupedNotes,
                        selectedDateNotes = newSelectedDateNotes
                    )
                }
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { state ->
            val notesForDate = state.notesByDate[date] ?: emptyList()
            state.copy(
                selectedDate = date,
                selectedDateNotes = notesForDate
            )
        }
    }
}
