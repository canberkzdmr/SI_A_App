package com.cbo.core.domain.usecase

import com.cbo.core.domain.model.RestoreSummary
import com.cbo.core.domain.repository.BackupRepository
import javax.inject.Inject

class RestoreBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(userId: Int, jsonContent: String): RestoreSummary {
        return backupRepository.restoreBackup(userId, jsonContent)
    }
}
