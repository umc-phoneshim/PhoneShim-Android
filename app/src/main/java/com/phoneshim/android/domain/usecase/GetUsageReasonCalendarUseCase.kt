package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.ReasonCalendarDay
import com.phoneshim.android.domain.repository.ReportUsageReasonRepository
import javax.inject.Inject

/** 월 단위 사용 사유 입력 여부. GET /api/usage-reasons/calendar?month= (예정). */
class GetUsageReasonCalendarUseCase @Inject constructor(
    private val repository: ReportUsageReasonRepository,
) {
    /** @param month YYYY-MM */
    suspend operator fun invoke(month: String): Result<List<ReasonCalendarDay>> =
        repository.getReasonCalendar(month)
}
