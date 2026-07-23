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
