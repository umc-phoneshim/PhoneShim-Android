package com.phoneshim.android.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.phoneshim.android.data.database.dao.ReminderCacheEntry
import com.phoneshim.android.data.database.entity.ReminderEntity
import com.phoneshim.android.data.database.entity.ReminderRestrictedAppEntity
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderDaoTest {
    private lateinit var database: PhoneShimDatabase

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PhoneShimDatabase::class.java,
        ).build()
    }

    @After
    @Throws(IOException::class)
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun replaceDate_replacesOnlyRequestedDateAndStoresEmptySyncMarker() = runTest {
        val dao = database.reminderDao()
        dao.replaceDate(DATE_ONE, listOf(entry("one", DATE_ONE, 1_000)), 10L)
        dao.replaceDate(DATE_TWO, listOf(entry("two", DATE_TWO, 2_000)), 20L)

        dao.replaceDate(DATE_ONE, emptyList(), 30L)

        assertTrue(dao.getForDate(DATE_ONE).isEmpty())
        assertEquals("two", dao.getForDate(DATE_TWO).single().reminder.id)
        assertNotNull(dao.getSyncState(DATE_ONE))
    }

    @Test
    fun upsert_replacesRestrictedAppsAndDeleteCascades() = runTest {
        val dao = database.reminderDao()
        dao.upsert(entry("one", DATE_ONE, 1_000, setOf("app-1", "app-2")))
        assertEquals(2, dao.getById("one")?.restrictedApps?.size)

        dao.upsert(entry("one", DATE_ONE, 1_000, setOf("app-3")))
        assertEquals(setOf("app-3"), dao.getById("one")?.restrictedApps?.map { it.monitoredAppId }?.toSet())

        dao.deleteById("one")
        assertNull(dao.getById("one"))
    }

    private fun entry(
        id: String,
        date: Long,
        start: Long,
        apps: Set<String> = emptySet(),
    ) = ReminderCacheEntry(
        reminder = ReminderEntity(
            id = id,
            userId = "user",
            dateEpochDay = date,
            title = id,
            startTimeEpochMillis = start,
            endTimeEpochMillis = start + 1_000,
            restrictionMode = "NONE",
            createdAtEpochMillis = start,
            updatedAtEpochMillis = start,
        ),
        restrictedApps = apps.map { ReminderRestrictedAppEntity(id, it) },
    )

    private companion object {
        const val DATE_ONE = 20_000L
        const val DATE_TWO = 20_001L
    }
}

private fun assertTrue(value: Boolean) = org.junit.Assert.assertTrue(value)
