package com.phoneshim.android.data.database

import android.content.Context
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhoneShimMigrationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val databaseName = "migration-4-5.db"

    @After
    fun cleanUp() {
        context.deleteDatabase(databaseName)
    }

    @Test
    fun migration4To5_replacesOnlyReminderSkeletonAndPreservesOtherTables() {
        val helper = createVersionFourDatabase()
        val sqlite = helper.writableDatabase
        MIGRATION_4_5.migrate(sqlite)

        assertTrue(sqlite.query("SELECT name FROM sqlite_master WHERE type='table' AND name='reminder_restricted_apps'").use { it.moveToFirst() })
        assertTrue(sqlite.query("SELECT name FROM sqlite_master WHERE type='table' AND name='reminder_sync_state'").use { it.moveToFirst() })
        assertEquals(1, sqlite.query("SELECT COUNT(*) FROM sentinel_table").use { cursor -> cursor.moveToFirst(); cursor.getInt(0) })
        helper.close()
    }

    private fun createVersionFourDatabase(): androidx.sqlite.db.SupportSQLiteOpenHelper {
        val configuration = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(databaseName)
            .callback(
                object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(4) {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE reminders (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, scheduledAt INTEGER NOT NULL)")
                        db.execSQL("CREATE TABLE sentinel_table (id INTEGER NOT NULL PRIMARY KEY)")
                        db.execSQL("INSERT INTO sentinel_table(id) VALUES (1)")
                    }

                    override fun onUpgrade(
                        db: androidx.sqlite.db.SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int,
                    ) = Unit
                },
            )
            .build()
        return FrameworkSQLiteOpenHelperFactory().create(configuration).also { it.writableDatabase }
    }
}
