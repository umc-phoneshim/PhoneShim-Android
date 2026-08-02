package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.DailyReport
import com.phoneshim.android.domain.model.ReportRange
import com.phoneshim.android.domain.model.ReportSummary
import com.phoneshim.android.domain.model.RestSuggestion

interface ReportRepository {

    /**
     * 선택 날짜의 앱별 사용량.
     * 오늘이면 앱 이름까지 포함된 /api/usage-logs/status 를, 과거 날짜면 /api/usage-logs 를 사용합니다.
     */
    suspend fun getDailyReport(date: String, isToday: Boolean): Result<DailyReport>

    /** 주간/월간 요약. 데이터 부족은 공통 API 오류 코드로 구분됩니다. */
    suspend fun getReportSummary(range: ReportRange, date: String?): Result<ReportSummary>

    /** 쉼이의 제안. 백엔드 분석 결과 문구를 그대로 받습니다. */
    suspend fun getRestSuggestion(date: String?): Result<RestSuggestion>
}
