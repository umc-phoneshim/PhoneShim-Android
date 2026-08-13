package com.phoneshim.android.data.database.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.phoneshim.android.data.database.entity.ReminderEntity
import com.phoneshim.android.data.database.entity.ReminderRestrictedAppEntity
import com.phoneshim.android.data.database.entity.ReminderSyncStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Transaction
    @Query(
        """
        SELECT * FROM reminders
        WHERE dateEpochDay = :dateEpochDay
        ORDER BY startTimeEpochMillis ASC, endTimeEpochMillis ASC, createdAtEpochMillis ASC
        """,
    )
    suspend fun getForDate(dateEpochDay: Long): List<ReminderWithRestrictedApps>

    /** [getForDate]와 같은 쿼리를 Flow로. Room이 reminders 테이블 변경을 감지해 자동 재emit한다. */
    @Transaction
    @Query(
        """
        SELECT * FROM reminders
        WHERE dateEpochDay = :dateEpochDay
        ORDER BY startTimeEpochMillis ASC, endTimeEpochMillis ASC, createdAtEpochMillis ASC
        """,
    )
    fun observeForDate(dateEpochDay: Long): Flow<List<ReminderWithRestrictedApps>>

    @Transaction
    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: String): ReminderWithRestrictedApps?

    @Query("SELECT * FROM reminder_sync_state WHERE dateEpochDay = :dateEpochDay")
    suspend fun getSyncState(dateEpochDay: Long): ReminderSyncStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReminders(reminders: List<ReminderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRestrictedApps(apps: List<ReminderRestrictedAppEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSyncState(state: ReminderSyncStateEntity)

    @Query("DELETE FROM reminders WHERE dateEpochDay = :dateEpochDay")
    suspend fun deleteForDate(dateEpochDay: Long)

    @Query("DELETE FROM reminder_restricted_apps WHERE reminderId = :reminderId")
    suspend fun deleteRestrictedApps(reminderId: String)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: String)

    @Transaction
    suspend fun replaceDate(
        dateEpochDay: Long,
        entries: List<ReminderCacheEntry>,
        syncedAtEpochMillis: Long,
    ) {
        deleteForDate(dateEpochDay)
        if (entries.isNotEmpty()) {
            upsertReminders(entries.map(ReminderCacheEntry::reminder))
            upsertRestrictedApps(entries.flatMap(ReminderCacheEntry::restrictedApps))
        }
        upsertSyncState(ReminderSyncStateEntity(dateEpochDay, syncedAtEpochMillis))
    }

    @Transaction
    suspend fun upsert(entry: ReminderCacheEntry) {
        upsertReminders(listOf(entry.reminder))
        deleteRestrictedApps(entry.reminder.id)
        if (entry.restrictedApps.isNotEmpty()) upsertRestrictedApps(entry.restrictedApps)
    }
}

data class ReminderWithRestrictedApps(
    @Embedded val reminder: ReminderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "reminderId",
    )
    val restrictedApps: List<ReminderRestrictedAppEntity>,
)

data class ReminderCacheEntry(
    val reminder: ReminderEntity,
    val restrictedApps: List<ReminderRestrictedAppEntity>,
)
