package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.repository.MonitoredAppRepository
import javax.inject.Inject

/**
 * Reminder 의 restrictedAppIds(서버 monitoredAppId 목록)를 차단 엔진이 쓰는 packageName 목록으로 바꿉니다.
 *
 * 차단 엔진은 UsageEvents 에서 packageName 으로 앱을 감지하므로 UUID 를 그대로 쓰면 안 됩니다.
 * 서버에서 삭제된 주의 앱은 변환되지 않고 결과에서 빠집니다. 차단할 대상이 없으니
 * 목록에서 제외하는 편이 안전합니다.
 */
class ResolveRestrictedPackageNamesUseCase @Inject constructor(
    private val monitoredAppRepository: MonitoredAppRepository,
) {
    suspend operator fun invoke(monitoredAppIds: List<String>): Result<List<String>> =
        monitoredAppRepository.resolvePackageNames(monitoredAppIds)
}
