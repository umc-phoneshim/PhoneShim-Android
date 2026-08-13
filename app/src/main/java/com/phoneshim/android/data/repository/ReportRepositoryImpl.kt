package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.ReportApi
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.domain.model.DailyReport
import com.phoneshim.android.domain.model.ReasonAppUsage
import com.phoneshim.android.domain.model.ReasonSummary
import com.phoneshim.android.domain.model.ReportAppUsage
import com.phoneshim.android.domain.model.ReportRange
import com.phoneshim.android.domain.model.ReportSummary
import com.phoneshim.android.domain.model.RestSuggestion
import com.phoneshim.android.domain.model.SuggestionType
import com.phoneshim.android.domain.model.UsageReasonCode
import com.phoneshim.android.domain.model.UsageSession
import com.phoneshim.android.domain.repository.ReportRepository
import com.phoneshim.android.domain.repository.UsageLogRepository
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val reportApi: ReportApi,
    private val apiCallExecutor: ApiCallExecutor,
    private val usageLogRepository: UsageLogRepository,
) : ReportRepository {

    override suspend fun getDailyReport(date: String, isToday: Boolean): Result<DailyReport> =
        if (isToday) {
            // 오늘은 앱 이름/패키지명/목표까지 함께 내려오는 status 를 사용합니다.
            // UsageLogApi.getUsageStatus()와 완전히 같은 엔드포인트라 폴의 UsageLogRepository를 재사용합니다.
            usageLogRepository.getUsageStatus().map { statuses ->
                val usages = statuses.map { status ->
                    ReportAppUsage(
                        monitoredAppId = status.monitoredAppId,
                        appName = status.appName,
                        packageName = status.packageName,
                        usedMinutes = status.usedMinutes,
                        entryCount = status.entryCount,
                        targetMinutes = status.targetMinutes,
                        targetCount = status.targetCount,
                    )
                }
                DailyReport(date = date, appUsages = usages)
            }
        } else {
            // UsageLogApi.getUsageLogs()와 완전히 같은 엔드포인트라 폴의 UsageLogRepository를 재사용합니다.
            usageLogRepository.getUsageLogs(date).map { logs ->
                // 과거 날짜는 앱 이름/패키지명이 내려오지 않습니다.
                // TODO: GET /api/monitored-apps 와 조인하면 과거 날짜에도 아이콘을 띄울 수 있습니다.
                //  해당 API는 MonitoredApp 도메인 담당입니다.
                val usages = logs.map { log ->
                    ReportAppUsage(
                        monitoredAppId = log.monitoredAppId,
                        usedMinutes = log.usedMinutes,
                        entryCount = log.entryCount,
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

    override suspend fun getRestSuggestion(date: String?): Result<RestSuggestion> =
        apiCallExecutor.executeAsResult { reportApi.getReportSuggestion(date) }.map { response ->
            RestSuggestion(
                suggestionType = SuggestionType.from(response.suggestionType),
                message = response.message.orEmpty(),
                excessMinutes = response.excessMinutes ?: 0,
                appName = response.appName,
            )
        }
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
