package com.phoneshim.android.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phoneshim.android.data.database.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY scheduledAt ASC")
    fun getReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity)
}
