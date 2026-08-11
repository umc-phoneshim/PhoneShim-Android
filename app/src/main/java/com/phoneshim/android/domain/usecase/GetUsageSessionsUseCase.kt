package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.UsageSession
import com.phoneshim.android.domain.repository.ReportRepository
import javax.inject.Inject

/** 타임테이블용 사용 구간 조회. GET /api/usage-sessions?date= */
class GetUsageSessionsUseCase @Inject constructor(
    private val reportRepository: ReportRepository,
) {
    suspend operator fun invoke(date: String): Result<List<UsageSession>> =
        reportRepository.getUsageSessions(date)
}
