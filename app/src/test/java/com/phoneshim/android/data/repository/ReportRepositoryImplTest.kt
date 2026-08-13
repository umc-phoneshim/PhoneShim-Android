package com.phoneshim.android.data.repository

import com.google.gson.Gson
import com.phoneshim.android.data.api.ReportApi
import com.phoneshim.android.data.api.UsageSessionCreateRequest
import com.phoneshim.android.data.api.ReportSuggestionResponse
import com.phoneshim.android.data.api.ReportSummaryResponse
import com.phoneshim.android.data.api.UsageCalendarResponse
import com.phoneshim.android.data.api.UsageSessionResponse
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.data.api.common.ApiResponse
import com.phoneshim.android.domain.model.DailyUsageLog
import com.phoneshim.android.domain.model.UsageStatus
import com.phoneshim.android.domain.repository.UsageLogRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * getDailyReport()가 더 이상 ReportApi의 usage-logs/usage-logs/status를 직접 부르지 않고,
 * 폴의 UsageLogRepository(도메인)로 위임하는지 검증. 그 외 메서드(getUsageSessions/
 * getReportSummary/getAchievedDates/getRestSuggestion)는 손대지 않았으니 다루지 않는다.
 */
class ReportRepositoryImplTest {

    private val usageLogRepository = FakeUsageLogRepository()
    private val repository = ReportRepositoryImpl(
        reportApi = FakeReportApi(),
        apiCallExecutor = ApiCallExecutor(Gson()),
        usageLogRepository = usageLogRepository,
    )

    @Test
    fun `오늘 리포트는 UsageLogRepository getUsageStatus를 도메인 모델로 매핑한다`() = runTest {
        usageLogRepository.statusResult = Result.success(
            listOf(
                UsageStatus(
                    monitoredAppId = "m-1",
                    appName = "카카오톡",
                    packageName = "com.kakao.talk",
                    usedMinutes = 30,
                    entryCount = 3,
                    targetMinutes = 60,
                    targetCount = 5,
                ),
            ),
        )

        val report = repository.getDailyReport(date = "2026-08-13", isToday = true).getOrThrow()

        assertEquals("2026-08-13", report.date)
        val usage = report.appUsages.single()
        assertEquals("m-1", usage.monitoredAppId)
        assertEquals("카카오톡", usage.appName)
        assertEquals("com.kakao.talk", usage.packageName)
        assertEquals(30, usage.usedMinutes)
        assertEquals(3, usage.entryCount)
        assertEquals(60, usage.targetMinutes)
        assertEquals(5, usage.targetCount)
    }

    @Test
    fun `오늘 리포트 조회가 실패하면 결과에 오류를 담는다`() = runTest {
        usageLogRepository.statusResult = Result.failure(IllegalStateException("network"))

        val result = repository.getDailyReport(date = "2026-08-13", isToday = true)

        assertTrue(result.isFailure)
    }

    @Test
    fun `과거 날짜 리포트는 UsageLogRepository getUsageLogs를 도메인 모델로 매핑한다`() = runTest {
        usageLogRepository.logsResult = Result.success(
            listOf(
                DailyUsageLog(id = "l-1", monitoredAppId = "m-1", date = "2026-08-01", usedMinutes = 20, entryCount = 2),
            ),
        )

        val report = repository.getDailyReport(date = "2026-08-01", isToday = false).getOrThrow()

        assertEquals("2026-08-01", report.date)
        val usage = report.appUsages.single()
        assertEquals("m-1", usage.monitoredAppId)
        assertEquals(20, usage.usedMinutes)
        assertEquals(2, usage.entryCount)
        // 과거 날짜는 앱 이름/패키지명이 내려오지 않는다.
        assertEquals("", usage.appName)
        assertEquals("", usage.packageName)
        assertEquals(usageLogRepository.requestedDate, "2026-08-01")
    }

    @Test
    fun `과거 날짜 리포트 조회가 실패하면 결과에 오류를 담는다`() = runTest {
        usageLogRepository.logsResult = Result.failure(IllegalStateException("network"))

        val result = repository.getDailyReport(date = "2026-08-01", isToday = false)

        assertTrue(result.isFailure)
    }

    private class FakeUsageLogRepository : UsageLogRepository {
        var statusResult: Result<List<UsageStatus>> = Result.success(emptyList())
        var logsResult: Result<List<DailyUsageLog>> = Result.success(emptyList())
        var requestedDate: String? = null

        override suspend fun getUsageLogs(date: String?): Result<List<DailyUsageLog>> {
            requestedDate = date
            return logsResult
        }

        override suspend fun getUsageStatus(): Result<List<UsageStatus>> = statusResult

        override suspend fun uploadUsageLog(
            monitoredAppId: String,
            usedMinutes: Int,
            entryCount: Int,
            date: String?,
        ) = throw UnsupportedOperationException()
    }

    private class FakeReportApi : ReportApi {
        override suspend fun getUsageCalendar(month: String): ApiResponse<UsageCalendarResponse> =
            throw UnsupportedOperationException()

        override suspend fun getUsageSessions(date: String?): ApiResponse<List<UsageSessionResponse>> =
            throw UnsupportedOperationException()

        override suspend fun postUsageSession(
            request: UsageSessionCreateRequest,
        ): ApiResponse<UsageSessionResponse> = throw UnsupportedOperationException()

        override suspend fun getReportSummary(
            range: String,
            date: String?,
        ): ApiResponse<ReportSummaryResponse> = throw UnsupportedOperationException()

        override suspend fun getReportSuggestion(date: String?): ApiResponse<ReportSuggestionResponse> =
            throw UnsupportedOperationException()
    }
}
