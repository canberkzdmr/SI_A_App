package com.cbo.core.database.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add attachments column to notes table
        db.execSQL("ALTER TABLE notes ADD COLUMN attachments TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add color column (nullable hex string for note color coding)
        db.execSQL("ALTER TABLE notes ADD COLUMN color TEXT")
        // Add noteType column (TEXT, CHECKLIST, AUDIO) defaulting to TEXT
        db.execSQL("ALTER TABLE notes ADD COLUMN noteType TEXT NOT NULL DEFAULT 'TEXT'")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Drop noteType and add todos (as JSON text)
        db.execSQL("ALTER TABLE notes ADD COLUMN todos TEXT NOT NULL DEFAULT '[]'")
        // SQLite doesn't support DROP COLUMN cleanly before newer versions, but we can leave noteType alone
        // or just ignore it in Room 2.6.1 since it drops it from the entity. 
        // Room will ignore the extra column if we just don't map it.
    }
}

val ALL_MIGRATIONS = arrayOf(
    MIGRATION_1_2,
    MIGRATION_2_3,
    MIGRATION_3_4
)