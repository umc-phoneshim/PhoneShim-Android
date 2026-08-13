package com.phoneshim.android.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 전체 폰 목표 서버 데이터의 로컬 캐시.
 *
 * [원본] 서버 TotalGoal API (/api/total-goals). 저장·수정 원본은 서버.
 * [역할] 오프라인 엔진 판정용 미러.
 *   goalMinutes = 서버 targetMinutes, limitEnabled = 서버 restrictAfter.
 */
@Entity(tableName = "phone_goal_cache")
data class PhoneGoalEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val goalMinutes: Int,
    val limitEnabled: Boolean,
    // 서버 TotalGoal 식별자. 있으면 PATCH, 없으면 POST 로 보낸다. 동기화 전이면 null.
    val serverGoalId: String? = null,
) {
    companion object {
        const val SINGLE_ROW_ID = 0
    }
}
