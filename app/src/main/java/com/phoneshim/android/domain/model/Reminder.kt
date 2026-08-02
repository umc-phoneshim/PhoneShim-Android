package com.phoneshim.android.domain.model

import java.time.Instant
import java.time.LocalDate

/** 서버 DTO를 화면에 직접 노출하지 않기 위한 Reminder 도메인 모델. */
data class Reminder(
    val id: String,
    val userId: String,
    val date: LocalDate,
    val title: String,
    val startTime: Instant,
    val endTime: Instant,
    val restrictionMode: ReminderRestrictionMode,
    val restrictedAppIds: Set<String>,
    val createdAt: Instant,
    val updatedAt: Instant,
)

enum class ReminderRestrictionMode {
    NONE,
    FULL_PHONE,
    SPECIFIC_APP,
}

data class CreateReminderCommand(
    val date: LocalDate,
    val title: String,
    val startTime: Instant,
    val endTime: Instant,
    val restrictionMode: ReminderRestrictionMode = ReminderRestrictionMode.NONE,
    val restrictedAppIds: Set<String> = emptySet(),
)

/** null인 값은 PATCH 요청에서 변경하지 않는 필드다. */
data class UpdateReminderCommand(
    val date: LocalDate? = null,
    val title: String? = null,
    val startTime: Instant? = null,
    val endTime: Instant? = null,
    val restrictionMode: ReminderRestrictionMode? = null,
    val restrictedAppIds: Set<String>? = null,
)
