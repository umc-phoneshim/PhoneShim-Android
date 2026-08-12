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
     * 폰 전체 사용량과 특정 앱 사용량을 **한 번의 이벤트 조회로** 함께 계산한다.
     *
     * 두 값을 따로 구하면 자정~현재 구간을 두 번 파싱하게 되는데,
     * 이 계산은 매 tick(1초)마다 돌고 하루가 갈수록 이벤트가 쌓여
     * 시간이 지날수록 비용이 커진다(발열·배터리 소모).
     *
     * 집계 기준은 항상 자정이다. 판정에 쓰는 숫자와 화면·서버에 올라가는 숫자가
     * 같은 기준을 갖도록, 이 클래스 밖의 어떤 상태도 시작 시각을 바꾸지 못하게 한다.
     *
     * [blockedIntervals] 차단이 걸려 있던 구간. 세션과 겹치는 만큼 사용량에서 제외한다.
     * [foregroundPackage] 지금 화면에 떠 있는 앱. 자정 이전부터 이어지고 있는
     *   세션을 살리는 데만 쓴다. 자세한 건 aggregateByEvents 참고. null 이면 보정하지 않는다.
     *   [packageName] 과 같은 값이 들어오는 것이 보통이지만 의미가 다르므로 따로 받는다
     *   ("사용량을 알고 싶은 앱" vs "지금 화면에 있는 앱").
     */
    fun usageSnapshot(
        packageName: String,
        blockedIntervals: List<BlockedInterval> = emptyList(),
        foregroundPackage: String? = null,
        watchedPackages: Set<String> = emptySet(),
    ): UsageSnapshot {
        val start = startOfToday()
        val now = System.currentTimeMillis()
        val perPackage = aggregateByEvents(start, now, blockedIntervals, foregroundPackage)

        val phoneMs = perPackage
            .filterKeys { it !in launcherPackages }
            .filterKeys { it != context.packageName }
            .filterKeys { !isSystemApp(it) }
            .values.sumOf { it.usedMs }
        val appMs = perPackage[packageName]?.usedMs ?: 0L

        // 오늘 한 번이라도 쓴 주의앱만 담는다. 안 쓴 앱은 서버에 보낼 값이 없다.
        val watched = watchedPackages.mapNotNull { pkg ->
            val aggregate = perPackage[pkg] ?: return@mapNotNull null
            AppUsageSnapshot(
                packageName = pkg,
                usedMinutes = (aggregate.usedMs / 60_000L).toInt(),
                entryCount = aggregate.entryCount,
            )
        }

        return UsageSnapshot(
            phoneMinutes = (phoneMs / 60_000L).toInt(),
            appMinutes = (appMs / 60_000L).toInt(),
            watchedApps = watched,
        )
    }

    /**
     * [start,now) 구간의 패키지별 포그라운드 체류 시간.
     * 각 세션에서 [blockedIntervals] 와 겹치는 시간은 제외한다.
     *
     * 세션은 MOVE_TO_FOREGROUND 를 봐야 열린다. 그래서 [start] 이전에 열려서 지금까지
     * 이어지는 세션은 이 구간에 여는 이벤트가 없어 통째로 빠진다.
     * (23:50 에 켠 앱을 00:30 까지 쓰면 오늘치 30분이 0분으로 잡히고,
     *  앱을 빠져나가는 순간 MOVE_TO_BACKGROUND 가 짝 없는 이벤트로 버려져 영구 손실된다.)
     * 사용량이 실제보다 적게 잡히는 쪽이라 차단이 늦어지므로 방향도 나쁘다.
     *
     * [foregroundPackage] 로 보정한다. 그 앱의 전환 이벤트가 이 구간에 하나도 없는데
     * 지금 화면에 떠 있다면, [start] 부터 계속 떠 있었다는 뜻이므로 그 시각에 연 것으로 본다.
     * 구간 안에서 들락거렸다면 이벤트가 남아 있어 이 보정에 걸리지 않는다.
     */
    private fun aggregateByEvents(
        start: Long,
        now: Long,
        blockedIntervals: List<BlockedInterval>,
        foregroundPackage: String?,
    ): Map<String, PackageAggregate> {
        val usedMs = HashMap<String, Long>()
        val entries = HashMap<String, Int>()   // 진입 횟수. 1분 내 재진입은 묶는다.
        val exitedAt = HashMap<String, Long>() // 직전 이탈 시각. 재진입 묶음 판정용.
        val openedAt = HashMap<String, Long>() // 아직 종료 안 된 세션의 시작 시각
        val switched = HashSet<String>()       // 이 구간에 전환 이벤트가 한 번이라도 관측된 패키지

        val events = usm.queryEvents(start, now)
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    switched += pkg
                    openedAt[pkg] = event.timeStamp
                    if (isNewEntry(exitedAt[pkg], event.timeStamp)) {
                        entries[pkg] = (entries[pkg] ?: 0) + 1
                    }
                }
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    switched += pkg
                    exitedAt[pkg] = event.timeStamp
                    val from = openedAt.remove(pkg) ?: continue
                    val counted = countedMs(pkg, from, event.timeStamp, blockedIntervals)
                    if (counted > 0L) usedMs[pkg] = (usedMs[pkg] ?: 0L) + counted
                }
            }
        }

        // start 이전부터 이어지는 세션 보정. 위 KDoc 참고.
        // 진입 이벤트가 아니므로 진입 횟수는 올리지 않는다.
        // (자정 이전부터 이어진 세션은 당일 진입 횟수에 잡히지 않는다 - 이슈 #45 완료 조건)
        if (foregroundPackage != null && foregroundPackage !in switched) {
            openedAt[foregroundPackage] = start
        }

        // 아직 열려 있는 세션 반영. 이 블록이 없으면 사용 중인 앱의 사용량이 영원히 늘지 않는다.
        // 차단 중인 앱도 세션이 열린 채로 남으므로, 여기서도 차단 구간을 제외한다.
        for ((pkg, from) in openedAt) {
            val counted = countedMs(pkg, from, now, blockedIntervals)
            if (counted > 0L) usedMs[pkg] = (usedMs[pkg] ?: 0L) + counted
        }

        return (usedMs.keys + entries.keys).associateWith { pkg ->
            PackageAggregate(
                usedMs = usedMs[pkg] ?: 0L,
                entryCount = entries[pkg] ?: 0,
            )
        }
    }

    /**
     * 세션 [from,to) 중 실제로 사용한 것으로 인정할 시간.
     * 해당 패키지를 막고 있던 차단 구간과 겹치는 만큼 뺀다.
     *
     * 동시에 두 종류의 차단이 걸리지는 않으므로(판정은 매 tick 하나만 내려간다)
     * 구간끼리 겹치지 않는다고 보고 단순 합산한다. 음수 방어만 둔다.
     */
    private fun countedMs(
        pkg: String,
        from: Long,
        to: Long,
        blockedIntervals: List<BlockedInterval>,
    ): Long {
        if (to <= from) return 0L
        var blocked = 0L
        for (interval in blockedIntervals) {
            if (interval.covers(pkg)) blocked += interval.overlapMs(from, to)
        }
        return ((to - from) - blocked).coerceAtLeast(0L)
    }

    /** 한 패키지의 오늘 집계 중간값. 이 클래스 밖으로 나가지 않는다. */
    private data class PackageAggregate(
        val usedMs: Long,
        val entryCount: Int,
    )

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

