// TODO(3단계 교차연동): data/api/ReportApi.kt(이안 소유)에 getUsageLogs/getUsageStatus가
//  이미 중복 구현돼 있음(리포트 타임테이블용, 주석에 "폴 담당"이라고 표시돼 있음).
//  분배안 3단계 "이안: 폴의 UsageLog를 사용한 리포트 연결"에서 이안과 상의해
//  ReportRepositoryImpl이 이 UsageLogApi/UsageLogRepository를 쓰도록 정리하고
//  ReportApi.kt의 중복 메서드+DTO를 제거할 것. 지금은 이안 파일을 건드리지 않음.
//  NOTE: ReportApi.kt가 같은 패키지(data.api)에 UsageLogResponse/UsageStatusResponse를
//  이미 선언해놔서 이름이 그대로 겹칩니다(컴파일 에러). 정리 전까지 임시로 접미어를 붙여
//  UsageLogEntryResponse/UsageAppStatusResponse로 구분했습니다. 3단계 정리 때 하나로 합치고
//  이 이름도 다시 정리해주세요.
package com.phoneshim.android.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface UsageLogApi {

    @GET("api/usage-logs")
    suspend fun getUsageLogs(
        @Query("date") date: String? = null,
    ): ApiEnvelope<List<UsageLogEntryResponse>>

    @GET("api/usage-logs/status")
    suspend fun getUsageStatus(): ApiEnvelope<List<UsageAppStatusResponse>>

    @PUT("api/usage-logs")
    suspend fun putUsageLog(
        @Body request: UsageLogUpsertRequest,
    ): ApiEnvelope<UsageLogEntryResponse>
}

data class UsageLogEntryResponse(
    val id: String,
    val userId: String? = null,
    val monitoredAppId: String,
    val date: String,
    val usedMinutes: Int,
    val entryCount: Int,
)

data class UsageAppStatusResponse(
    val monitoredAppId: String,
    val appName: String,
    val packageName: String,
    val appIcon: String? = null,
    val sortOrder: Int = 0,
    val targetMinutes: Int? = null,
    val targetCount: Int? = null,
    val usedMinutes: Int,
    val entryCount: Int,
)

/** date 생략 시 KST 오늘 기준. */
data class UsageLogUpsertRequest(
    val monitoredAppId: String,
    val date: String? = null,
    val usedMinutes: Int,
    val entryCount: Int,
)
