package com.cbo.notes.domain.repository

import com.cbo.core.domain.model.ViewMode

interface UserSettingsRepository {
    suspend fun getNotesViewMode(userId: Int): Result<ViewMode>
    suspend fun setNotesViewMode(userId: Int, viewMode: ViewMode): Result<Unit>
}