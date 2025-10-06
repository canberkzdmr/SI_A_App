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


val ALL_MIGRATIONS = arrayOf(
    MIGRATION_2_3
)