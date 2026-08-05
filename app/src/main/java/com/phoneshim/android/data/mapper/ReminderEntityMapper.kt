package com.phoneshim.android.data.mapper

import com.phoneshim.android.data.database.dao.ReminderCacheEntry
import com.phoneshim.android.data.database.dao.ReminderWithRestrictedApps
import com.phoneshim.android.data.database.entity.ReminderEntity
import com.phoneshim.android.data.database.entity.ReminderRestrictedAppEntity
import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.model.ReminderRestrictionMode
import java.time.Instant
import java.time.LocalDate

fun Reminder.toCacheEntry(): ReminderCacheEntry = ReminderCacheEntry(
    reminder = ReminderEntity(
        id = id,
        userId = userId,
        dateEpochDay = date.toEpochDay(),
        title = title,
        startTimeEpochMillis = startTime.toEpochMilli(),
        endTimeEpochMillis = endTime.toEpochMilli(),
        restrictionMode = restrictionMode.name,
        createdAtEpochMillis = createdAt.toEpochMilli(),
        updatedAtEpochMillis = updatedAt.toEpochMilli(),
    ),
    restrictedApps = restrictedAppIds.sorted().map { monitoredAppId ->
        ReminderRestrictedAppEntity(reminderId = id, monitoredAppId = monitoredAppId)
    },
)

fun ReminderWithRestrictedApps.toDomain(): Reminder = try {
    Reminder(
        id = reminder.id,
        userId = reminder.userId,
        date = LocalDate.ofEpochDay(reminder.dateEpochDay),
        title = reminder.title,
        startTime = Instant.ofEpochMilli(reminder.startTimeEpochMillis),
        endTime = Instant.ofEpochMilli(reminder.endTimeEpochMillis),
        restrictionMode = ReminderRestrictionMode.valueOf(reminder.restrictionMode),
        restrictedAppIds = restrictedApps.mapTo(linkedSetOf()) { it.monitoredAppId },
        createdAt = Instant.ofEpochMilli(reminder.createdAtEpochMillis),
        updatedAt = Instant.ofEpochMilli(reminder.updatedAtEpochMillis),
    )
} catch (error: RuntimeException) {
    throw ReminderMappingException("Invalid cached reminder.", error)
}
