package com.cbo.core.database.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class NoteWithDetails(
    @Embedded val note: NoteEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteTagCrossRef::class,
            parentColumn = "noteId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)

data class CategoryWithNotes(
    @Embedded val category: CategoryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "categoryId"
    )
    val notes: List<NoteEntity>
)

data class TagWithNotes(
    @Embedded val tag: TagEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteTagCrossRef::class,
            parentColumn = "tagId",
            entityColumn = "noteId"
        )
    )
    val notes: List<NoteEntity>
)
