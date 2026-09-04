package com.cbo.core.domain.usecase

import com.cbo.core.domain.model.RestoreSummary
import com.cbo.core.domain.repository.BackupRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class BackupUseCasesTest {

    private val backupRepository: BackupRepository = mock()
    private lateinit var exportBackupUseCase: ExportBackupUseCase
    private lateinit var restoreBackupUseCase: RestoreBackupUseCase

    @Before
    fun setup() {
        exportBackupUseCase = ExportBackupUseCase(backupRepository)
        restoreBackupUseCase = RestoreBackupUseCase(backupRepository)
    }

    @Test
    fun exportBackupUseCase_delegatesToRepository() = runTest {
        val userId = 42
        val expectedJson = """{"version":1}"""
        whenever(backupRepository.exportBackup(userId)).thenReturn(expectedJson)

        val result = exportBackupUseCase(userId)
        assertEquals(expectedJson, result)
        verify(backupRepository).exportBackup(userId)
    }

    @Test
    fun restoreBackupUseCase_delegatesToRepository() = runTest {
        val userId = 42
        val inputJson = """{"version":1}"""
        val expectedSummary = RestoreSummary(
            notesCount = 5,
            categoriesCount = 2,
            tagsCount = 3,
            linksCount = 1,
            templatesCount = 0
        )
        whenever(backupRepository.restoreBackup(userId, inputJson)).thenReturn(expectedSummary)

        val result = restoreBackupUseCase(userId, inputJson)
        assertEquals(5, result.notesCount)
        assertEquals(2, result.categoriesCount)
        assertEquals(3, result.tagsCount)
        verify(backupRepository).restoreBackup(userId, inputJson)
    }
}
