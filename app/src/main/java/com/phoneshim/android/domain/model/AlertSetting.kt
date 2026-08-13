package com.phoneshim.android.domain.model

import java.time.Instant

/** 서버에 저장되는 데일리 리포트 알림 설정. */
data class AlertSetting(
    val id: String,
    val userId: String,
    val enabled: Boolean,
    val alertTimeMinutes: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val hour: Int get() = alertTimeMinutes / MINUTES_PER_HOUR
    val minute: Int get() = alertTimeMinutes % MINUTES_PER_HOUR
    val hourLabel: String get() = "%02d".format(hour)
    val minuteLabel: String get() = "%02d".format(minute)

    init {
        require(AlertSettingPolicy.isValid(alertTimeMinutes)) {
            "alertTimeMinutes must be between ${AlertSettingPolicy.MIN_MINUTES} and " +
                "${AlertSettingPolicy.MAX_MINUTES}."
        }
    }

    private companion object {
        const val MINUTES_PER_HOUR = 60
    }
}

/** API 명세 v3 및 백엔드 확인 답변에 따른 AlertSetting 입력 정책. */
object AlertSettingPolicy {
    const val MIN_MINUTES = 22 * 60
    const val MAX_MINUTES = 23 * 60 + 59
    const val DEFAULT_MINUTES = MIN_MINUTES

    fun isValid(minutes: Int): Boolean = minutes in MIN_MINUTES..MAX_MINUTES
}

class InvalidAlertTimeException(
    val alertTimeMinutes: Int,
) : IllegalArgumentException("알림 시간은 22:00~23:59 사이로 설정해 주세요.")
