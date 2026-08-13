package com.phoneshim.android.domain.usecase

import com.phoneshim.android.data.api.common.ApiException
import com.phoneshim.android.domain.repository.PackageMonitoredAppResolver
import com.phoneshim.android.domain.repository.ReportRepository
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import javax.inject.Inject

/**
 * 업로드할 사용 구간 한 건. 시각은 epoch millis.
 *
 * [uploadKey] 는 호출부가 "이미 보낸 구간" 을 가려내는 데 쓰는 식별자입니다.
 */
data class UsageSessionUpload(
    val packageName: String,
    val startedAt: Long,
    val endedAt: Long,
    val uploadKey: String,
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
    /**
     * @return 업로드에 성공한 구간의 [UsageSessionUpload.uploadKey] 집합.
     *   호출부는 이 값만 "보냈음" 으로 기록해야 실패분이 다음 주기에 재시도됩니다.
     */
    suspend operator fun invoke(sessions: List<UsageSessionUpload>): Set<String> {
        if (sessions.isEmpty()) return emptySet()

        // 같은 앱이 여러 구간을 가지므로 패키지당 한 번만 조회합니다.
        val resolved = mutableMapOf<String, Result<String?>>()
        val uploaded = mutableSetOf<String>()

        for (session in sessions) {
            if (session.endedAt <= session.startedAt) continue

            val lookup = resolved.getOrPut(session.packageName) {
                resolver.resolve(session.packageName)
            }

            // 조회 자체가 실패(네트워크 등)면 다음 주기에 다시 시도해야 하므로 기록하지 않습니다.
            if (lookup.isFailure) continue

            val monitoredAppId = lookup.getOrNull()
            if (monitoredAppId == null) {
                // 주의 앱이 아닙니다. 다시 시도해도 결과가 같으므로 보낸 것으로 처리해
                // 매 주기 반복 조회를 막습니다.
                uploaded += session.uploadKey
                continue
            }

            reportRepository.uploadUsageSession(
                monitoredAppId = monitoredAppId,
                startTime = session.startedAt.toIsoString(),
                endTime = session.endedAt.toIsoString(),
            )
                .onSuccess { uploaded += session.uploadKey }
                // 409 는 서버에 이미 그 구간이 있다는 뜻이라 다시 보낼 이유가 없다.
                // 실패로 두면 매 주기마다 같은 요청을 반복하게 된다.
                .onFailure { if (it.isAlreadyStored()) uploaded += session.uploadKey }
        }
        return uploaded
    }
}

/**
 * 이미 저장된 구간인가. 서버는 겹치는 세션을 USAGE_SESSION_OVERLAP 으로 거부한다.
 *
 * 오류가 두 형태로 올 수 있어 둘 다 본다.
 * - HTTP 409 로 오면 [ApiException.Http] (httpStatus = 409)
 * - HTTP 200 + success:false 로 오면 [ApiException.Server] (httpStatus 는 null)
 *
 * 이 프로젝트는 GoalErrorCodes 처럼 코드 문자열로 판별하는 방식을 쓰므로 코드를 우선한다.
 */
private fun Throwable.isAlreadyStored(): Boolean {
    val error = this as? ApiException ?: return false
    return error.code == USAGE_SESSION_OVERLAP || error.httpStatus == HTTP_CONFLICT
}

/** 겹치는 사용 구간. 재시도해도 결과가 같으므로 보낸 것으로 처리한다. */
private const val USAGE_SESSION_OVERLAP = "USAGE_SESSION_OVERLAP"
private const val HTTP_CONFLICT = 409

/** 서버는 ISO 8601 오프셋 포함 형식을 받습니다. */
private fun Long.toIsoString(): String =
    OffsetDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault()).toString()
