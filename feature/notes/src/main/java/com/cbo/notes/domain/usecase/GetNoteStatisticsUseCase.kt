package com.cbo.notes.domain.usecase

import com.cbo.core.session.UserSession
import com.cbo.notes.domain.model.NoteStatistics
import com.cbo.notes.domain.repository.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Oturum açmış kullanıcıya ait tüm istatistikleri hesaplayıp döndürür.
 *
 * UserSession'dan mevcut kullanıcı ID'sini alır ve
 * NoteRepository.getNoteStatistics() çağrısına iletir.
 * Kullanıcı oturum açmamışsa boş bir NoteStatistics döndürür.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GetNoteStatisticsUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val userSession: UserSession,
) {
    operator fun invoke(): Flow<NoteStatistics> {
        return userSession.currentUser.flatMapLatest { user ->
            if (user == null) {
                flowOf(NoteStatistics())
            } else {
                flow {
                    emit(noteRepository.getNoteStatistics(user.id))
                }
            }
        }
    }
}
