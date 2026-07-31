package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.UsageReasonEntry
import com.phoneshim.android.domain.repository.ReportUsageReasonRepository
import javax.inject.Inject

/** 사용 사유 입력. POST /api/usage-reasons (구현완료). */
class SubmitUsageReasonUseCase @Inject constructor(
    private val repository: ReportUsageReasonRepository,
) {
    suspend operator fun invoke(entry: UsageReasonEntry): Result<Unit> =
        repository.submitUsageReason(entry)
}
