package com.phoneshim.android.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * 구 목표 API. 서버 명세에 없는 임시 계약이라 아래 3종으로 교체 예정입니다.
 *
 * - 주의 앱 CRUD    → [MonitoredAppApi]
 * - 전체 폰 목표     → [TotalGoalApi]
 * - 앱별 목표        → [AppGoalApi]
 *
 * 교체 지점: [com.phoneshim.android.data.repository.GoalRepositoryImpl] 하나만 이 API를 씁니다.
 * repository를 새 3종으로 옮기면서 packageName ↔ monitoredAppId 매핑을 붙이면 이 파일은 삭제합니다.
 */
interface GoalApi {
    @GET("goals/me")
    suspend fun getGoal(): GoalResponse?

    @POST("goals")
    suspend fun saveGoal(@Body request: GoalResponse)
}

data class GoalResponse(
    val id: String,
    val targetPackageNames: List<String>,
    val dailyUsageLimitMinutes: Int,
    val accessCountLimit: Int,
    val description: String,
)
