package com.phoneshim.android.blocking.service

import android.content.Context
import com.phoneshim.android.blocking.detection.BlockScope
import com.phoneshim.android.blocking.detection.BlockedInterval
import java.time.LocalDate

/**
 * 서비스 재시작을 넘겨야 하는 '하루치' 상태.
 *
 * BlockerService 는 START_STICKY 로 되살아나고 부팅 시에도 다시 뜨는데,
 * 그때마다 메모리 상태가 초기화된다. 그러면
 *   - 오늘 차단됐던 구간이 사라져, 막혀 있던 시간이 사용량으로 다시 잡히고
 *   - 이미 확인을 누른 목표 도달 알림이 그날 다시 뜬다.
 *
 * 두 값은 자정에 함께 비워지는 같은 수명이라 한 곳에서 관리한다.
 * 저장된 날짜가 오늘이 아니면 복원하지 않는다.
 *
 */
class BlockingStateStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    data class Restored(
        val blockedIntervals: List<BlockedInterval>,
        val dismissedGoals: Set<String>,
    )

    /** 저장된 날짜가 오늘일 때만 값을 돌려준다. 아니면 비우고 빈 상태로 시작. */
    fun restore(): Restored {
        if (prefs.getLong(KEY_DAY, -1L) != today()) {
            clear()
            return Restored(emptyList(), emptySet())
        }
        val intervals = prefs.getStringSet(KEY_INTERVALS, emptySet())
            .orEmpty()
            .mapNotNull(::decodeInterval)
            .sortedBy { it.startMs }
        val dismissed = prefs.getStringSet(KEY_DISMISSED, emptySet()).orEmpty().toSet()
        return Restored(intervals, dismissed)
    }

    /** 확정 구간 + 진행 중 구간을 통째로 덮어쓴다. */
    fun persistIntervals(intervals: List<BlockedInterval>) {
        prefs.edit()
            .putLong(KEY_DAY, today())
            .putStringSet(KEY_INTERVALS, intervals.map(::encodeInterval).toSet())
            .apply()
    }

    fun persistDismissed(dismissed: Set<String>) {
        prefs.edit()
            .putLong(KEY_DAY, today())
            // 방어 복사: prefs 가 돌려준 Set 을 그대로 되돌려 넣으면 안 된다(Android 알려진 함정).
            .putStringSet(KEY_DISMISSED, dismissed.toSet())
            .apply()
    }

    /** 자정 롤오버. 하루치 키만 지운다. */
    fun clear() {
        prefs.edit()
            .remove(KEY_INTERVALS)
            .remove(KEY_DISMISSED)
            .putLong(KEY_DAY, today())
            .apply()
    }

    private fun today(): Long = LocalDate.now().toEpochDay()

    // "시작,끝,ALL" 또는 "시작,끝,PKG,패키지명".
    private fun encodeInterval(i: BlockedInterval): String = when (val s = i.scope) {
        BlockScope.AllApps -> "${i.startMs},${i.endMs},$SCOPE_ALL"
        is BlockScope.SinglePackage -> "${i.startMs},${i.endMs},$SCOPE_PKG,${s.packageName}"
    }

    private fun decodeInterval(raw: String): BlockedInterval? {
        val parts = raw.split(",")
        if (parts.size < 3) return null
        val start = parts[0].toLongOrNull() ?: return null
        val end = parts[1].toLongOrNull() ?: return null
        val scope = when (parts[2]) {
            SCOPE_ALL -> BlockScope.AllApps
            SCOPE_PKG -> parts.getOrNull(3)
                ?.takeIf { it.isNotEmpty() }
                ?.let(BlockScope::SinglePackage)
                ?: return null
            else -> return null
        }
        return if (end > start) BlockedInterval(start, end, scope) else null
    }

    private companion object {
        const val PREFS_NAME = "blocking_engine" // BlockerService 와 동일 파일
        const val KEY_DAY = "state_day_epoch"
        const val KEY_INTERVALS = "blocked_intervals"
        const val KEY_DISMISSED = "dismissed_goals"
        const val SCOPE_ALL = "ALL"
        const val SCOPE_PKG = "PKG"
    }
}