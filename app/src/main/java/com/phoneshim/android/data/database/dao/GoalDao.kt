package com.phoneshim.android.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phoneshim.android.data.database.entity.AppGoalEntity
import com.phoneshim.android.data.database.entity.PhoneGoalEntity
import kotlinx.coroutines.flow.Flow

/**
 * 차단 목표 저장소.
 * 온보딩 완료 시 upsert*, 설정 수정 시 upsert/삭제하면 차단 엔진이 다음 tick 에 새 값을 읽는다.
 */
@Dao
interface GoalDao {

    // --- 전체 폰 차단 목표 ---
    @Query("SELECT * FROM phone_goal_cache WHERE id = 0")
    fun observePhoneGoal(): Flow<PhoneGoalEntity?>

    @Query("SELECT * FROM phone_goal_cache WHERE id = 0")
    suspend fun getPhoneGoal(): PhoneGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPhoneGoal(goal: PhoneGoalEntity)

    // --- 앱 별 차단 목표 ---
    @Query("SELECT * FROM app_goal_cache")
    fun observeAppGoals(): Flow<List<AppGoalEntity>>

    @Query("SELECT * FROM app_goal_cache")
    suspend fun getAppGoals(): List<AppGoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAppGoals(goals: List<AppGoalEntity>)

    @Query("DELETE FROM app_goal_cache WHERE packageName = :packageName")
    suspend fun deleteAppGoal(packageName: String)

    @Query("DELETE FROM app_goal_cache")
    suspend fun clearAppGoals()
}
