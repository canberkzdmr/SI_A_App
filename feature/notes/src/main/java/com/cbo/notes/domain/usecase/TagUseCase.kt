package com.cbo.notes.domain.usecase

import com.cbo.notes.domain.model.Tag
import com.cbo.notes.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTagsUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    operator fun invoke(userId: Int): Flow<List<Tag>> {
        return tagRepository.getTagsByUser(userId)
    }
}

class SearchTagsUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    operator fun invoke(userId: Int, query: String): Flow<List<Tag>> {
        return tagRepository.searchTags(userId, query)
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
