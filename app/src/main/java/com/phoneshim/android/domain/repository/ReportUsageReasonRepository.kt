package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.ReasonCalendarDay
import com.phoneshim.android.domain.model.UsageReasonEntry

/**
 * 리포트 도메인의 사용 사유 입력/조회.
 *
 * 앱 차단 오버레이 쪽 [UsageReasonRepository] 와 분리돼 있습니다.
 * 오버레이는 packageName 만 알고 있는데 이 API는 monitoredAppId 를 요구해서,
 * 두 경로를 하나로 합치려면 packageName → monitoredAppId 해석이 먼저 필요합니다.
 * 해당 조회(GET /api/monitored-apps)는 MonitoredApp 도메인 담당입니다.
 */
interface ReportUsageReasonRepository {

    suspend fun submitUsageReason(entry: UsageReasonEntry): Result<Unit>

    /** @param month YYYY-MM */
    suspend fun getReasonCalendar(month: String): Result<List<ReasonCalendarDay>>
}
