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

    /**
     * packageName == null 이면 전체 폰 합계.
     *
     * [openSessionCeilingMs] — 아직 안 끝난 세션을 이 시각까지만 집계한다.
     * 차단 오버레이가 떠 있는 동안은 사용자가 실제로 못 쓰는데도 UsageEvents 상으로는
     * 아래 앱이 계속 포그라운드로 남아 세션이 안 끊긴다. 그대로 두면 막힌 시간이 사용량으로
     * 쌓여, 못 썼는데 목표를 깎아먹고 차단이 풀리자마자 다시 걸린다.
     * 그래서 차단 중에는 서비스가 '차단 시작 시각'을 넘겨 그 이후는 세지 않게 한다.
     * 차단이 아닐 때는 now 를 넘기면 되고(실시간 집계 유지), 이게 기본값이다.
     */
    /**
     * 폰 전체 사용량과 특정 앱 사용량을 **한 번의 이벤트 조회로** 함께 계산한다.
     *
     * 두 값을 따로 구하면 자정~현재 구간을 두 번 파싱하게 되는데,
     * 이 계산은 매 tick(1초)마다 돌고 하루가 갈수록 이벤트가 쌓여
     * 시간이 지날수록 비용이 커진다(발열·배터리 소모).
     *
     * [openSessionCeilingMs] 아직 안 끝난 세션을 이 시각까지만 집계(차단 중 상한).
     * [countFromMs] 집계 시작 하한. 목표를 정한 당일에는 그 시각부터 센다.
     *   다음날부터는 startOfToday() 가 이 값을 앞질러 자동으로 자정 기준이 된다.
     */
    fun usageSnapshot(
        packageName: String,
        openSessionCeilingMs: Long = System.currentTimeMillis(),
        countFromMs: Long = 0L,
    ): UsageSnapshot {
        val start = maxOf(startOfToday(), countFromMs)
        val now = System.currentTimeMillis()
        val perPackageMs = aggregateByEvents(start, now, openSessionCeilingMs)

        val phoneMs = perPackageMs
            .filterKeys { it !in launcherPackages }
            .filterKeys { it != context.packageName }
            .filterKeys { !isSystemApp(it) }
            .values.sum()
        val appMs = perPackageMs[packageName] ?: 0L

        return UsageSnapshot(
            phoneMinutes = (phoneMs / 60_000L).toInt(),
            appMinutes = (appMs / 60_000L).toInt(),
        )
    }

    /**
     * [start,now) 구간의 패키지별 포그라운드 체류 시간.
     * 진행 중인 세션은 [ceilingMs] 까지로 쳐서 더함(차단 중 상한 처리, 위 주석 참고).
     */
    private fun aggregateByEvents(start: Long, now: Long, ceilingMs: Long): Map<String, Long> {
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

        // 아직 열려 있는 세션 반영. 단 ceiling 을 넘기지 않는다.
        // 이 한 블록이 없으면 사용 중인 앱의 사용량이 영원히 늘지 않고,
        // ceiling 이 없으면 차단 중에도 계속 쌓인다.
        val end = minOf(now, ceilingMs)
        for ((pkg, from) in openedAt) {
            if (end > from) total[pkg] = (total[pkg] ?: 0L) + (end - from)
        }
        return total
    }

    private fun resolveLauncherPackages(): Set<String> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return pm.queryIntentActivities(intent, 0)
            .mapNotNull { it.activityInfo?.packageName }
            .toSet()
    }

    // 패키지의 시스템 앱 여부는 설치 후 변하지 않는다. PackageManager 조회는 IPC 라
    // 매 tick 마다 사용 패키지 수만큼 부르면 비용이 크므로 한 번만 조회하고 캐싱한다.
    private val systemAppCache = HashMap<String, Boolean>()

    private fun isSystemApp(pkg: String): Boolean = systemAppCache.getOrPut(pkg) {
        try {
            val info = pm.getApplicationInfo(pkg, 0)
            // 시스템 앱이되, 사용자가 스토어에서 업데이트한 것(FLAG_UPDATED_SYSTEM_APP)은 실사용으로 인정
            (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
                    (info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}

/** 한 번의 조회로 얻은 사용량 스냅샷(분). */
data class UsageSnapshot(
    val phoneMinutes: Int,
    val appMinutes: Int,
)