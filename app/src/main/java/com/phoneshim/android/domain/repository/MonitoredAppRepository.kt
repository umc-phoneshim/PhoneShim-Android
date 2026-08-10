package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.MonitoredApp

/**
 * 주의 앱 조회와 식별자 변환.
 *
 * 서버는 주의 앱을 monitoredAppId(UUID)로 식별하지만 차단 엔진은 UsageEvents 에서
 * packageName 으로 앱을 감지합니다. 그 사이 변환을 여기서 책임집니다.
 *
 * 변환은 로컬 캐시를 먼저 봅니다. 차단 판정과 사용량 업로드는 네트워크가 없는 상황에서도
 * 돌아야 하는데, 매번 서버를 왕복하면 오프라인에서 아예 동작하지 못하기 때문입니다.
 */
interface MonitoredAppRepository {

    /** 등록된 주의 앱 목록. 서버 우선, 실패하면 로컬 캐시. */
    suspend fun getMonitoredApps(): Result<List<MonitoredApp>>

    /** packageName -> monitoredAppId. 등록되지 않은 앱이면 null. */
    suspend fun resolveMonitoredAppId(packageName: String): Result<String?>

    /** monitoredAppId -> packageName. 삭제됐거나 모르는 id 면 null. */
    suspend fun resolvePackageName(monitoredAppId: String): Result<String?>

    /**
     * monitoredAppId 여러 개를 한 번에 packageName 으로 변환합니다.
     * Reminder 의 restrictedAppIds 처럼 목록을 통째로 넘겨받는 쪽을 위한 함수입니다.
     * 변환하지 못한 id 는 결과에서 빠집니다(삭제된 주의 앱).
     */
    suspend fun resolvePackageNames(monitoredAppIds: List<String>): Result<List<String>>
}
