package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.RestSuggestion
import com.phoneshim.android.domain.repository.ReportRepository
import javax.inject.Inject

/**
 * 쉼이의 제안.
 * 백엔드가 사용 빈도 등을 분석해 완성된 문구를 내려주고, 화면은 그대로 출력합니다.
 */
class GetRestSuggestionUseCase @Inject constructor(
    private val reportRepository: ReportRepository,
) {
    suspend operator fun invoke(date: String? = null): Result<RestSuggestion> =
        reportRepository.getRestSuggestion(date)
}
