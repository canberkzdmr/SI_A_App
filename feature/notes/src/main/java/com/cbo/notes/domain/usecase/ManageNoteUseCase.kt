package com.cbo.notes.domain.usecase

import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.repository.NoteRepository
import javax.inject.Inject

class CreateNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(note: Note): Result<Note> {
        return noteRepository.insertNote(note)
    }
}

class UpdateNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(note: Note): Result<Note> {
        return noteRepository.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
    }
}

class DeleteNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(noteId: Int): Result<Unit> {
        return noteRepository.deleteNote(noteId)
    }
}

class GetNoteByIdUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(noteId: Int): Note? {
        return noteRepository.getNoteById(noteId)
    }
}

class ToggleNotePinnedUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(noteId: Int, isPinned: Boolean): Result<Unit> {
        return noteRepository.updatePinnedStatus(noteId, isPinned)
    }
}

class ToggleNoteFavoriteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(noteId: Int, isFavorite: Boolean): Result<Unit> {
        return noteRepository.updateFavoriteStatus(noteId, isFavorite)
    }
}

class ArchiveNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(noteId: Int, isArchived: Boolean): Result<Unit> {
        return noteRepository.updateArchivedStatus(noteId, isArchived)
    }
}
