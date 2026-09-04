package com.cbo.core.domain.repository

import com.cbo.core.domain.model.RestoreSummary

interface BackupRepository {
    suspend fun exportBackup(userId: Int): String
    suspend fun restoreBackup(userId: Int, jsonContent: String): RestoreSummary
}
