package com.phoneshim.android.blocking.policy


import javax.inject.Inject
import javax.inject.Singleton

/**
 * 순수 판정 로직. 두 소스를 합친다:
 *   1) 일정 차단 (리마인더): 지금 시간대에 걸린 제한 → 시간 기반, 우선순위 높음
 *   2) 쿼터 차단 (온보딩/설정): 오늘 사용량이 목표 도달 → 사용량 기반
 */
@Singleton
class BlockPolicyEngine @Inject constructor(
    private val goalProvider: BlockingPolicyProvider,
    private val scheduleProvider: SchedulePolicyProvider,
) {
    /**
     * 하드코딩 목록으로는 못 잡는, 기기별 기본 전화/문자 앱을 런타임에 채워 넣는 자리.
     * BlockerService 가 TelecomManager/Telephony 로 실제 기본앱을 조회해 주입한다.
     * 이게 없으면 전체 차단 중 전화/문자 버튼으로 연 앱이 허용목록에 없어 다시 차단된다.
     */
    @Volatile
    var extraAllowed: Set<String> = emptySet()

    suspend fun decide(
        foregroundPackage: String,
        phoneUsedMinutes: Int,
        appUsedMinutes: Int,
        reasonAlreadyAsked: Boolean,
    ): BlockDecision {
        if (foregroundPackage in ALWAYS_ALLOWED || foregroundPackage in extraAllowed) {
            return BlockDecision.Allow
        }

        // ── 1) 일정 차단 우선 ──
        when (val schedule = scheduleProvider.activeScheduleBlock()) {
            ScheduleBlock.None -> Unit
            ScheduleBlock.FullPhone -> return BlockDecision.PhoneBlocked
            is ScheduleBlock.SpecificApps ->
                if (foregroundPackage in schedule.packages) {
                    val label = goalProvider.watchedApps()
                        .firstOrNull { it.packageName == foregroundPackage }?.appLabel
                        ?: foregroundPackage
                    return BlockDecision.AppBlocked(foregroundPackage, label)
                }
        }

        // ── 2) 전체 폰 쿼터 ──
        val phoneGoal = goalProvider.phoneGoalMinutes()
        if (phoneGoal != null && phoneUsedMinutes >= phoneGoal) {
            return if (goalProvider.phoneLimitEnabled()) {
                BlockDecision.PhoneBlocked
            } else {
                BlockDecision.PhoneGoalReached
            }
        }

        // ── 3) 주의앱 쿼터 ──
        val policy = goalProvider.watchedApps()
            .firstOrNull { it.packageName == foregroundPackage }
            ?: return BlockDecision.Allow

        if (appUsedMinutes >= policy.goalMinutes) {
            return if (policy.limitEnabled) {
                BlockDecision.AppBlocked(policy.packageName, policy.appLabel)
            } else {
                BlockDecision.AppGoalReached(policy.packageName, policy.appLabel)
            }
        }

        // ── 4) 목표 전이면 진입 사유만 (한 번) ──
        return if (reasonAlreadyAsked) {
            BlockDecision.Allow
        } else {
            BlockDecision.UsageReasonPrompt(policy.packageName, policy.appLabel)
        }
    }

    private companion object {
        //   런처 차단 유지. 따라서 런처를 ALWAYS_ALLOWED 에 넣지 않는다.
        val ALWAYS_ALLOWED = setOf(
            "com.phoneshim.android",
            "com.android.dialer",
            "com.google.android.dialer",
            "com.samsung.android.dialer",
            "com.android.mms",
            "com.samsung.android.messaging",
            "com.google.android.apps.messaging",
        )
    }
}
