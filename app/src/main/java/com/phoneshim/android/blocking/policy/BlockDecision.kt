package com.phoneshim.android.blocking.policy

/**
 * 엔진이 "지금 이 앱을 어떻게 처리할지" 판단한 결과.
 * 어떤 오버레이 화면(연우님 Composable)을 띄울지는 이 값이 결정한다.
 */
sealed interface BlockDecision {

    /** 아무 것도 안 함. 오버레이 내림. */
    data object Allow : BlockDecision

    /** 전체 폰 목표 도달 + 차단 ON → 10_2 폰 전체 차단 → 10_2 폰 전체 차단 이후 화면 */
    data object PhoneBlocked : BlockDecision

    /** 전체 폰 목표 도달 + 차단 OFF → 10_2 목표 시간 알림 */
    data object PhoneGoalReached : BlockDecision

    /** 주의앱 목표 도달 + 차단 ON → 10_3 주의 어플 클릭 시 */
    data class AppBlocked(val packageName: String, val appLabel: String) : BlockDecision

    /** 주의앱 목표 도달 + 차단 OFF → 10_3 클릭 안 할 시 */
    data class AppGoalReached(val packageName: String, val appLabel: String) : BlockDecision

    /** 주의앱 진입 → 10_1 사용 이유 입력(1분 내 재진입 시 스킵) */
    data class UsageReasonPrompt(val packageName: String, val appLabel: String) : BlockDecision
}
