package com.cbo.notes.domain.usecase

import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.model.NoteLink
import com.cbo.notes.domain.model.NoteTemplate
import com.cbo.notes.domain.repository.NoteLinkRepository
import com.cbo.notes.domain.repository.NoteTemplateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNoteTemplatesUseCase @Inject constructor(
    private val repository: NoteTemplateRepository
) {
    operator fun invoke(userId: Int): Flow<List<NoteTemplate>> {
        return repository.getTemplatesForUser(userId)
    }
}

class AddNoteTemplateUseCase @Inject constructor(
    private val repository: NoteTemplateRepository
) {
    suspend operator fun invoke(template: NoteTemplate): Long {
        return repository.addTemplate(template)
    }
}

class DeleteNoteTemplateUseCase @Inject constructor(
    private val repository: NoteTemplateRepository
) {
    suspend operator fun invoke(id: Int) {
        repository.deleteTemplateById(id)
    }
}

class GetBacklinksForNoteUseCase @Inject constructor(
    private val repository: NoteLinkRepository
) {
    operator fun invoke(noteId: Int): Flow<List<Note>> {
        return repository.getBacklinksForNote(noteId)
    }
}

class AddNoteLinkUseCase @Inject constructor(
    private val repository: NoteLinkRepository
) {
    suspend operator fun invoke(link: NoteLink): Long {
        return repository.addLink(link)
    }
}

class DeleteAllLinksForNoteUseCase @Inject constructor(
    private val repository: NoteLinkRepository
) {
    suspend operator fun invoke(noteId: Int) {
        repository.deleteAllLinksForNote(noteId)
    }
}
