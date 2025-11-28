package com.cbo.notes.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.core.domain.model.ViewMode
import com.cbo.notes.R
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.usecase.ArchiveNoteUseCase
import com.cbo.notes.domain.usecase.GetArchivedNotesUseCase
import com.cbo.notes.domain.usecase.GetDeletedNotesUseCase
import com.cbo.notes.domain.usecase.PermanentlyDeleteNoteUseCase
import com.cbo.notes.domain.usecase.RestoreDeletedNoteUseCase
import com.cbo.notes.presentation.SortOrder
import com.cbo.notes.presentation.screen.toDeleteArchiveMode
import com.cbo.notes.presentation.screen.toTabIndex
import com.cbo.ui.snackbar.SnackbarManager
import com.cbo.ui.snackbar.SnackbarMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeletedArchivedNotesViewModel
    @Inject
    constructor(
        private val savedStateHandle: SavedStateHandle,
        private val getArchivedNotesUseCase: GetArchivedNotesUseCase,
        private val getDeletedNotesUseCase: GetDeletedNotesUseCase,
        private val setArchivedStatusUseCase: ArchiveNoteUseCase,
        private val restoreDeletedNoteUseCase: RestoreDeletedNoteUseCase,
        private val permanentlyDeleteNoteUseCase: PermanentlyDeleteNoteUseCase,
        private val snackbarManager: SnackbarManager,
    ) : ViewModel() {
        val selectedTab: DeleteArchiveMode =
            savedStateHandle.get<Int>("tabId")?.toDeleteArchiveMode()
                ?: DeleteArchiveMode.DELETE
        private val _uiState = MutableStateFlow(DeletedArchivedNotesUiState())
        val uiState: StateFlow<DeletedArchivedNotesUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                switchTab(selectedTab.toTabIndex())
                delay(1500)
                loadData()
            }
        }

        fun loadData() {
            Log.d("DeletedArchivedNotesViewModel", "loadData()")
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }

                combine(
                    getDeletedNotesUseCase(),
                    getArchivedNotesUseCase(),
                ) { deletedNotes, archivedNotes ->
                    Log.d("DeletedArchivedNotesViewModel", "deletedNotes: $deletedNotes\narchivedNotes: $archivedNotes")
                    Pair(deletedNotes, archivedNotes)
                }.catch { throwable ->
                    Log.e("DeletedArchivedNotesViewModel", "Could not get deleted/archived notes:\n\t${throwable.message}")
                    _uiState.update { it.copy(isLoading = false) }
                }.collect { (deletedNotes, archivedNotes) ->
                    Log.d("DeletedArchivedNotesViewModel", "deletedNotes: $deletedNotes\narchivedNotes: $archivedNotes")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            deletedNotes = deletedNotes,
                            filteredDeletedNotes = deletedNotes,
                            archivedNotes = archivedNotes,
                            filteredArchivedNotes = archivedNotes,
                        )
                    }
                }
            }
        }

        fun switchTab(selectedTab: Int) {
            Log.d("DeletedArchivedNotesViewModel", "ViewMode changed to -> ${selectedTab.toDeleteArchiveMode()}")
            _uiState.update {
                it.copy(
                    viewMode = selectedTab.toDeleteArchiveMode()
                )
            }
        }

        fun restoreNote(noteId: Int) {
            Log.d("DeletedArchivedNotesViewModel", "restoreNote Id: $noteId, viewMode: ${_uiState.value.viewMode}")
            viewModelScope.launch {
                val result = when (_uiState.value.viewMode) {
                    DeleteArchiveMode.DELETE -> restoreDeletedNoteUseCase.invoke(noteId)
                    DeleteArchiveMode.ARCHIVE -> setArchivedStatusUseCase.invoke(noteId = noteId, isArchived = false)
                }
                
                result.fold(
                    onSuccess = {
                        snackbarManager.showMessage(SnackbarMessage.Success(messageRes = R.string.note_restored))
                    },
                    onFailure = { error ->
                        Log.e("DeletedArchivedNotesViewModel", "Failed to restore note: ${error.message}")
                        snackbarManager.showMessage(
                            SnackbarMessage.Error(messageRes = R.string.failed_to_restore_note)
                        )
                    }
                )
            }
        }

        fun permanentlyDeleteNote(noteId: Int) {
            Log.d("DeletedArchivedNotesViewModel", "permanentlyDeleteNote Id: $noteId")
            viewModelScope.launch {
                permanentlyDeleteNoteUseCase.invoke(noteId).fold(
                    onSuccess = {
                        snackbarManager.showMessage(
                            SnackbarMessage.Success(messageRes = R.string.note_permanently_deleted)
                        )
                    },
                    onFailure = { error ->
                        Log.e("DeletedArchivedNotesViewModel", "Failed to permanently delete note: ${error.message}")
                        snackbarManager.showMessage(
                            SnackbarMessage.Error(messageRes = R.string.failed_to_permanently_delete_note)
                        )
                    }
                )
            }
        }
    }

data class DeletedArchivedNotesUiState(
    val isLoading: Boolean = false,
    val archivedNotes: List<Note> = emptyList(),
    val filteredArchivedNotes: List<Note> = emptyList(),
    val deletedNotes: List<Note> = emptyList(),
    val filteredDeletedNotes: List<Note> = emptyList(),
    val searchQuery: String = "",
    val listMode: ViewMode = ViewMode.COMPACT,
    val viewMode: DeleteArchiveMode = DeleteArchiveMode.ARCHIVE,
    val sortOrder: SortOrder = SortOrder.UPDATED_DESC,
)

enum class DeleteArchiveMode {
    DELETE,
    ARCHIVE,
}
