package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.MonitoredApp
import com.phoneshim.android.domain.repository.MonitoredAppRepository
import javax.inject.Inject

/**
 * 주의 앱 목록을 서버 기준으로 다시 맞춥니다. 캐시로 폴백하지 않습니다.
 *
 * 언제 부르나
 *  - 서버가 INVALID_RESTRICTED_APP_IDS 를 돌려준 직후.
 *    Reminder 의 restrictedAppIds 에 삭제됐거나 본인 소유가 아닌 id 가 섞였다는 뜻이라,
 *    클라이언트 목록이 서버와 어긋난 상태입니다.
 *  - [ResolveRestrictedPackageNamesUseCase] 결과의 unresolvedIds 가 비어 있지 않아
 *    목록이 오래됐다고 판단될 때.
 *
 * 왜 캐시로 폴백하지 않나
 *  정합성을 맞추려고 부르는 경로인데 서버를 못 읽었을 때 어긋난 캐시로 답하면
 *  같은 오류가 그대로 반복됩니다. 실패는 실패로 돌려주고 호출부가 재시도를 정합니다.
 */
class RefreshMonitoredAppsUseCase @Inject constructor(
    private val monitoredAppRepository: MonitoredAppRepository,
) {
    suspend operator fun invoke(): Result<List<MonitoredApp>> =
        monitoredAppRepository.refreshMonitoredApps()
}
