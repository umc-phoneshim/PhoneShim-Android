package com.phoneshim.android.data.repository

import com.phoneshim.android.domain.model.UsageReasonCode
import com.phoneshim.android.domain.model.UsageReasonEntry
import com.phoneshim.android.domain.model.UsageReasonSubmission
import com.phoneshim.android.domain.repository.PackageMonitoredAppResolver
import com.phoneshim.android.domain.repository.ReportUsageReasonRepository
import com.phoneshim.android.domain.repository.UsageReasonRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/** 차단 오버레이의 packageName 기반 입력을 실제 사용 사유 API 계약으로 변환합니다. */
class OverlayUsageReasonRepositoryImpl @Inject constructor(
    private val packageResolver: PackageMonitoredAppResolver,
    private val reportUsageReasonRepository: ReportUsageReasonRepository,
) : UsageReasonRepository {

    override suspend fun saveUsageReason(submission: UsageReasonSubmission): Result<Unit> =
        packageResolver.resolve(submission.packageName).fold(
            onSuccess = { monitoredAppId ->
                if (monitoredAppId.isNullOrBlank()) {
                    Result.failure(IllegalStateException("등록된 주의 앱을 찾을 수 없습니다."))
                } else {
                    runCatching { createEntry(submission, monitoredAppId) }
                        .fold(
                            onSuccess = { reportUsageReasonRepository.submitUsageReason(it) },
                            onFailure = { Result.failure(it) },
                        )
                }
            },
            onFailure = { Result.failure(it) },
        )

    internal fun createEntry(
        submission: UsageReasonSubmission,
        monitoredAppId: String,
        now: LocalDateTime = LocalDateTime.now(KST),
    ): UsageReasonEntry {
        val start = now.truncatedTo(ChronoUnit.HOURS)
        val reasonCode = UsageReasonCode.entries.firstOrNull { code ->
            submission.reason == code.label || submission.reason in code.overlayLabels
        } ?: error("지원하지 않는 사용 이유입니다: ${submission.reason}")

        return UsageReasonEntry(
            monitoredAppId = monitoredAppId,
            date = start.toLocalDate().toString(),
            timeRangeStart = start.toString(),
            timeRangeEnd = start.plusHours(1).toString(),
            reasonCodes = listOf(reasonCode),
        )
    }

    private val UsageReasonCode.overlayLabels: Set<String>
        get() = when (this) {
            UsageReasonCode.LEISURE -> setOf("여가 시간")
            UsageReasonCode.COMMUTE -> setOf("이동 시간 중", "이동 중")
            UsageReasonCode.HABIT -> setOf("습관적으로")
            UsageReasonCode.INFO -> setOf("정보를 얻기 위해", "정보성")
            UsageReasonCode.OTHER -> setOf("기타")
        }

    private companion object {
        val KST: ZoneId = ZoneId.of("Asia/Seoul")
    }
}
