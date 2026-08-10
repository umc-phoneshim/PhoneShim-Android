package com.phoneshim.android.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 기존 reminders는 id/title/scheduledAt뿐이라 API Reminder로 복원할 수 없다.
 * 해당 초기 골격 테이블만 교체하고 다른 도메인과 차단 엔진 캐시는 보존한다.
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS `reminders`")
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `reminders` (
                `id` TEXT NOT NULL,
                `userId` TEXT NOT NULL,
                `dateEpochDay` INTEGER NOT NULL,
                `title` TEXT NOT NULL,
                `startTimeEpochMillis` INTEGER NOT NULL,
                `endTimeEpochMillis` INTEGER NOT NULL,
                `restrictionMode` TEXT NOT NULL,
                `createdAtEpochMillis` INTEGER NOT NULL,
                `updatedAtEpochMillis` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_reminders_dateEpochDay` ON `reminders` (`dateEpochDay`)",
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `reminder_restricted_apps` (
                `reminderId` TEXT NOT NULL,
                `monitoredAppId` TEXT NOT NULL,
                PRIMARY KEY(`reminderId`, `monitoredAppId`),
                FOREIGN KEY(`reminderId`) REFERENCES `reminders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_reminder_restricted_apps_reminderId` ON `reminder_restricted_apps` (`reminderId`)",
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `reminder_sync_state` (
                `dateEpochDay` INTEGER NOT NULL,
                `syncedAtEpochMillis` INTEGER NOT NULL,
                PRIMARY KEY(`dateEpochDay`)
            )
            """.trimIndent(),
        )
    }
}
