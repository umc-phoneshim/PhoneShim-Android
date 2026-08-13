package com.phoneshim.android.data.api

import com.phoneshim.android.data.api.common.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * MonitoredApp(주의 앱) 도메인 API. 명세서 2_System_MonitoredApp 기준.
 *
 * 공통 정책
 * - 인증 필요
 * - 사용자별 최대 5개까지 등록 가능
 * - 같은 사용자 안에서 packageName 중복 등록 불가
 * - 목록 정렬: sortOrder asc, createdAt asc
 * - 본인 소유가 아닌 앱은 조회/수정/삭제 불가 (모두 404 MONITORED_APP_NOT_FOUND)
 */
interface MonitoredAppApi {

    /** 상태: 구현완료. 5개 초과 시 400 MONITORED_APP_LIMIT_EXCEEDED, 중복 시 409. */
    @POST("api/monitored-apps")
    suspend fun createMonitoredApp(
        @Body request: MonitoredAppCreateRequest,
    ): ApiResponse<MonitoredAppResponse>

    /** 상태: 구현완료. 로그인 사용자의 주의 앱 전체 목록(pagination 없음). */
    @GET("api/monitored-apps")
    suspend fun getMonitoredApps(): ApiResponse<List<MonitoredAppResponse>>

    /** 상태: 구현완료. */
    @GET("api/monitored-apps/{id}")
    suspend fun getMonitoredApp(
        @Path("id") id: String,
    ): ApiResponse<MonitoredAppResponse>

    /** 상태: 구현완료. 넘긴 필드만 수정됩니다. */
    @PATCH("api/monitored-apps/{id}")
    suspend fun updateMonitoredApp(
        @Path("id") id: String,
        @Body request: MonitoredAppUpdateRequest,
    ): ApiResponse<MonitoredAppResponse>

    /**
     * 상태: 구현완료. 성공 시 204 No Content라 envelope 없이 본문이 비어 옵니다.
     * 연결된 app_goals·reminder_restricted_apps는 서버 cascade 정책을 따릅니다.
     *
     * 공통 ApiCallExecutor.executeNoContent 로 감싸야 실패 응답이 ApiException 으로
     * 변환되고 서버 오류 코드가 보존됩니다. 그래서 Response<Unit> 으로 받습니다.
     */
    @DELETE("api/monitored-apps/{id}")
    suspend fun deleteMonitoredApp(
        @Path("id") id: String,
    ): Response<Unit>
}

data class MonitoredAppCreateRequest(
    val packageName: String,
    val appName: String,
    val appIcon: String? = null,
    // 없으면 서버가 마지막 순서로 저장합니다.
    val sortOrder: Int? = null,
)

// 모든 필드가 선택입니다. null인 필드는 요청에서 빠지도록 Gson 기본 동작(null 생략)에 맡깁니다.
data class MonitoredAppUpdateRequest(
    val packageName: String? = null,
    val appName: String? = null,
    val appIcon: String? = null,
    val sortOrder: Int? = null,
)

/**
 * Gson 은 Kotlin 기본값을 적용하지 않아 응답에 없는 필드가 null 로 들어옵니다.
 * 그래서 전부 nullable 로 받고 도메인 변환에서 보정합니다.
 */
data class MonitoredAppResponse(
    val id: String? = null,
    val userId: String? = null,
    val packageName: String? = null,
    val appName: String? = null,
    val appIcon: String? = null,
    val sortOrder: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)
