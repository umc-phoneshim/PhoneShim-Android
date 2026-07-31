package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.DailyReport
import com.phoneshim.android.domain.repository.ReportRepository
import javax.inject.Inject

/** 선택 날짜의 앱별 사용량 조회. 폴 담당 UsageLog API 결과를 리포트 입력으로 사용합니다. */
class GetDailyReportUseCase @Inject constructor(
    private val reportRepository: ReportRepository,
) {
    suspend operator fun invoke(date: String, isToday: Boolean): Result<DailyReport> =
        reportRepository.getDailyReport(date, isToday)
}
