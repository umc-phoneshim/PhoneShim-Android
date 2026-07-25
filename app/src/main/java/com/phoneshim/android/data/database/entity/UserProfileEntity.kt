package com.phoneshim.android.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 온보딩에서 입력한 사용자 프로필(성별·나이대) 저장소.
 *
 * [원본] 서버 계약이 아직 없어 현재는 로컬이 유일한 저장처다.
 *   프로필 API가 확정되면 다른 캐시들처럼 서버 미러 역할로 전환한다.
 * [역할] 온보딩 재진입·설정 화면에서 이전 선택값을 복원한다.
 *   차단 엔진은 이 값을 읽지 않으므로 목표 캐시와 테이블을 분리했다.
 *
 * 값은 UI가 넘긴 문자열을 그대로 둔다(도메인 Goal.gender/ageGroup과 동일).
 * 서버 계약 확정 시 enum 정규화를 함께 논의한다.
 */
@Entity(tableName = "user_profile_cache")
data class UserProfileEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val gender: String?,
    val ageGroup: String?,
) {
    companion object {
        const val SINGLE_ROW_ID = 0
    }
}
