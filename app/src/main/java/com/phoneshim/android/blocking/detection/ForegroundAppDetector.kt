package com.phoneshim.android.blocking.detection

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import javax.inject.Inject

/**
 * UsageEvents 기반 포그라운드 앱 감지.
 *
 * 단일 스레드에서만 호출하는 것을 전제로 한다.
 * 오버레이 콜백 등 다른 스레드에서 재조회하면 커서가 밀리고 레이스가 생기므로,
 * 그쪽에서는 이 detector 를 다시 부르지 말고 BlockDecision 이 이미 들고 있는 값을 쓸 것.
 */
class ForegroundAppDetector @Inject constructor(
    private val context: Context,
) {
    private val usm =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    private var lastForeground: String? = null
    // 마지막으로 소비한 이벤트 시각. 다음 조회는 여기서 살짝 겹쳐 시작해 지연 이벤트를 놓치지 않는다
    private var lastConsumedTime: Long = 0L
    private var seeded: Boolean = false

    /**
     * 최신 포그라운드 패키지. 새 전환이 없으면 직전 값 유지.
     * 최초 호출 시 최근 창을 역조회해 이미 떠 있던 앱을 seed 한다.
     */
    fun currentForegroundPackage(): String? {
        val now = System.currentTimeMillis()
        val seeding = !seeded
        val from = if (seeding) {
            seeded = true
            now - SEED_LOOKBACK_MS
        } else {
            // 지연 이벤트 대비로 소폭 겹쳐서 조회
            (lastConsumedTime - OVERLAP_MS).coerceAtLeast(0L)
        }

        val events = usm.queryEvents(from, now)
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.timeStamp <= lastConsumedTime) continue // 겹침 구간 중복 소비 방지
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastForeground = event.packageName
            }
            lastConsumedTime = event.timeStamp
        }

        // 조회 구간에 이벤트가 하나도 없었던 경우. 0 으로 두면 다음 조회가
        // queryEvents(0, now) 가 되어 시스템이 보관한 이력 전체를 훑는다.
        if (lastConsumedTime == 0L) lastConsumedTime = now - OVERLAP_MS

        // seed 폴백. 지금 떠 있는 앱이 SEED_LOOKBACK_MS 보다 전에 열렸으면
        // 되돌아본 창에 여는 이벤트가 없어 null 이 되고, 그러면 BlockerService 의
        // tick 이 첫 줄에서 리턴해 그 앱을 나갈 때까지 차단이 아예 안 걸린다.
        // 창을 넓히는 것으로는 임의의 상한을 옮길 뿐이라, 집계 통계에서 직접 찾는다.
        if (seeding && lastForeground == null) {
            lastForeground = resolveMostRecentlyUsed(now)
        }
        return lastForeground
    }

    /**
     * 이벤트로 못 잡을 때 쓰는 폴백. 최근 사용 시각이 가장 늦은 패키지를 고른다.
     *
     * queryUsageStats 는 집계값이라 이벤트만큼 정확하지 않다(포그라운드가 아닌데도
     * lastTimeUsed 가 갱신되는 경우가 있다). 그래서 평상시엔 쓰지 않고,
     * 이벤트가 아무것도 없어 어차피 null 을 돌려줄 상황에서만 쓴다.
     * 틀린 값이 나와도 다음 전환 이벤트가 곧바로 덮어쓴다.
     */
    private fun resolveMostRecentlyUsed(now: Long): String? = runCatching {
        usm.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            now - STATS_LOOKBACK_MS,
            now,
        )
            ?.filter { it.lastTimeUsed > 0L }
            ?.maxByOrNull { it.lastTimeUsed }
            ?.packageName
    }.getOrNull()

    fun reset() {
        lastForeground = null
        lastConsumedTime = 0L
        seeded = false
    }

    private companion object {
        const val SEED_LOOKBACK_MS = 10 * 60 * 1000L // 시작 시 최근 10분 역조회
        const val OVERLAP_MS = 2 * 1000L             // 조회 겹침 2초
        const val STATS_LOOKBACK_MS = 24 * 60 * 60 * 1000L // 폴백 조회 창 24시간
    }
}
