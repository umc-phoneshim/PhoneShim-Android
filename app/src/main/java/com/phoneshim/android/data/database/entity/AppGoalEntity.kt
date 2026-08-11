package com.phoneshim.android.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 주의앱 차단 목표 서버 데이터의 로컬 캐시.
 *
 * [원본] 서버 AppGoal API (/api/app-goals). 저장·수정의 원본은 서버다.
 * [이 테이블의 역할] 오프라인에서도 엔진이 목표를 읽을 수 있게, 서버 응답을 로컬에 미러링한다.
 *
 * [필드가 서버와 다른 이유]
 *   서버는 앱을 monitoredAppId로 식별하지만, 엔진은 UsageEvents 에서 packageName 으로 앱을 감지한다.
 *   매 판정마다 uuid→패키지명 조회는 오프라인에서 불가하므로,
 *   캐시는 처음부터 packageName 기준으로 저장한다.
 *   goalMinutes = 서버 targetMinutes, limitEnabled = 서버 restrictAfter 에 대응.
 */
@Entity(tableName = "app_goal_cache")
data class AppGoalEntity(
    @PrimaryKey val packageName: String,
    val appLabel: String,
    val goalMinutes: Int,   // 서버 targetMinutes
    val limitEnabled: Boolean, // 서버 restrictAfter
    val targetCount: Int = 1, // 서버 targetCount (진입 횟수, 최소 1)
    // 서버 식별자. 다음 저장 때 POST 대신 PATCH 로 보내 409 중복을 피하고,
    // 선택에서 빠진 앱을 DELETE 할 때 쓴다. 서버 동기화 전이면 null.
    val monitoredAppId: String? = null,
    val appGoalId: String? = null,
)
