package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.ResolvedRestrictedApps
import com.phoneshim.android.domain.repository.MonitoredAppRepository
import javax.inject.Inject

/**
 * Reminder 의 restrictedAppIds(서버 monitoredAppId 목록)를 차단 엔진이 쓰는 packageName 목록으로 바꿉니다.
 *
 * 차단 엔진은 UsageEvents 에서 packageName 으로 앱을 감지하므로 UUID 를 그대로 쓰면 안 됩니다.
 * 변환하지 못한 id 는 [ResolvedRestrictedApps.unresolvedIds] 로 함께 돌려줍니다.
 * 목록에서 빼기만 하면 '제한 없음'과 '전부 변환 실패'가 같은 빈 목록으로 보여,
 * 제한을 걸어둔 일정이 조용히 아무것도 막지 않는 상태를 알아챌 수 없습니다.
 * 그 상황에서 이전 정책을 유지할지 전체를 막을지는 차단 정책이라 엔진이 정합니다.
 */
class ResolveRestrictedPackageNamesUseCase @Inject constructor(
    private val monitoredAppRepository: MonitoredAppRepository,
) {
    suspend operator fun invoke(monitoredAppIds: List<String>): Result<ResolvedRestrictedApps> =
        monitoredAppRepository.resolvePackageNames(monitoredAppIds)
}
