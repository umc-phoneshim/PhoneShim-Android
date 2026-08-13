package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.MonitoredApp
import com.phoneshim.android.domain.model.ResolvedRestrictedApps

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

    /**
     * 서버 목록만으로 다시 읽습니다. 캐시로 폴백하지 않습니다.
     *
     * 서버가 INVALID_RESTRICTED_APP_IDS 를 돌려줬거나 삭제된 주의 앱이 의심될 때 씁니다.
     * 그 상황에서 [getMonitoredApps] 를 쓰면 서버를 못 읽었을 때 어긋난 캐시로 답해
     * 같은 오류가 반복되므로, 정합성을 맞추는 경로는 캐시를 보지 않습니다.
     */
    suspend fun refreshMonitoredApps(): Result<List<MonitoredApp>>

    /** packageName -> monitoredAppId. 등록되지 않은 앱이면 null. */
    suspend fun resolveMonitoredAppId(packageName: String): Result<String?>

    /**
     * monitoredAppId 를 packageName 으로 변환합니다.
     * Reminder 의 restrictedAppIds 처럼 목록을 통째로 넘겨받는 쪽을 위한 함수입니다.
     *
     * 단건이 필요하면 `listOf(id)` 로 부르세요. 한 개짜리 함수를 따로 두면 못 찾았을 때
     * 그냥 null 이 되어 아래 [ResolvedRestrictedApps.unresolvedIds] 구분이 사라집니다.
     *
     * 변환하지 못한 id 는 [ResolvedRestrictedApps.unresolvedIds] 로 함께 돌려줍니다.
     * 목록에서 그냥 빼버리면 '제한 없음'과 '전부 변환 실패'가 구분되지 않아,
     * 제한을 걸어둔 일정이 조용히 아무것도 막지 않는 상태가 됩니다.
     */
    suspend fun resolvePackageNames(monitoredAppIds: List<String>): Result<ResolvedRestrictedApps>
}
