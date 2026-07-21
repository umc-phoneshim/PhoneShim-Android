package com.phoneshim.android.blocking.detection

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.app.usage.UsageStatsManager
import java.util.Calendar
import javax.inject.Inject

/**
 * 오늘 자정부터 현재까지의 사용량.
 *
 * queryAndAggregateUsageStats 로 패키지별 병합값을 얻는다.
 *
 * 전체 폰 합계에서 사용자가 쓴 것이 아닌 패키지를 제외한다.
 *   - 런처(홈): 기기마다 다르므로 PackageManager 로 기본 런처를 동적 조회
 *   - 시스템 앱(시스템UI/키보드 등): FLAG_SYSTEM 으로 제외
 *   - 폰쉼 자신
 * 제외 안 하면 실제 스크린타임보다 부풀려져 전체폰 목표가 일찍 걸린다.
 */
class UsageMinutesReader @Inject constructor(
    private val context: Context,
) {
    private val usm =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val pm = context.packageManager

    // 기본 런처 패키지. 캐시.
    private val launcherPackages: Set<String> by lazy { resolveLauncherPackages() }

    private fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    /** packageName == null 이면 전체 폰 합계. */
    fun usedMinutesToday(packageName: String?): Int {
        val start = startOfToday()
        val now = System.currentTimeMillis()
        val aggregated = usm.queryAndAggregateUsageStats(start, now)

        val totalMs = if (packageName == null) {
            aggregated.values
                .filter { it.packageName !in launcherPackages }
                .filter { it.packageName != context.packageName }
                .filter { !isSystemApp(it.packageName) }
                .sumOf { it.totalTimeInForeground }
        } else {
            // 특정 앱은 그대로
            aggregated[packageName]?.totalTimeInForeground ?: 0L
        }
        return (totalMs / 60_000L).toInt()
    }

    private fun resolveLauncherPackages(): Set<String> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return pm.queryIntentActivities(intent, 0)
            .mapNotNull { it.activityInfo?.packageName }
            .toSet()
    }

    private fun isSystemApp(pkg: String): Boolean = try {
        val info = pm.getApplicationInfo(pkg, 0)
        // 시스템 앱이되, 사용자가 스토어에서 업데이트한 것(FLAG_UPDATED_SYSTEM_APP)은 실사용으로 인정
        (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
            (info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}
