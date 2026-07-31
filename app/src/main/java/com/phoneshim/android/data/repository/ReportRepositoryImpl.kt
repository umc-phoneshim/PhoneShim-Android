package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.DailyFeedbackRequest
import com.phoneshim.android.data.api.ReportApi
import com.phoneshim.android.data.api.runCatchingApi
import com.phoneshim.android.data.api.unwrap
import com.phoneshim.android.domain.model.DailyReport
import com.phoneshim.android.domain.model.ReportAppUsage
import com.phoneshim.android.domain.model.ReasonKeyword
import com.phoneshim.android.domain.model.ReportRange
import com.phoneshim.android.domain.model.ReportSummary
import com.phoneshim.android.domain.model.RestSuggestion
import com.phoneshim.android.domain.repository.ReportRepository
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val reportApi: ReportApi,
) : ReportRepository {

    override suspend fun getDailyReport(date: String, isToday: Boolean): Result<DailyReport> =
        runCatchingApi {
            val usages = if (isToday) {
                // 오늘은 앱 이름/목표까지 함께 내려오는 status 를 사용합니다.
                reportApi.getUsageStatus().unwrap().map { response ->
                    ReportAppUsage(
                        monitoredAppId = response.monitoredAppId,
                        appName = response.appName,
                        packageName = response.packageName,
                        usedMinutes = response.usedMinutes,
                        entryCount = response.entryCount,
                        targetMinutes = response.targetMinutes,
                        targetCount = response.targetCount,
                    )
                }
            } else {
                // 과거 날짜는 앱 이름이 내려오지 않습니다.
                // TODO: GET /api/monitored-apps 결과와 조인해 appName 을 채우세요.
                //  해당 API는 다른 도메인 담당이라 여기서는 monitoredAppId 만 보관합니다.
                reportApi.getUsageLogs(date).unwrap().map { response ->
                    ReportAppUsage(
                        monitoredAppId = response.monitoredAppId,
                        usedMinutes = response.usedMinutes,
                        entryCount = response.entryCount,
                    )
                }
            }
            DailyReport(date = date, appUsages = usages)
        }

    override suspend fun getReportSummary(
        range: ReportRange,
        date: String?,
    ): Result<ReportSummary> = runCatchingApi {
        val response = reportApi.getReportSummary(range = range.value, date = date).unwrap()
        ReportSummary(
            range = ReportRange.entries.firstOrNull { it.value == response.range } ?: range,
            from = response.from,
            to = response.to,
            keywords = response.keywords.map { ReasonKeyword(text = it.text, count = it.count) },
            summary = response.summary,
        )
    }

    override suspend fun getRestSuggestion(date: String?): Result<RestSuggestion> = runCatchingApi {
        val response = reportApi.getDailyFeedback(DailyFeedbackRequest(date = date)).unwrap()
        RestSuggestion(date = response.date, message = response.feedback)
    }
}
