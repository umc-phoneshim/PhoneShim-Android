package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.ReportRange
import com.phoneshim.android.domain.model.ReportSummary
import com.phoneshim.android.domain.repository.ReportRepository
import javax.inject.Inject

/** 기간별 사용 사유 요약. GET /api/reports/summary?range=day|week|month (구현완료). */
class GetReportSummaryUseCase @Inject constructor(
    private val reportRepository: ReportRepository,
) {
    suspend operator fun invoke(range: ReportRange, date: String? = null): Result<ReportSummary> =
        reportRepository.getReportSummary(range, date)
}
