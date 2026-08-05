package com.phoneshim.android.domain.repository

// packageName -> monitoredAppId 매핑. 노뱅의 MonitoredApp 도메인이 develop에 들어오고
// 공개 함수가 생기면 이 인터페이스를 구현으로 교체하고 Fake를 지우세요.
interface PackageMonitoredAppResolver {
    suspend fun resolve(packageName: String): Result<String?>
}
