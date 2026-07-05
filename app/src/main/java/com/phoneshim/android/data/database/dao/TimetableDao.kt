package com.phoneshim.android.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phoneshim.android.data.database.entity.TimetableEntity

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable_entries WHERE startedAt BETWEEN :startOfDay AND :endOfDay")
    suspend fun getEntriesForDay(startOfDay: Long, endOfDay: Long): List<TimetableEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: TimetableEntity)
}
