package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.repository.ReportRepository
import javax.inject.Inject

/** 그 달에 전체 목표를 달성한 날짜 목록. GET /api/usage-logs/calendar?month=YYYY-MM */
class GetAchievedDatesUseCase @Inject constructor(
    private val reportRepository: ReportRepository,
) {
    /** @param month YYYY-MM */
    suspend operator fun invoke(month: String): Result<List<String>> =
        reportRepository.getAchievedDates(month)
}
