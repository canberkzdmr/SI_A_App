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
    /**
     * Soft deletes a note - marks it as deleted with a timestamp.
     * The note will be permanently deleted after 7 days.
     */
    suspend operator fun invoke(noteId: Int): Result<Unit> {
        return noteRepository.softDeleteNote(noteId)
    }
}

class RestoreDeletedNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    /**
     * Restores a soft-deleted note back to the active notes list.
     */
    suspend operator fun invoke(noteId: Int): Result<Unit> {
        return noteRepository.restoreDeletedNote(noteId)
    }
}

class PermanentlyDeleteNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    /**
     * Permanently deletes a note from the database.
     */
    suspend operator fun invoke(noteId: Int): Result<Unit> {
        return noteRepository.permanentlyDeleteNote(noteId)
    }
}

class CleanupExpiredDeletedNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    /**
     * Permanently deletes all notes that have been soft-deleted for longer than 7 days.
     * @return The number of notes that were permanently deleted.
     */
    suspend operator fun invoke(): Result<Int> {
        return noteRepository.cleanupExpiredDeletedNotes()
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
