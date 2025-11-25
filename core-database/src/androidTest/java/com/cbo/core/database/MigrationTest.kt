package com.cbo.core.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cbo.core.database.database.AppDatabase
import com.cbo.core.database.database.MIGRATION_2_3
import com.cbo.core.database.database.MIGRATION_3_4
import com.cbo.core.database.database.MIGRATION_4_5
import com.cbo.core.database.database.MIGRATION_5_6
import com.cbo.core.database.database.MIGRATION_6_7
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Comprehensive migration tests for AppDatabase
 * Tests migrations 2→3, 3→4, 4→5, 5→6, and 6→7
 */
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
        // Clean up after each test
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(TEST_DB_NAME)
    }

    @Test
    @Throws(IOException::class)
    fun migrate2To3_CreatesUserSettingsTable() {
        // Create database at version 2
        helper.createDatabase(TEST_DB_NAME, 2).apply {
            // Insert test user data
            execSQL(
                """
                INSERT INTO users (id, email, password, createdAt, updatedAt) 
                VALUES (1, 'test@example.com', 'hashedPassword123', ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
                """.trimIndent()
            )
            close()
        }

        // Run migration 2→3
        val db = helper.runMigrationsAndValidate(TEST_DB_NAME, 3, true, MIGRATION_2_3)

        // Verify user_settings table was created
        val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='user_settings'")
        assertTrue("user_settings table should exist", cursor.moveToFirst())
        cursor.close()

        // Verify default settings were inserted for existing user
        val settingsCursor = db.query("SELECT * FROM user_settings WHERE userId = 1")
        assertTrue("Settings should exist for user 1", settingsCursor.moveToFirst())
        assertEquals("isFirstLoginDone should be 0", 0, settingsCursor.getInt(settingsCursor.getColumnIndexOrThrow("isFirstLoginDone")))
        assertEquals("isBiometricsEnabled should be 0", 0, settingsCursor.getInt(settingsCursor.getColumnIndexOrThrow("isBiometricsEnabled")))
        settingsCursor.close()

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate3To4_AddsNotesViewModeColumn() {
        // Create database at version 3 with user_settings table
        helper.createDatabase(TEST_DB_NAME, 3).apply {
            // Insert test user
            execSQL(
                """
                INSERT INTO users (id, email, password, createdAt, updatedAt) 
                VALUES (1, 'test@example.com', 'hashedPassword123', ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
                """.trimIndent()
            )
            // Insert user settings
            execSQL(
                """
                INSERT INTO user_settings (userId, isFirstLoginDone, isBiometricsEnabled) 
                VALUES (1, 1, 0)
                """.trimIndent()
            )
            close()
        }

        // Run migration 3→4
        val db = helper.runMigrationsAndValidate(TEST_DB_NAME, 4, true, MIGRATION_3_4)

        // Verify notesViewMode column was added with default value
        val cursor = db.query("SELECT notesViewMode FROM user_settings WHERE userId = 1")
        assertTrue("Settings should exist for user 1", cursor.moveToFirst())
        assertEquals("notesViewMode should default to LIST", "LIST", cursor.getString(cursor.getColumnIndexOrThrow("notesViewMode")))
        cursor.close()

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate4To5_CreatesSupportedLanguagesTable() {
        // Create database at version 4
        helper.createDatabase(TEST_DB_NAME, 4).apply {
            close()
        }

        // Run migration 4→5
        val db = helper.runMigrationsAndValidate(TEST_DB_NAME, 5, true, MIGRATION_4_5)

        // Verify supported_languages table was created
        val tableCursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='supported_languages'")
        assertTrue("supported_languages table should exist", tableCursor.moveToFirst())
        tableCursor.close()

        // Verify unique index on code was created
        val indexCursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND name='index_supported_languages_code'")
        assertTrue("index_supported_languages_code should exist", indexCursor.moveToFirst())
        indexCursor.close()

        // Verify initial languages were inserted
        val languagesCursor = db.query("SELECT code, displayName, nativeName, isEnabled, sortOrder FROM supported_languages ORDER BY sortOrder")
        
        // Turkish
        assertTrue("Should have Turkish", languagesCursor.moveToFirst())
        assertEquals("tr", languagesCursor.getString(0))
        assertEquals("Turkish", languagesCursor.getString(1))
        assertEquals("Türkçe", languagesCursor.getString(2))
        assertEquals(1, languagesCursor.getInt(3))
        assertEquals(0, languagesCursor.getInt(4))
        
        // English
        assertTrue("Should have English", languagesCursor.moveToNext())
        assertEquals("en", languagesCursor.getString(0))
        assertEquals("English", languagesCursor.getString(1))
        assertEquals("English", languagesCursor.getString(2))
        assertEquals(1, languagesCursor.getInt(3))
        assertEquals(1, languagesCursor.getInt(4))
        
        // German
        assertTrue("Should have German", languagesCursor.moveToNext())
        assertEquals("de", languagesCursor.getString(0))
        assertEquals("German", languagesCursor.getString(1))
        assertEquals("Deutsch", languagesCursor.getString(2))
        assertEquals(0, languagesCursor.getInt(3))
        assertEquals(2, languagesCursor.getInt(4))
        
        languagesCursor.close()
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate5To6_AddsPreferredLanguageColumn() {
        // Create database at version 5
        helper.createDatabase(TEST_DB_NAME, 5).apply {
            // Insert test user
            execSQL(
                """
                INSERT INTO users (id, email, password, createdAt, updatedAt) 
                VALUES (1, 'test@example.com', 'hashedPassword123', ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
                """.trimIndent()
            )
            // Insert user settings without preferredLanguage
            execSQL(
                """
                INSERT INTO user_settings (userId, isFirstLoginDone, isBiometricsEnabled, notesViewMode) 
                VALUES (1, 1, 0, 'LIST')
                """.trimIndent()
            )
            close()
        }

        // Run migration 5→6
        val db = helper.runMigrationsAndValidate(TEST_DB_NAME, 6, true, MIGRATION_5_6)

        // Verify preferredLanguage column was added with default value
        val cursor = db.query("SELECT preferredLanguage FROM user_settings WHERE userId = 1")
        assertTrue("Settings should exist for user 1", cursor.moveToFirst())
        assertEquals("preferredLanguage should default to 'en'", "en", cursor.getString(cursor.getColumnIndexOrThrow("preferredLanguage")))
        cursor.close()

        // Verify all existing data was preserved
        val allDataCursor = db.query("SELECT * FROM user_settings WHERE userId = 1")
        assertTrue("Settings should exist", allDataCursor.moveToFirst())
        assertEquals(1, allDataCursor.getInt(allDataCursor.getColumnIndexOrThrow("userId")))
        assertEquals(1, allDataCursor.getInt(allDataCursor.getColumnIndexOrThrow("isFirstLoginDone")))
        assertEquals(0, allDataCursor.getInt(allDataCursor.getColumnIndexOrThrow("isBiometricsEnabled")))
        assertEquals("LIST", allDataCursor.getString(allDataCursor.getColumnIndexOrThrow("notesViewMode")))
        assertEquals("en", allDataCursor.getString(allDataCursor.getColumnIndexOrThrow("preferredLanguage")))
        allDataCursor.close()

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate6To7_EnsuresLanguagesPopulated() {
        // Create database at version 6 (simulating a database that might have skipped migration 4_5)
        helper.createDatabase(TEST_DB_NAME, 6).apply {
            // Don't insert languages - testing the idempotency
            close()
        }

        // Run migration 6→7
        val db = helper.runMigrationsAndValidate(TEST_DB_NAME, 7, true, MIGRATION_6_7)

        // Verify languages are present (INSERT OR IGNORE should work)
        val languagesCursor = db.query("SELECT COUNT(*) FROM supported_languages")
        assertTrue(languagesCursor.moveToFirst())
        val count = languagesCursor.getInt(0)
        assertTrue("Should have at least 3 languages", count >= 3)
        languagesCursor.close()

        // Verify the specific languages
        val trCursor = db.query("SELECT * FROM supported_languages WHERE code = 'tr'")
        assertTrue("Turkish should exist", trCursor.moveToFirst())
        trCursor.close()

        val enCursor = db.query("SELECT * FROM supported_languages WHERE code = 'en'")
        assertTrue("English should exist", enCursor.moveToFirst())
        enCursor.close()

        val deCursor = db.query("SELECT * FROM supported_languages WHERE code = 'de'")
        assertTrue("German should exist", deCursor.moveToFirst())
        deCursor.close()

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrateAll_From2To7() {
        // Create database at version 2
        helper.createDatabase(TEST_DB_NAME, 2).apply {
            // Insert test user
            execSQL(
                """
                INSERT INTO users (id, email, password, createdAt, updatedAt) 
                VALUES (1, 'test@example.com', 'hashedPassword123', ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
                """.trimIndent()
            )
            close()
        }

        // Run all migrations from 2 to 7
        val db = helper.runMigrationsAndValidate(
            TEST_DB_NAME, 
            7, 
            true, 
            MIGRATION_2_3, 
            MIGRATION_3_4, 
            MIGRATION_4_5, 
            MIGRATION_5_6, 
            MIGRATION_6_7
        )

        // Verify final database state
        
        // 1. user_settings should exist with all columns
        val settingsCursor = db.query("SELECT * FROM user_settings WHERE userId = 1")
        assertTrue("Settings should exist for user 1", settingsCursor.moveToFirst())
        assertNotNull(settingsCursor.getColumnIndex("userId"))
        assertNotNull(settingsCursor.getColumnIndex("isFirstLoginDone"))
        assertNotNull(settingsCursor.getColumnIndex("isBiometricsEnabled"))
        assertNotNull(settingsCursor.getColumnIndex("notesViewMode"))
        assertNotNull(settingsCursor.getColumnIndex("preferredLanguage"))
        assertEquals("LIST", settingsCursor.getString(settingsCursor.getColumnIndexOrThrow("notesViewMode")))
        assertEquals("en", settingsCursor.getString(settingsCursor.getColumnIndexOrThrow("preferredLanguage")))
        settingsCursor.close()

        // 2. supported_languages should exist with initial data
        val languagesCursor = db.query("SELECT COUNT(*) FROM supported_languages")
        assertTrue(languagesCursor.moveToFirst())
        assertEquals(3, languagesCursor.getInt(0))
        languagesCursor.close()

        // 3. Verify data integrity
        val userCursor = db.query("SELECT * FROM users WHERE id = 1")
        assertTrue("User should still exist", userCursor.moveToFirst())
        assertEquals("test@example.com", userCursor.getString(userCursor.getColumnIndexOrThrow("email")))
        userCursor.close()

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun testAutoMigrate_CreatesValidDatabase() {
        // Test that Room can auto-create and auto-migrate the database
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Create database with all migrations
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "test-auto-migrate.db"
        )
            .addMigrations(
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7
            )
            .fallbackToDestructiveMigration()
            .build()

        // Verify database can be opened and accessed
        val supportedLanguageDao = db.supportedLanguageDao()
        val languages = supportedLanguageDao.getAllLanguagesSync()
        
        assertNotNull("Languages should not be null", languages)
        assertTrue("Should have at least 3 languages", languages.size >= 3)

        db.close()
        context.deleteDatabase("test-auto-migrate.db")
    }

    @Test
    @Throws(IOException::class)
    fun migrate5To6_PreservesMultipleUsers() {
        // Test that migration preserves data for multiple users
        helper.createDatabase(TEST_DB_NAME, 5).apply {
            // Insert multiple users
            execSQL(
                """
                INSERT INTO users (id, email, password, createdAt, updatedAt) 
                VALUES 
                    (1, 'user1@example.com', 'hash1', ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                    (2, 'user2@example.com', 'hash2', ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                    (3, 'user3@example.com', 'hash3', ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
                """.trimIndent()
            )
            // Insert settings for all users
            execSQL(
                """
                INSERT INTO user_settings (userId, isFirstLoginDone, isBiometricsEnabled, notesViewMode) 
                VALUES 
                    (1, 1, 1, 'GRID'),
                    (2, 0, 0, 'LIST'),
                    (3, 1, 0, 'COMPACT')
                """.trimIndent()
            )
            close()
        }

        // Run migration 5→6
        val db = helper.runMigrationsAndValidate(TEST_DB_NAME, 6, true, MIGRATION_5_6)

        // Verify all users have settings with preferredLanguage
        val cursor = db.query("SELECT userId, notesViewMode, preferredLanguage FROM user_settings ORDER BY userId")
        
        assertTrue(cursor.moveToFirst())
        assertEquals(1, cursor.getInt(0))
        assertEquals("GRID", cursor.getString(1))
        assertEquals("en", cursor.getString(2))
        
        assertTrue(cursor.moveToNext())
        assertEquals(2, cursor.getInt(0))
        assertEquals("LIST", cursor.getString(1))
        assertEquals("en", cursor.getString(2))
        
        assertTrue(cursor.moveToNext())
        assertEquals(3, cursor.getInt(0))
        assertEquals("COMPACT", cursor.getString(1))
        assertEquals("en", cursor.getString(2))
        
        cursor.close()
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate6To7_IdempotentLanguageInsertion() {
        // Test that running migration 6→7 multiple times doesn't cause issues
        helper.createDatabase(TEST_DB_NAME, 6).apply {
            // Pre-populate with one language
            execSQL(
                """
                INSERT INTO supported_languages (code, displayName, nativeName, isEnabled, sortOrder)
                VALUES ('tr', 'Turkish', 'Türkçe', 1, 0)
                """.trimIndent()
            )
            close()
        }

        // Run migration 6→7
        val db = helper.runMigrationsAndValidate(TEST_DB_NAME, 7, true, MIGRATION_6_7)

        // Verify languages exist without duplicates
        val trCount = db.query("SELECT COUNT(*) FROM supported_languages WHERE code = 'tr'").use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
        assertEquals("Turkish should exist exactly once", 1, trCount)

        // Verify all three languages exist
        val totalCount = db.query("SELECT COUNT(*) FROM supported_languages").use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
        assertEquals("Should have exactly 3 languages", 3, totalCount)

        db.close()
    }
}


