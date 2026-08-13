package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.RestSuggestion
import com.phoneshim.android.domain.repository.ReportRepository
import javax.inject.Inject

/**
 * 쉼이의 제안. GET /api/reports/suggestion?date=
 *
 * AI가 아니라 백엔드가 목표 대비 사용량을 보고 정해진 문구를 골라 줍니다.
 */
class GetRestSuggestionUseCase @Inject constructor(
    private val reportRepository: ReportRepository,
) {
    suspend operator fun invoke(date: String? = null): Result<RestSuggestion> =
        reportRepository.getRestSuggestion(date)
}
