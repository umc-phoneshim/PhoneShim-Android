package com.phoneshim.android.blocking.detection

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import java.util.Calendar
import javax.inject.Inject

/**
 * 오늘 자정부터 현재까지의 사용량.
 *
 * UsageEvents(FOREGROUND/BACKGROUND 쌍)로 세션을 직접 합산.
 *
 * queryAndAggregateUsageStats 를 쓰지 않는 이유:
 *   totalTimeInForeground 는 '지금 포그라운드에 떠 있는 앱'에 대해 실시간으로 증가하지 않는다.
 *   앱이 백그라운드로 내려갈 때 커밋되므로, 그걸로 판정하면 "쓰고 있는 앱"은 목표를 넘겨도
 *   차단되지 않고, 앱을 빠져나오는 순간에야 뒤늦게 차단이 뜬다(= 차단 기능이 무의미해짐).
 *   그래서 이벤트를 직접 훑고, 아직 안 끝난 현재 세션(now - resume)까지 더한다.
 *
 * 전체 폰 합계에서 사용자가 쓴 것이 아닌 패키지를 제외.
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
        val perPackageMs = aggregateByEvents(start, now)

        val totalMs = if (packageName == null) {
            perPackageMs
                .filterKeys { it !in launcherPackages }
                .filterKeys { it != context.packageName }
                .filterKeys { !isSystemApp(it) }
                .values.sum()
        } else {
            perPackageMs[packageName] ?: 0L
        }
        return (totalMs / 60_000L).toInt()
    }

    /**
     * [start,now) 구간의 패키지별 포그라운드 체류 시간.
     * 진행 중인 세션은 now 까지로 쳐서 더함.
     */
    private fun aggregateByEvents(start: Long, now: Long): Map<String, Long> {
        val total = HashMap<String, Long>()
        val openedAt = HashMap<String, Long>() // 아직 종료 안 된 세션의 시작 시각

        val events = usm.queryEvents(start, now)
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND ->
                    openedAt[pkg] = event.timeStamp
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    val from = openedAt.remove(pkg) ?: continue
                    if (event.timeStamp > from) {
                        total[pkg] = (total[pkg] ?: 0L) + (event.timeStamp - from)
                    }
                }
            }
        }

        // 아직 열려 있는 세션 반영.
        // 이 한 블록이 없으면 사용 중인 앱의 사용량이 영원히 늘지 않는다.
        for ((pkg, from) in openedAt) {
            if (now > from) total[pkg] = (total[pkg] ?: 0L) + (now - from)
        }
        return total
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
