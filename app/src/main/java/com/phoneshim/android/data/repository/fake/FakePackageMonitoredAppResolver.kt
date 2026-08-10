package com.phoneshim.android.data.repository.fake

import com.phoneshim.android.domain.repository.PackageMonitoredAppResolver
import javax.inject.Inject

/**
 * TODO: 노뱅의 MonitoredApp 도메인이 공개되면 실제 구현으로 교체하세요.
 * 지금은 항상 실패를 반환해 UploadUsageLogUseCase 호출부가 "매핑 안 됨"으로 처리하게 합니다.
 */
class FakePackageMonitoredAppResolver @Inject constructor() : PackageMonitoredAppResolver {
    override suspend fun resolve(packageName: String): Result<String?> = Result.success(null)
}
