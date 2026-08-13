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
    fun migration4To5_mergesReminderAndGoalSchemaChanges() {
        val helper = createVersionFourDatabase()
        val sqlite = helper.writableDatabase
        MIGRATION_4_5.migrate(sqlite)

        assertTrue(tableExists(sqlite, "reminder_restricted_apps"))
        assertTrue(tableExists(sqlite, "reminder_sync_state"))
        assertEquals(1, intValue(sqlite, "SELECT targetCount FROM app_goal_cache"))
        assertTrue(
            sqlite.query("SELECT monitoredAppId, appGoalId FROM app_goal_cache").use { cursor ->
                cursor.moveToFirst()
                cursor.isNull(0) && cursor.isNull(1)
            },
        )
        assertTrue(sqlite.query("SELECT serverGoalId FROM phone_goal_cache").use { cursor -> cursor.moveToFirst(); cursor.isNull(0) })
        assertEquals(1, intValue(sqlite, "SELECT COUNT(*) FROM sentinel_table"))
        helper.close()
    }

    @Test
    fun migration5To6_addsGoalReasonKeepingExistingRows() {
        val helper = createVersionFourDatabase()
        val sqlite = helper.writableDatabase
        MIGRATION_4_5.migrate(sqlite)
        MIGRATION_5_6.migrate(sqlite)

        // 기존 행은 유지되고 새 컬럼만 NULL 로 붙는다.
        assertEquals(1, intValue(sqlite, "SELECT COUNT(*) FROM app_goal_cache"))
        assertTrue(
            sqlite.query("SELECT goalReason FROM app_goal_cache").use { cursor ->
                cursor.moveToFirst()
                cursor.isNull(0)
            },
        )
        assertEquals(30, intValue(sqlite, "SELECT goalMinutes FROM app_goal_cache"))
        helper.close()
    }

    private fun createVersionFourDatabase(): androidx.sqlite.db.SupportSQLiteOpenHelper {
        val configuration = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(databaseName)
            .callback(
                object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(4) {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE reminders (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, scheduledAt INTEGER NOT NULL)")
                        db.execSQL("CREATE TABLE app_goal_cache (packageName TEXT NOT NULL PRIMARY KEY, appLabel TEXT NOT NULL, goalMinutes INTEGER NOT NULL, limitEnabled INTEGER NOT NULL)")
                        db.execSQL("INSERT INTO app_goal_cache VALUES ('com.example.app', 'Example', 30, 1)")
                        db.execSQL("CREATE TABLE phone_goal_cache (id INTEGER NOT NULL PRIMARY KEY, goalMinutes INTEGER NOT NULL, limitEnabled INTEGER NOT NULL)")
                        db.execSQL("INSERT INTO phone_goal_cache VALUES (0, 120, 1)")
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

    private fun tableExists(
        database: androidx.sqlite.db.SupportSQLiteDatabase,
        tableName: String,
    ): Boolean = database.query(
        "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
        arrayOf(tableName),
    ).use { it.moveToFirst() }

    private fun intValue(
        database: androidx.sqlite.db.SupportSQLiteDatabase,
        query: String,
    ): Int = database.query(query).use { cursor ->
        cursor.moveToFirst()
        cursor.getInt(0)
    }
}
