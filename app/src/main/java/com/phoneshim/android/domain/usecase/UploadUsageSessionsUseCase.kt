package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.repository.PackageMonitoredAppResolver
import com.phoneshim.android.domain.repository.ReportRepository
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import javax.inject.Inject

/** 업로드할 사용 구간 한 건. 시각은 epoch millis. */
data class UsageSessionUpload(
    val packageName: String,
    val startedAt: Long,
    val endedAt: Long,
)

/**
 * 차단 엔진이 감지한 앱 사용 구간을 서버에 올립니다. 리포트 타임테이블의 데이터 소스입니다.
 *
 * 감지 쪽은 packageName 만 알고 서버는 monitoredAppId 를 요구해서 변환이 한 번 필요합니다.
 * [UploadUsageLogUseCase] 와 같은 [PackageMonitoredAppResolver] 를 씁니다.
 *
 * 한 건이 실패해도 나머지는 계속 올립니다. 구간은 서로 독립이고, 하나가 매핑에 실패했다고
 * 그날 타임테이블 전체를 비울 이유가 없습니다.
 */
class UploadUsageSessionsUseCase @Inject constructor(
    private val resolver: PackageMonitoredAppResolver,
    private val reportRepository: ReportRepository,
) {
    /** @return 실패한 구간 수. 0이면 전부 성공. */
    suspend operator fun invoke(sessions: List<UsageSessionUpload>): Int {
        if (sessions.isEmpty()) return 0

        // 같은 앱이 여러 구간을 가지므로 패키지당 한 번만 조회합니다.
        val resolved = mutableMapOf<String, String?>()
        var failures = 0

        for (session in sessions) {
            if (session.endedAt <= session.startedAt) continue

            val monitoredAppId = resolved.getOrPut(session.packageName) {
                resolver.resolve(session.packageName).getOrNull()
            } ?: run {
                failures++
                continue
            }

            reportRepository.uploadUsageSession(
                monitoredAppId = monitoredAppId,
                startTime = session.startedAt.toIsoString(),
                endTime = session.endedAt.toIsoString(),
            ).onFailure { failures++ }
        }
        return failures
    }
}

/** 서버는 ISO 8601 오프셋 포함 형식을 받습니다. */
private fun Long.toIsoString(): String =
    OffsetDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault()).toString()
