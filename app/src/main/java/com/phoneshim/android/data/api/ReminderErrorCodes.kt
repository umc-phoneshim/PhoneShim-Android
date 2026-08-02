package com.phoneshim.android.data.api

/** Reminder API가 반환하는 도메인 오류 코드. */
object ReminderErrorCodes {
    const val VALIDATION_ERROR = "VALIDATION_ERROR"
    const val INVALID_TIME_RANGE = "INVALID_TIME_RANGE"
    const val INVALID_RESTRICT_MODE = "INVALID_RESTRICT_MODE"
    const val INVALID_RESTRICTED_APP_IDS = "INVALID_RESTRICTED_APP_IDS"
    const val REMINDER_NOT_FOUND = "REMINDER_NOT_FOUND"
    const val REMINDER_TIME_OVERLAP = "REMINDER_TIME_OVERLAP"
}
