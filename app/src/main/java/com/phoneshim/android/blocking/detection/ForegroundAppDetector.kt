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
        val from = if (!seeded) {
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
        return lastForeground
    }

    fun reset() {
        lastForeground = null
        lastConsumedTime = 0L
        seeded = false
    }

    private companion object {
        const val SEED_LOOKBACK_MS = 10 * 60 * 1000L // 시작 시 최근 10분 역조회
        const val OVERLAP_MS = 2 * 1000L             // 조회 겹침 2초
    }
}
