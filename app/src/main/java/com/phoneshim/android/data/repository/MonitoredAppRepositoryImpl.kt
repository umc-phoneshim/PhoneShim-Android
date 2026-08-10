package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.MonitoredAppApi
import com.phoneshim.android.data.api.MonitoredAppResponse
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.data.database.dao.GoalDao
import com.phoneshim.android.domain.model.MonitoredApp
import com.phoneshim.android.domain.repository.MonitoredAppRepository
import javax.inject.Inject

/**
 * 주의 앱 조회와 packageName <-> monitoredAppId 변환.
 *
 * [변환을 캐시 우선으로 두는 이유]
 *   차단 판정과 사용량 업로드는 백그라운드에서, 네트워크가 없는 상황에서도 돌아야 합니다.
 *   변환할 때마다 서버를 왕복하면 오프라인에서 아예 동작하지 못하고, 판정 주기마다
 *   네트워크를 두드리게 됩니다. 그래서 목표 캐시(app_goal_cache)에 이미 함께 저장해 둔
 *   monitoredAppId 를 먼저 찾고, 캐시에 없을 때만 서버에 물어봅니다.
 *
 * [캐시에 없을 때]
 *   주의 앱을 다른 기기에서 등록했거나 캐시가 비워진 경우입니다. 이때는 서버 목록을 받아
 *   캐시를 갱신하지 않고 조회만 합니다. 캐시 갱신은 목표 저장·조회 경로(GoalRepository)가
 *   소유하고 있어, 여기서 같이 건드리면 두 곳이 같은 테이블을 쓰게 됩니다.
 */
class MonitoredAppRepositoryImpl @Inject constructor(
    private val monitoredAppApi: MonitoredAppApi,
    private val apiCallExecutor: ApiCallExecutor,
    private val goalDao: GoalDao,
) : MonitoredAppRepository {

    override suspend fun getMonitoredApps(): Result<List<MonitoredApp>> {
        val remote = apiCallExecutor.executeAsResult { monitoredAppApi.getMonitoredApps() }
        return remote.map { apps -> apps.map { it.toDomain() } }
            .recoverCatching { cause ->
                // 서버를 못 읽으면 캐시로 답합니다. 캐시에도 없으면 그때는 원래 오류를 올립니다.
                cachedMonitoredApps().ifEmpty { throw cause }
            }
    }

    override suspend fun resolveMonitoredAppId(packageName: String): Result<String?> {
        goalDao.findMonitoredAppId(packageName)?.let { return Result.success(it) }
        return getMonitoredApps().map { apps ->
            apps.firstOrNull { it.packageName == packageName }?.id
        }
    }

    override suspend fun resolvePackageName(monitoredAppId: String): Result<String?> {
        goalDao.findPackageName(monitoredAppId)?.let { return Result.success(it) }
        return getMonitoredApps().map { apps ->
            apps.firstOrNull { it.id == monitoredAppId }?.packageName
        }
    }

    override suspend fun resolvePackageNames(
        monitoredAppIds: List<String>,
    ): Result<List<String>> {
        if (monitoredAppIds.isEmpty()) return Result.success(emptyList())

        val fromCache = monitoredAppIds.associateWith { goalDao.findPackageName(it) }
        val missing = fromCache.filterValues { it == null }.keys
        if (missing.isEmpty()) {
            return Result.success(fromCache.values.filterNotNull())
        }

        // 캐시에 없는 id 가 하나라도 있으면 서버 목록을 한 번만 받아 함께 해결합니다.
        return getMonitoredApps().map { apps ->
            val byId = apps.associate { it.id to it.packageName }
            // 변환하지 못한 id 는 결과에서 빠집니다. 서버에서 삭제된 주의 앱이라 표시할 패키지가 없습니다.
            monitoredAppIds.mapNotNull { id -> fromCache[id] ?: byId[id] }
        }
    }

    private suspend fun cachedMonitoredApps(): List<MonitoredApp> =
        goalDao.getAppGoals().mapNotNull { entity ->
            val id = entity.monitoredAppId ?: return@mapNotNull null
            MonitoredApp(
                id = id,
                packageName = entity.packageName,
                appName = entity.appLabel,
            )
        }

    private fun MonitoredAppResponse.toDomain(): MonitoredApp = MonitoredApp(
        id = id,
        packageName = packageName,
        appName = appName,
        appIcon = appIcon,
    )
}
