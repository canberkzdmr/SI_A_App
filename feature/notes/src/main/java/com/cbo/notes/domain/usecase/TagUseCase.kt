package com.cbo.notes.domain.usecase

import com.cbo.core.session.UserSession
import com.cbo.notes.domain.model.Tag
import com.cbo.notes.domain.repository.TagRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class GetTagsUseCase @Inject constructor(
    private val tagRepository: TagRepository,
    private val userSession: UserSession
) {
    operator fun invoke(): Flow<List<Tag>> {
        return userSession.currentUser.flatMapLatest { user ->
            user?.let { tagRepository.getTagsByUser(it.id) } ?: flowOf(emptyList())
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SearchTagsUseCase @Inject constructor(
    private val tagRepository: TagRepository,
    private val userSession: UserSession
) {
    operator fun invoke(query: String): Flow<List<Tag>> {
        return userSession.currentUser.flatMapLatest { user ->
            user?.let { tagRepository.searchTags(it.id, query) } ?: flowOf(emptyList())
        }
    }
}

class CreateTagUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(tag: Tag): Result<Tag> {
        return tagRepository.insertTag(tag)
    }
}

class UpdateTagUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(tag: Tag): Result<Tag> {
        return tagRepository.updateTag(tag)
    }
}

class DeleteTagUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(tagId: Int): Result<Unit> {
        return tagRepository.deleteTag(tagId)
    }
}

class DeleteTagListUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(tagList: List<Tag>): Result<Unit> {
        return tagRepository.deleteTagList(tagList)
    }
}

class GetMostUsedTagsUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(userId: Int, limit: Int = 10): List<Tag> {
        return tagRepository.getMostUsedTags(userId, limit)
    }
}
