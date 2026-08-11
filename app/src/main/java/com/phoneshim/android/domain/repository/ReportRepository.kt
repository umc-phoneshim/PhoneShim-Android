package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.DailyReport
import com.phoneshim.android.domain.model.ReportRange
import com.phoneshim.android.domain.model.ReportSummary
import com.phoneshim.android.domain.model.UsageSession

interface ReportRepository {

    /**
     * 선택 날짜의 앱별 사용량.
     * 오늘이면 앱 이름까지 포함된 /api/usage-logs/status 를, 과거 날짜면 /api/usage-logs 를 사용합니다.
     */
    suspend fun getDailyReport(date: String, isToday: Boolean): Result<DailyReport>

    /** 타임테이블용 사용 구간 목록. */
    suspend fun getUsageSessions(date: String): Result<List<UsageSession>>

    /** 기간별 사용 사유 요약. day/week/month. */
    suspend fun getReportSummary(range: ReportRange, date: String?): Result<ReportSummary>

    /** 그 달에 목표를 달성한 날짜 목록. @param month YYYY-MM */
    suspend fun getAchievedDates(month: String): Result<List<String>>
}
