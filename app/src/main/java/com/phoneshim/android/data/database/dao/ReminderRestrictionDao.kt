package com.phoneshim.android.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phoneshim.android.data.database.entity.ReminderRestrictionEntity
import kotlinx.coroutines.flow.Flow

/**
 * 리마인더 일정 제한 저장소.
 * 리마인더 saveTask() 자리에서 upsert 하면 엔진이 읽는다.
 * 일정 삭제 시 delete 도 같이 호출 필요.
 */
@Dao
interface ReminderRestrictionDao {

    /** 특정 날짜의 제한 일정.**/
    @Query("SELECT * FROM reminder_restriction_cache WHERE date = :epochDay")
    suspend fun getForDate(epochDay: Long): List<ReminderRestrictionEntity>

    @Query("SELECT * FROM reminder_restriction_cache WHERE date = :epochDay")
    fun observeForDate(epochDay: Long): Flow<List<ReminderRestrictionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(restriction: ReminderRestrictionEntity)

    @Query("DELETE FROM reminder_restriction_cache WHERE taskId = :taskId")
    suspend fun delete(taskId: String)
}
