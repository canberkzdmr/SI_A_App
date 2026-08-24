package com.cbo.notes.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.core.session.domain.repository.SessionRepository
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val username: String = "",
    val totalNotes: Int = 0,
    val activeReminders: Int = 0,
    val pendingTodos: Int = 0,
    val pinnedNotes: List<Note> = emptyList(),
    val recentNotes: List<Note> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionRepository.currentSession
                .flatMapLatest { session ->
                    if (session != null) {
                        _uiState.update { it.copy(username = session.username) }
                        noteRepository.getNotesByUser(session.userId)
                    } else {
                        flowOf(emptyList())
                    }
                }
                .collect { notes ->
                    val activeReminders = notes.count { it.hasActiveReminder() }
                    val pendingTodos = notes.sumOf { note -> note.todos.count { !it.isDone } }
                    val pinnedNotes = notes.filter { it.isPinned }
                    val recentNotes = notes.sortedByDescending { it.updatedAt }.take(5)

                    _uiState.update { state ->
                        state.copy(
                            totalNotes = notes.size,
                            activeReminders = activeReminders,
                            pendingTodos = pendingTodos,
                            pinnedNotes = pinnedNotes,
                            recentNotes = recentNotes,
                            isLoading = false
                        )
                    }
                }
        }
    }
}
