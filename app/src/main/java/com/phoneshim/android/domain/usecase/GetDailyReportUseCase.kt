package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.DailyReport
import com.phoneshim.android.domain.repository.ReportRepository
import javax.inject.Inject

class GetDailyReportUseCase @Inject constructor(
    private val reportRepository: ReportRepository,
) {
    suspend operator fun invoke(date: String): Result<DailyReport> =
        reportRepository.getDailyReport(date)
}