/**
 * 이 시간 안에 다시 들어오면 같은 진입으로 본다. 기획 MAIN104.
 */
internal const val REENTRY_WINDOW_MS = 60_000L

/**
 * 이 포그라운드 전환을 새 진입으로 셀지.
 *
 * 기획(MAIN104): "진입 횟수는 1분 내 재진입 시 1회로 처리한다."
 * 알림을 확인하려고 잠깐 나갔다 오는 것을 두 번으로 세지 않기 위한 규칙이다.
 * 직전 이탈 기록이 없으면(오늘 첫 진입) 항상 새 진입이다.
 *
 * [UsageMinutesReader] 는 UsageStatsManager 를 직접 잡아 인스턴스를 만들 수 없으므로,
 * 규칙만 최상위 함수로 빼서 단위 테스트가 가능하게 둔다.
 *
 * @param lastExitAt 직전 이탈 시각. 오늘 첫 진입이면 null.
 * @param enteredAt 이번 진입 시각.
 */
internal fun isNewEntry(lastExitAt: Long?, enteredAt: Long): Boolean =
    lastExitAt == null || enteredAt - lastExitAt >= REENTRY_WINDOW_MS

/** 한 번의 조회로 얻은 사용량 스냅샷(분). */
data class UsageSnapshot(
    val phoneMinutes: Int,
    val appMinutes: Int,
    /**
     * 주의앱별 오늘 사용량. usageSnapshot(watchedPackages = ...) 로 요청했을 때만 채워진다.
     * 서버 업로드용이며, 오늘 사용 기록이 없는 앱은 담기지 않는다.
     */
    val watchedApps: List<AppUsageSnapshot> = emptyList(),
)

/**
 * 오늘(기기 자정)부터 현재까지 누적된 주의앱 하나의 사용량.
 *
 * 하루가 끝난 확정값이 아니라 그 시점의 스냅샷이라 조회할 때마다 값이 커진다.
 * 서버 업로드(PUT /api/usage-logs)에 그대로 실린다.
 */
data class AppUsageSnapshot(
    val packageName: String,
    val usedMinutes: Int,
    val entryCount: Int,
)