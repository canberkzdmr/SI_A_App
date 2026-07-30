package com.cbo.core.database

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cbo.core.database.database.AppDatabase
import com.cbo.core.database.database.MIGRATION_1_2
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    companion object {
        private const val TEST_DB_NAME = "migration-test"
    }

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @After
    @Throws(IOException::class)
    fun closeDb() {
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(TEST_DB_NAME)
    }

    @Test
    @Throws(IOException::class)
    fun migrate1To2_AddsAttachmentsColumn() {
        // Create database at version 1
        helper.createDatabase(TEST_DB_NAME, 1).apply {
            execSQL(
                """
                CREATE TABLE IF NOT EXISTS `notes` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `userId` INTEGER NOT NULL, 
                    `title` TEXT NOT NULL, 
                    `content` TEXT NOT NULL, 
                    `categoryId` INTEGER, 
                    `createdAt` INTEGER NOT NULL, 
                    `updatedAt` INTEGER NOT NULL, 
                    `isPinned` INTEGER NOT NULL, 
                    `isArchived` INTEGER NOT NULL, 
                    `isFavorite` INTEGER NOT NULL, 
                    `isDeleted` INTEGER NOT NULL, 
                    `deletedAt` INTEGER, 
                    `reminderTime` INTEGER, 
                    `zettelId` TEXT
                )
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO notes (id, userId, title, content, createdAt, updatedAt, isPinned, isArchived, isFavorite, isDeleted) 
                VALUES (1, 1, 'Test Note', 'Content', ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0, 0, 0, 0)
                """.trimIndent()
            )
            close()
        }

        // Run migration 1→2
        val db = helper.runMigrationsAndValidate(TEST_DB_NAME, 2, true, MIGRATION_1_2)

        // Verify attachments column was added
        val cursor = db.query("SELECT attachments FROM notes WHERE id = 1")
        assertTrue("Note should exist", cursor.moveToFirst())
        assertEquals("attachments should default to empty string", "", cursor.getString(cursor.getColumnIndexOrThrow("attachments")))
        cursor.close()

        db.close()
    }
}
