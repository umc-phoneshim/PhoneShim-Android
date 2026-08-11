package com.phoneshim.android.data.api

import com.phoneshim.android.data.api.common.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

/**
 * TotalGoal(전체 폰 사용 목표) 도메인 API. 명세서 5_TotalGoal_AppGoal 기준.
 *
 * 공통 정책
 * - 인증 필요
 * - 사용자당 전체 목표는 1개 (경로에 id가 없는 이유)
 * - targetMinutes는 10~1430분만 허용
 */
interface TotalGoalApi {

    /** 상태: 예정. 이미 있으면 409 TOTAL_GOAL_ALREADY_EXISTS. */
    @POST("api/total-goals")
    suspend fun createTotalGoal(
        @Body request: TotalGoalCreateRequest,
    ): ApiResponse<TotalGoalResponse>

    /** 상태: 예정. 목표가 없으면 404 TOTAL_GOAL_NOT_FOUND. */
    @GET("api/total-goals")
    suspend fun getTotalGoal(): ApiResponse<TotalGoalResponse>

    /** 상태: 예정. 수정 가능한 필드가 하나도 없으면 400 VALIDATION_ERROR. */
    @PATCH("api/total-goals")
    suspend fun updateTotalGoal(
        @Body request: TotalGoalUpdateRequest,
    ): ApiResponse<TotalGoalResponse>
}

data class TotalGoalCreateRequest(
    val targetMinutes: Int,
    // 목표 초과 후 제한 여부. 서버 기본값 false.
    val restrictAfter: Boolean? = null,
)

data class TotalGoalUpdateRequest(
    val targetMinutes: Int? = null,
    val restrictAfter: Boolean? = null,
)

data class TotalGoalResponse(
    val id: String,
    val userId: String,
    val targetMinutes: Int,
    val restrictAfter: Boolean,
    val createdAt: String,
    val updatedAt: String,
)
