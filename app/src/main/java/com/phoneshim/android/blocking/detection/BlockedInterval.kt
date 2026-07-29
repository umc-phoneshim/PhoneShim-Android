package com.phoneshim.android.blocking.detection

/**
 * 차단이 걸려 있던 시간 구간.
 *
 * 차단 중에는 사용자가 그 앱을 실제로 쓰지 못하는데, OS 의 UsageEvents 상으로는
 * 오버레이 아래 앱이 여전히 포그라운드로 남아 세션이 끊기지 않는다.
 * 그대로 두면 막혀 있던 시간이 사용량으로 잡혀, 못 썼는데 목표가 깎이고
 * 차단이 풀리는 순간 그만큼이 한꺼번에 반영된다.
 *
 * 그래서 차단 구간을 따로 기록해두고, 사용량을 계산할 때 세션과 겹치는 만큼 뺀다.
 */
data class BlockedInterval(
    val startMs: Long,
    val endMs: Long,
    val scope: BlockScope,
) {
    /** 이 구간이 해당 패키지의 사용을 실제로 막고 있었는가. */
    fun covers(packageName: String): Boolean = when (scope) {
        BlockScope.AllApps -> true
        is BlockScope.SinglePackage -> scope.packageName == packageName
    }

    /** [from,to) 와 겹치는 밀리초. 겹치지 않으면 0. */
    fun overlapMs(from: Long, to: Long): Long {
        val s = maxOf(from, startMs)
        val e = minOf(to, endMs)
        return if (e > s) e - s else 0L
    }
}

/**
 * 차단이 무엇을 막고 있었는지.
 *
 * 전체 폰 차단이면 모든 앱이 제외 대상이지만, 특정 앱 차단이면 그 앱만 제외해야 한다.
 */
sealed interface BlockScope {
    /** 전체 폰 차단. */
    data object AllApps : BlockScope

    /** 특정 앱만 차단. */
    data class SinglePackage(val packageName: String) : BlockScope
}