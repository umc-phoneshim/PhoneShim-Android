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
        val phoneGoal = goalProvider.phoneGoal()
        if (phoneGoal != null && phoneUsedMinutes >= phoneGoal.goalMinutes) {
            return if (phoneGoal.limitEnabled) {
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
        /**
         * 긴급 연락 보장. decide() 의 첫 분기라 일정 차단·주의앱 쿼터·사유 프롬프트를
         * 전부 건너뛴다. 즉 전화/문자 앱은 주의앱으로 등록하거나 일정 제한 대상에 넣어도
         * 차단되지 않는다 — 의도된 동작이다.
         *
         * 런처는 넣지 않는다. 전체 폰 차단 중에는 홈도 막혀야 한다.
         */
        val ALWAYS_ALLOWED = setOf(
            "com.phoneshim.android",
            "com.android.dialer",
            "com.google.android.dialer",
            "com.samsung.android.dialer",
            "com.android.mms",
            "com.samsung.android.messaging",
            "com.google.android.apps.messaging",
            // 앱이 띄우는 시스템 인증·권한 화면. 사용자가 직접 여는 앱이 아니라
            // 폰쉼 안에서 로그인·권한 허용을 하면 포그라운드로 잡히는 것들이다.
            // 막으면 전체 폰 차단 중 로그인 자체가 불가능해진다.
            "com.android.credentialmanager",
            "com.google.android.gms",
            "com.google.android.permissioncontroller",
        )
    }
}