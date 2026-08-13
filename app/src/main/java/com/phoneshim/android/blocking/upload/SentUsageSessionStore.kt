package com.phoneshim.android.blocking.upload

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 서버에 이미 올린 사용 구간의 키.
 *
 * 사용량(usage-logs)과 달리 사용 구간(usage-sessions)은 upsert 가 아니라 누적이고,
 * 서버가 겹치는 구간을 409 USAGE_SESSION_OVERLAP 으로 거부한다.
 * 그래서 "무엇을 이미 보냈는지" 를 기기가 기억해야 같은 구간을 다시 보내지 않는다.
 *
 * 성공한 것만 기록한다. 실패한 구간은 기록되지 않으므로 다음 주기에 자연히 재시도된다.
 */
@Singleton
class SentUsageSessionStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isSent(date: String, uploadKey: String): Boolean = keys(date).contains(uploadKey)

    fun markSent(date: String, uploadKey: String) {
        prefs.edit()
            .putStringSet(keyOf(date), keys(date) + uploadKey)
            .putStringSet(KEY_DATES, trackedDates(date))
            .apply()
    }

    /**
     * 오래된 날짜 정리. 무한정 쌓이면 SharedPreferences 가 계속 커진다.
     * 지난 날짜의 세션은 이미 다 올라갔거나 영영 못 올리므로 버려도 된다.
     */
    private fun trackedDates(newDate: String): Set<String> {
        val dates = (prefs.getStringSet(KEY_DATES, emptySet()).orEmpty() + newDate)
            .sorted()
        val kept = dates.takeLast(MAX_KEPT_DAYS).toSet()
        (dates - kept).forEach { stale -> prefs.edit().remove(keyOf(stale)).apply() }
        return kept
    }

    private fun keys(date: String): Set<String> =
        prefs.getStringSet(keyOf(date), emptySet()).orEmpty()

    private fun keyOf(date: String) = "$KEY_PREFIX$date"

    private companion object {
        const val PREFS_NAME = "usage_session_upload"
        const val KEY_DATES = "tracked_dates"
        const val KEY_PREFIX = "sent_"

        /** 하루치만 있으면 충분하지만 자정 경계와 시계 변경을 감안해 며칠 남긴다. */
        const val MAX_KEPT_DAYS = 3
    }
}
