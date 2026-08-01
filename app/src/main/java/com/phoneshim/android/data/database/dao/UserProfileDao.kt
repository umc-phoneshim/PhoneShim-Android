package com.phoneshim.android.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phoneshim.android.data.database.entity.UserProfileEntity

/**
 * 온보딩 사용자 프로필 저장소.
 * 목표 캐시와 달리 차단 엔진이 읽지 않고, 온보딩·설정 화면 복원에만 쓰인다.
 */
@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile_cache WHERE id = 0")
    suspend fun getProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)

    @Query("DELETE FROM user_profile_cache")
    suspend fun clearProfile()
}
