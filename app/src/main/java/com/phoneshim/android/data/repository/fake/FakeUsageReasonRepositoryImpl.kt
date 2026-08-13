package com.phoneshim.android.data.repository.fake

import com.phoneshim.android.domain.model.UsageReasonSubmission
import com.phoneshim.android.domain.repository.UsageReasonRepository
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class FakeUsageReasonRepositoryImpl @Inject constructor() : UsageReasonRepository {

    private val submissionsByPackage = ConcurrentHashMap<String, UsageReasonSubmission>()

    override suspend fun saveUsageReason(submission: UsageReasonSubmission): Result<Unit> =
        runCatching {
            /*
             * 실제 API 연동 전에도 ViewModel의 저장 완료 경계를 검증할 수 있도록
             * 프로세스 메모리에 패키지별 최신 제출 값을 저장한다. Repository 계약을
             * Result로 유지하므로 실제 구현체로 교체해도 UI 흐름은 바뀌지 않는다.
             */
            submissionsByPackage[submission.packageName] = submission
        }

    internal fun getSavedSubmission(packageName: String): UsageReasonSubmission? =
        submissionsByPackage[packageName]
}
