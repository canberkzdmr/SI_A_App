package com.cbo.notes.domain.usecase

import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for setting a reminder on a note.
 */
class SetReminderUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(noteId: Int, reminderTime: Long): Result<Unit> {
        require(reminderTime > System.currentTimeMillis()) { "Reminder time must be in the future" }
        return noteRepository.setReminder(noteId, reminderTime)
    }
}

/**
 * Use case for removing a reminder from a note.
 */
class RemoveReminderUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(noteId: Int): Result<Unit> {
        return noteRepository.removeReminder(noteId)
    }
}

/**
 * Use case for getting all notes with active (future) reminders.
 */
class GetNotesWithRemindersUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(): Flow<List<Note>> {
        return noteRepository.getNotesWithActiveReminders()
    }
}

/**
 * Use case for getting notes with reminders in a specific time range.
 * Useful for showing upcoming reminders.
 */
class GetUpcomingRemindersUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    /**
     * Gets notes with reminders scheduled within the next [hoursAhead] hours.
     */
    suspend operator fun invoke(hoursAhead: Int = 24): List<Note> {
        val currentTime = System.currentTimeMillis()
        val endTime = currentTime + (hoursAhead * 60 * 60 * 1000L)
        return noteRepository.getNotesWithRemindersBetween(currentTime, endTime)
    }
}


