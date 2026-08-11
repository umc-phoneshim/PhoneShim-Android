package com.phoneshim.android.data.api

import com.phoneshim.android.data.api.common.ApiResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * AppGoal(앱별 목표) 도메인 API. 명세서 5_TotalGoal_AppGoal 기준.
 *
 * 공통 정책
 * - 인증 필요
 * - monitoredAppId는 로그인 사용자의 주의 앱이어야 함
 * - 주의 앱 1개당 앱 목표는 1개
 * - targetMinutes는 10~1430분, targetCount는 1 이상
 * - goalReason은 공백 포함 최대 100자
 */
interface AppGoalApi {

    /** 상태: 구현완료. 이미 있으면 409 APP_GOAL_ALREADY_EXISTS. */
    @POST("api/app-goals")
    suspend fun createAppGoal(
        @Body request: AppGoalCreateRequest,
    ): ApiResponse<AppGoalResponse>

    /**
     * 상태: 구현완료. 목록이 아니라 해당 주의 앱의 목표 '단건'을 돌려줍니다
     * (주의 앱 1개당 목표 1개라서). 목표가 없으면 404 APP_GOAL_NOT_FOUND.
     */
    @GET("api/app-goals")
    suspend fun getAppGoal(
        @Query("monitoredAppId") monitoredAppId: String,
    ): ApiResponse<AppGoalResponse>

    /** 상태: 구현완료. 넘긴 필드만 수정됩니다. */
    @PATCH("api/app-goals/{id}")
    suspend fun updateAppGoal(
        @Path("id") id: String,
        @Body request: AppGoalUpdateRequest,
    ): ApiResponse<AppGoalResponse>

    /** 상태: 구현완료. 성공 시 204 No Content라 본문이 비어 옵니다. */
    @DELETE("api/app-goals/{id}")
    suspend fun deleteAppGoal(
        @Path("id") id: String,
    )
}

data class AppGoalCreateRequest(
    val monitoredAppId: String,
    val targetMinutes: Int,
    val targetCount: Int,
    val restrictAfter: Boolean? = null,
    val goalReason: String? = null,
)

data class AppGoalUpdateRequest(
    val targetMinutes: Int? = null,
    val targetCount: Int? = null,
    val restrictAfter: Boolean? = null,
    val goalReason: String? = null,
)

/**
 * Gson 은 Kotlin 기본값을 적용하지 않아 응답에 없는 필드가 null 로 들어옵니다.
 * 그래서 전부 nullable 로 받고 도메인 변환에서 보정합니다.
 */
data class AppGoalResponse(
    val id: String? = null,
    val monitoredAppId: String? = null,
    val targetMinutes: Int? = null,
    val targetCount: Int? = null,
    val restrictAfter: Boolean? = null,
    val goalReason: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)
