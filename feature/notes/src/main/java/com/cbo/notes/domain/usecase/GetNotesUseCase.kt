package com.cbo.notes.domain.usecase

import com.cbo.core.session.UserSession
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.repository.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class GetNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val userSession: UserSession
) {
    operator fun invoke(): Flow<List<Note>> {
        return userSession.currentUser.flatMapLatest { user ->
            user?.let { noteRepository.getNotesByUser(it.id) } ?: flowOf(emptyList())
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class GetFavoriteNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val userSession: UserSession
) {
    operator fun invoke(): Flow<List<Note>> {
        return userSession.currentUser.flatMapLatest { user ->
            user?.let { noteRepository.getFavoriteNotesByUser(it.id) } ?: flowOf(emptyList())
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class GetArchivedNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val userSession: UserSession
) {
    operator fun invoke(): Flow<List<Note>> {
        return userSession.currentUser.flatMapLatest { user ->
            user?.let { noteRepository.getArchivedNotesByUser(it.id) } ?: flowOf(emptyList())
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class GetDeletedNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val userSession: UserSession
) {
    operator fun invoke(): Flow<List<Note>> {
        return userSession.currentUser.flatMapLatest { user ->
            user?.let { noteRepository.getDeletedNotesByUser(it.id) } ?: flowOf(emptyList())
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SearchNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val userSession: UserSession
) {
    operator fun invoke(searchQuery: String): Flow<List<Note>> {
        return userSession.currentUser.flatMapLatest { user ->
            user?.let { noteRepository.searchNotes(it.id, searchQuery) } ?: flowOf(emptyList())
        }
    }
}
