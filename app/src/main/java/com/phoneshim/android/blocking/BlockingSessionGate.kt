package com.phoneshim.android.blocking

import android.content.Context

/**
 * 인증 세션에 따라 차단 엔진의 실행 가능 여부를 영속화합니다.
 *
 * 서비스뿐 아니라 부팅·리마인더 알람 수신기도 별도 진입점이므로, 모든 시작 경로가
 * 동일한 값을 확인해야 로그아웃 후 차단 엔진이 다시 살아나지 않습니다.
 */
object BlockingSessionGate {
    fun isEnabled(context: Context): Boolean = preferences(context)
        .getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        preferences(context)
            .edit()
            .putBoolean(KEY_ENABLED, enabled)
            .commit()
    }

    private fun preferences(context: Context) = context.applicationContext
        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private const val PREFERENCES_NAME = "blocking_session"
    private const val KEY_ENABLED = "enabled"
}
