package com.phoneshim.android.data.repository

import com.phoneshim.android.domain.repository.MonitoredAppRepository
import com.phoneshim.android.domain.repository.PackageMonitoredAppResolver
import javax.inject.Inject

/**
 * packageName -> monitoredAppId 변환의 실제 구현.
 *
 * 담담·폴 쪽 [com.phoneshim.android.domain.usecase.UploadUsageLogUseCase] 가 쓰던
 * FakePackageMonitoredAppResolver 를 대체합니다. Fake 는 항상 null 을 돌려줘서
 * 사용량 업로드가 "등록된 주의 앱이 아닙니다"로 막혀 있었습니다.
 *
 * 변환 자체는 [MonitoredAppRepository] 가 캐시 우선으로 처리합니다.
 */
class MonitoredAppPackageResolver @Inject constructor(
    private val monitoredAppRepository: MonitoredAppRepository,
) : PackageMonitoredAppResolver {

    override suspend fun resolve(packageName: String): Result<String?> =
        monitoredAppRepository.resolveMonitoredAppId(packageName)
}
