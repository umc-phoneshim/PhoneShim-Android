package com.phoneshim.android.data.repository

import com.phoneshim.android.domain.model.UsageReasonEntry
import com.phoneshim.android.domain.model.UsageReasonSubmission
import com.phoneshim.android.domain.repository.PackageMonitoredAppResolver
import com.phoneshim.android.domain.repository.ReportUsageReasonRepository
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayUsageReasonRepositoryImplTest {

    @Test
    fun `packageName을 변환해 실제 사용 사유 저장소로 전달한다`() = runTest {
        val reportRepository = RecordingReportRepository()
        val repository = OverlayUsageReasonRepositoryImpl(
            packageResolver = Resolver(Result.success("monitored-1")),
            reportUsageReasonRepository = reportRepository,
        )

        val result = repository.saveUsageReason(
            UsageReasonSubmission("com.example.app", "정보를 얻기 위해"),
        )

        assertTrue(result.isSuccess)
        assertEquals("monitored-1", reportRepository.entry?.monitoredAppId)
        assertEquals("INFO", reportRepository.entry?.reasonCodes?.single()?.name)
    }

    @Test
    fun `팝업 제출 시각을 1분 구간으로 변환한다`() {
        val repository = OverlayUsageReasonRepositoryImpl(
            packageResolver = Resolver(Result.success("monitored-1")),
            reportUsageReasonRepository = RecordingReportRepository(),
        )

        val entry = repository.createEntry(
            submission = UsageReasonSubmission("com.example.app", "이동 시간 중"),
            monitoredAppId = "monitored-1",
            now = LocalDateTime.of(2026, 8, 13, 21, 47, 32),
        )

        assertEquals("2026-08-13", entry.date)
        assertEquals("2026-08-13T21:47:32", entry.timeRangeStart)
        assertEquals("2026-08-13T21:48:32", entry.timeRangeEnd)
        assertEquals("COMMUTE", entry.reasonCodes.single().name)
    }

    @Test
    fun `등록 앱을 찾지 못하면 API를 호출하지 않고 실패한다`() = runTest {
        val reportRepository = RecordingReportRepository()
        val repository = OverlayUsageReasonRepositoryImpl(
            packageResolver = Resolver(Result.success(null)),
            reportUsageReasonRepository = reportRepository,
        )

        val result = repository.saveUsageReason(
            UsageReasonSubmission("com.example.app", "기타"),
        )

        assertTrue(result.isFailure)
        assertEquals(null, reportRepository.entry)
    }

    private class Resolver(
        private val result: Result<String?>,
    ) : PackageMonitoredAppResolver {
        override suspend fun resolve(packageName: String): Result<String?> = result
    }

    private class RecordingReportRepository : ReportUsageReasonRepository {
        var entry: UsageReasonEntry? = null

        override suspend fun submitUsageReason(entry: UsageReasonEntry): Result<Unit> {
            this.entry = entry
            return Result.success(Unit)
        }
    }
}
