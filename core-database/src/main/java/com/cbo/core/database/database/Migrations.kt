package com.cbo.core.database.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `user_settings` (
                `userId` INTEGER NOT NULL PRIMARY KEY,
                `isFirstLoginDone` INTEGER NOT NULL DEFAULT 0,
                `isBiometricsEnabled` INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        // Insert default rows for existing users
        db.execSQL(
            """
            INSERT INTO `user_settings` (`userId`, `isFirstLoginDone`, `isBiometricsEnabled`)
            SELECT `id`, 0, 0 FROM `users`
            """.trimIndent()
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE user_settings ADD COLUMN notesViewMode TEXT NOT NULL DEFAULT 'LIST'")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create supported_languages table
        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS `supported_languages` (
                    `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    `code` TEXT NOT NULL,
                    `displayName` TEXT NOT NULL,
                    `nativeName` TEXT NOT NULL,
                    `isEnabled` INTEGER NOT NULL DEFAULT 1,
                    `sortOrder` INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent()
        )

        // Create unique index on code
        db.execSQL(
            """
                CREATE UNIQUE INDEX IF NOT EXISTS `index_supported_languages_code`
                ON `supported_languages` (`code`)
            """.trimIndent()
        )

        // Insert initial languages
        db.execSQL(
            """
                INSERT INTO `supported_languages` (`code`, `displayName`, `nativeName`, `isEnabled`, `sortOrder`)
                VALUES
                    ('tr', 'Turkish', 'Türkçe', 1, 0),
                    ('en', 'English', 'English', 1, 1),
                    ('de', 'German', 'Deutsch', 0, 2)
            """.trimIndent()
        )
    }
}

val MIGRATION_5_6 = object: Migration(5,6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // SQLite doesn't support adding foreign keys to existing tables,
        // so we need to recreate the table with the new structure

        // Step 1: Create new table with correct structure (including both foreign keys)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `user_settings_new` (
                `userId` INTEGER NOT NULL PRIMARY KEY,
                `isFirstLoginDone` INTEGER NOT NULL DEFAULT 0,
                `isBiometricsEnabled` INTEGER NOT NULL DEFAULT 0,
                `notesViewMode` TEXT NOT NULL DEFAULT 'LIST',
                `preferredLanguage` TEXT NOT NULL DEFAULT 'en',
                FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`preferredLanguage`) REFERENCES `supported_languages`(`code`) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        // Step 2: Copy data from old table to new table
        // Set preferredLanguage to 'en' for all existing rows (default)
        db.execSQL(
            """
            INSERT INTO `user_settings_new` 
            (`userId`, `isFirstLoginDone`, `isBiometricsEnabled`, `notesViewMode`, `preferredLanguage`)
            SELECT 
                `userId`, 
                `isFirstLoginDone`, 
                `isBiometricsEnabled`, 
                `notesViewMode`,
                'en' as `preferredLanguage`
            FROM `user_settings`
            """.trimIndent()
        )

        // Step 3: Drop old table
        db.execSQL("DROP TABLE `user_settings`")

        // Step 4: Rename new table to original name
        db.execSQL("ALTER TABLE `user_settings_new` RENAME TO `user_settings`")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Ensure supported languages are populated (for databases that skipped migration 4_5)
        // Check if languages exist first to avoid duplicates
        db.execSQL(
            """
            INSERT OR IGNORE INTO `supported_languages` (`code`, `displayName`, `nativeName`, `isEnabled`, `sortOrder`)
            VALUES
                ('tr', 'Turkish', 'Türkçe', 1, 0),
                ('en', 'English', 'English', 1, 1),
                ('de', 'German', 'Deutsch', 0, 2)
            """.trimIndent()
        )
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add soft delete columns to notes table
        db.execSQL("ALTER TABLE notes ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE notes ADD COLUMN deletedAt INTEGER DEFAULT NULL")
        
        // Create indices for better query performance
        db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_isDeleted ON notes(isDeleted)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_deletedAt ON notes(deletedAt)")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add reminder column to notes table
        db.execSQL("ALTER TABLE notes ADD COLUMN reminderTime INTEGER DEFAULT NULL")
        
        // Create index for reminder queries
        db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_reminderTime ON notes(reminderTime)")
    }
}

val ALL_MIGRATIONS = arrayOf(
    MIGRATION_2_3,
    MIGRATION_3_4,
    MIGRATION_4_5,
    MIGRATION_5_6,
    MIGRATION_6_7,
    MIGRATION_7_8,
    MIGRATION_8_9
)