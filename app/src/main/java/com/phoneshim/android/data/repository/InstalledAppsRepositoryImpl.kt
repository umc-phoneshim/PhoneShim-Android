package com.phoneshim.android.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import com.phoneshim.android.domain.model.InstalledApp
import com.phoneshim.android.domain.repository.InstalledAppsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** 사용량 정렬에 쓸 조회 구간. 하루치는 그날 안 쓴 앱이 통째로 빠져 대표성이 떨어진다. */
private val USAGE_WINDOW_MILLIS = TimeUnit.DAYS.toMillis(7)

class InstalledAppsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : InstalledAppsRepository {

    /**
     * 런처(MAIN/LAUNCHER) 액티비티를 가진 앱만 조회합니다.
     * manifest의 <queries> 선언으로 QUERY_ALL_PACKAGES 없이도 목록을 볼 수 있습니다.
     *
     * 정렬은 최근 7일 사용 시간 내림차순입니다. 주의 앱 선택은 '많이 쓰는 앱'을 고르는
     * 화면이라 가나다순으로 두면 설치 앱이 많을 때 정작 필요한 앱을 찾기 어렵습니다.
     *
     * 사용량을 못 읽으면(사용정보 접근 권한 거부, 이력 없음) 가나다순으로 돌아갑니다.
     * 권한이 없을 때 queryUsageStats 는 예외가 아니라 빈 결과를 주므로 그 값으로 판별합니다.
     */
    override suspend fun getInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(intent, 0)
            .asSequence()
            .map { it.activityInfo }
            .filter { it.packageName != context.packageName } // 폰쉼 자기 자신 제외
            .distinctBy { it.packageName }
            .map { info ->
                InstalledApp(
                    packageName = info.packageName,
                    label = info.loadLabel(pm).toString(),
                )
            }
            .toList()

        sortByUsage(apps, foregroundMillisByPackage())
    }

    /**
     * 최근 7일 앱별 포그라운드 시간. 못 읽으면 빈 map 이라 호출부가 가나다순으로 돌아갑니다.
     *
     * 차단 엔진의 UsageMinutesReader 를 쓰지 않는 이유: 그쪽은 판정 전용이라 자정 기준
     * 누적에 차단 오버레이 구간 차감과 시스템앱 제외가 들어가 있어 '많이 쓰는 앱'과는
     * 다른 지표입니다. 오늘 처음 켠 앱이 0으로 밀리는 문제도 있습니다.
     */
    private fun foregroundMillisByPackage(): Map<String, Long> = runCatching {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return@runCatching emptyMap()
        val now = System.currentTimeMillis()
        usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - USAGE_WINDOW_MILLIS, now)
            .orEmpty()
            // 같은 패키지가 여러 구간으로 쪼개져 오므로 합칩니다.
            .groupingBy { it.packageName }
            .fold(0L) { acc, stats -> acc + stats.totalTimeInForeground }
            .filterValues { it > 0L }
    }.getOrDefault(emptyMap())

}

/**
 * 사용량 내림차순 정렬. 사용량을 못 읽었으면(빈 map) 가나다순으로 돌아갑니다.
 *
 * 순수 함수로 분리해 둔 이유는 폴백 동작이 이 화면의 핵심이라 단위 테스트로 고정하기 위해서입니다.
 * 권한 거부 시 예외가 아니라 빈 결과가 오므로, 빈 map 이 곧 "정렬 근거 없음"입니다.
 */
internal fun sortByUsage(
    apps: List<InstalledApp>,
    usageMillis: Map<String, Long>,
): List<InstalledApp> {
    if (usageMillis.isEmpty()) {
        return apps.sortedBy { it.label.lowercase() }
    }
    // 사용 이력이 없는 앱은 뒤로 보내되, 그 안에서는 가나다순을 유지해 찾기 쉽게 둡니다.
    return apps.sortedWith(
        compareByDescending<InstalledApp> { usageMillis[it.packageName] ?: 0L }
            .thenBy { it.label.lowercase() },
    )
}
