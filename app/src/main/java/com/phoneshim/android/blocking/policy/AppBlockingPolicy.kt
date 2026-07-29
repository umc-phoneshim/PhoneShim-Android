package com.phoneshim.android.blocking.policy

/**
 * 주의앱 하나에 대한 차단 정책. 온보딩/설정에서 확정되어 넘어오는 값의 엔진 측 표현.
 * domain/model 이나 pref 의 AppGoal 을 엔진이 직접 알지 않도록, 이 최소 형태로만 받는다.
 */
data class AppBlockingPolicy(
    val packageName: String,
    val appLabel: String,
    val goalMinutes: Int,
    val limitEnabled: Boolean,
)

/**
 * 전체 폰 하루 목표. 목표 분과 차단 여부를 함께 들고 다닌다.
 *
 * 둘을 따로 조회하면 provider 가 같은 행을 tick 마다 두 번 읽게 되어 한 값으로 묶었다.
 * (전체 폰 목표는 앱별 목표의 합이 아니라 독립 입력값이다.)
 */
data class PhoneGoalPolicy(
    val goalMinutes: Int,
    val limitEnabled: Boolean,
)