package com.cbo.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.cbo.core.database.entity.NoteTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteTemplateDao : BaseDao<NoteTemplateEntity> {
    @Query("SELECT * FROM note_templates WHERE userId = :userId ORDER BY name ASC")
    fun getTemplatesForUser(userId: Int): Flow<List<NoteTemplateEntity>>

    @Query("SELECT * FROM note_templates WHERE id = :id")
    suspend fun getTemplateById(id: Int): NoteTemplateEntity?

    @Query("DELETE FROM note_templates WHERE id = :id")
    suspend fun deleteTemplateById(id: Int)
}
