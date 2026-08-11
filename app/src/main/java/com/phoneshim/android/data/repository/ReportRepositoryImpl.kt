package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.ReportApi
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.domain.model.DailyReport
import com.phoneshim.android.domain.model.ReasonAppUsage
import com.phoneshim.android.domain.model.ReasonSummary
import com.phoneshim.android.domain.model.ReportAppUsage
import com.phoneshim.android.domain.model.ReportRange
import com.phoneshim.android.domain.model.ReportSummary
import com.phoneshim.android.domain.model.UsageReasonCode
import com.phoneshim.android.domain.model.UsageSession
import com.phoneshim.android.domain.repository.ReportRepository
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val reportApi: ReportApi,
    private val apiCallExecutor: ApiCallExecutor,
) : ReportRepository {

    override suspend fun getDailyReport(date: String, isToday: Boolean): Result<DailyReport> =
        if (isToday) {
            // 오늘은 앱 이름/패키지명/목표까지 함께 내려오는 status 를 사용합니다.
            apiCallExecutor.executeAsResult { reportApi.getUsageStatus() }.map { responses ->
                val usages = responses.map { response ->
                    ReportAppUsage(
                        monitoredAppId = response.monitoredAppId.orEmpty(),
                        appName = response.appName.orEmpty(),
                        packageName = response.packageName.orEmpty(),
                        usedMinutes = response.usedMinutes ?: 0,
                        entryCount = response.entryCount ?: 0,
                        targetMinutes = response.targetMinutes,
                        targetCount = response.targetCount,
                    )
                }
                DailyReport(date = date, appUsages = usages)
            }
        } else {
            apiCallExecutor.executeAsResult { reportApi.getUsageLogs(date) }.map { responses ->
                // 과거 날짜는 앱 이름/패키지명이 내려오지 않습니다.
                // TODO: GET /api/monitored-apps 와 조인하면 과거 날짜에도 아이콘을 띄울 수 있습니다.
                //  해당 API는 MonitoredApp 도메인 담당입니다.
                val usages = responses.map { response ->
                    ReportAppUsage(
                        monitoredAppId = response.monitoredAppId.orEmpty(),
                        usedMinutes = response.usedMinutes ?: 0,
                        entryCount = response.entryCount ?: 0,
                    )
                }
                DailyReport(date = date, appUsages = usages)
            }
        }

    override suspend fun getUsageSessions(date: String): Result<List<UsageSession>> =
        apiCallExecutor.executeAsResult { reportApi.getUsageSessions(date) }.map { responses ->
            responses.mapNotNull { response ->
                val start = response.startTime?.toLocalDateTimeOrNull() ?: return@mapNotNull null
                val end = response.endTime?.toLocalDateTimeOrNull() ?: return@mapNotNull null
                UsageSession(
                    id = response.id.orEmpty(),
                    monitoredAppId = response.monitoredAppId.orEmpty(),
                    date = response.date.orEmpty(),
                    startTime = start,
                    endTime = end,
                )
            }
        }

    override suspend fun getReportSummary(
        range: ReportRange,
        date: String?,
    ): Result<ReportSummary> = apiCallExecutor.executeAsResult {
        reportApi.getReportSummary(range = range.value, date = date)
    }.map { response ->
        ReportSummary(
            range = ReportRange.from(response.range) ?: range,
            from = response.from.orEmpty(),
            to = response.to.orEmpty(),
            reasons = response.reasons.orEmpty().mapNotNull { reason ->
                val code = UsageReasonCode.from(reason.reason) ?: return@mapNotNull null
                ReasonSummary(
                    reason = code,
                    totalMinutes = reason.totalMinutes ?: 0,
                    apps = reason.apps.orEmpty().map { app ->
                        ReasonAppUsage(
                            monitoredAppId = app.monitoredAppId.orEmpty(),
                            appName = app.appName.orEmpty(),
                            minutes = app.minutes ?: 0,
                        )
                    },
                )
            },
        )
    }

    override suspend fun getAchievedDates(month: String): Result<List<String>> =
        apiCallExecutor.executeAsResult { reportApi.getUsageCalendar(month) }
            .map { it.achievedDates.orEmpty() }
}

/**
 * 서버는 시간 값을 ISO 8601(UTC)로 내려줍니다. 화면은 기기 시간대 기준으로 그려야 해서
 * 파싱 후 로컬 시간대로 변환합니다.
 */
private fun String.toLocalDateTimeOrNull(): LocalDateTime? = runCatching {
    OffsetDateTime.parse(this).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
}.recoverCatching {
    LocalDateTime.parse(this)
}.getOrNull()
