package com.cbo.notes.domain.usecase

import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(userId: Int): Flow<List<Note>> {
        return noteRepository.getNotesByUser(userId)
    }
}

class GetFavoriteNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(userId: Int): Flow<List<Note>> {
        return noteRepository.getFavoriteNotesByUser(userId)
    }
}

class GetArchivedNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(userId: Int): Flow<List<Note>> {
        return noteRepository.getArchivedNotesByUser(userId)
    }
}

class SearchNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(userId: Int, searchQuery: String): Flow<List<Note>> {
        return noteRepository.searchNotes(userId, searchQuery)
    }
}
