package com.phoneshim.android.data.api

import com.phoneshim.android.data.api.common.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * UsageReason 도메인 API. 백엔드 src/domains/usageReason 구현을 직접 확인해 맞췄습니다.
 *
 * 사유 입력 여부를 월 단위로 조회하는 엔드포인트는 서버에 없습니다.
 * (/api/usage-logs/calendar 는 목표 달성일이라 용도가 다릅니다)
 */
interface UsageReasonApi {

    /**
     * usageReasonRouter: POST / — 구현완료. 응답은 201.
     * 고른 사유 코드마다 행이 하나씩 생성돼 배열로 돌아옵니다.
     * 차단 팝업 입력은 시간 제한 없이 같은 API로 저장합니다.
     */
    @POST("api/usage-reasons")
    suspend fun submitUsageReason(
        @Body request: UsageReasonRequest,
    ): ApiResponse<List<UsageReasonResponse>>
}

/** reasonCodes 는 LEISURE / COMMUTE / HABIT / INFO / OTHER 중 하나 이상. */
data class UsageReasonRequest(
    val monitoredAppId: String,
    val date: String,
    val timeRangeStart: String,
    val timeRangeEnd: String,
    val reasonCodes: List<String>,
    val usageLogId: String? = null,
)

data class UsageReasonResponse(
    val id: String? = null,
    val monitoredAppId: String? = null,
    val date: String? = null,
    val timeRangeStart: String? = null,
    val timeRangeEnd: String? = null,
    val reason: String? = null,
)
