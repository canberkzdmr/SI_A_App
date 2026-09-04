package com.cbo.core.domain.usecase

import com.cbo.core.domain.repository.BackupRepository
import javax.inject.Inject

class ExportBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(userId: Int): String {
        return backupRepository.exportBackup(userId)
    }
}
