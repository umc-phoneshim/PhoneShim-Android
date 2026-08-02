package com.phoneshim.android.data.mapper

import com.phoneshim.android.data.api.CreateReminderRequest
import com.phoneshim.android.data.api.ReminderResponse
import com.phoneshim.android.data.api.ReminderRestrictModeValue
import com.phoneshim.android.data.api.UpdateReminderRequest
import com.phoneshim.android.domain.model.CreateReminderCommand
import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.model.ReminderRestrictionMode
import com.phoneshim.android.domain.model.UpdateReminderCommand
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

private val koreaZoneId: ZoneId = ZoneId.of("Asia/Seoul")

fun ReminderResponse.toDomain(): Reminder =
    Reminder(
        id = id,
        userId = userId,
        date = date.toLocalDate(),
        title = title,
        startTime = startTime.toInstant(),
        endTime = endTime.toInstant(),
        restrictionMode = restrictMode.toRestrictionMode(),
        restrictedAppIds = restrictedAppIds.toSet(),
        createdAt = createdAt.toInstant(),
        updatedAt = updatedAt.toInstant(),
    )

fun CreateReminderCommand.toRequest(): CreateReminderRequest =
    CreateReminderRequest(
        date = date.toString(),
        title = title,
        startTime = startTime.toString(),
        endTime = endTime.toString(),
        restrictMode = restrictionMode.toApiValue(),
        restrictedAppIds = restrictionMode.validatedAppIds(restrictedAppIds),
    )

fun UpdateReminderCommand.toRequest(): UpdateReminderRequest =
    UpdateReminderRequest(
        date = date?.toString(),
        title = title,
        startTime = startTime?.toString(),
        endTime = endTime?.toString(),
        restrictMode = restrictionMode?.toApiValue(),
        restrictedAppIds = when (restrictionMode) {
            ReminderRestrictionMode.NONE,
            ReminderRestrictionMode.FULL_PHONE,
            -> emptyList()
            ReminderRestrictionMode.SPECIFIC_APP -> restrictedAppIds.orEmpty().sorted()
            null -> restrictedAppIds?.sorted()
        },
    )

private fun String.toLocalDate(): LocalDate =
    runCatching { LocalDate.parse(this) }
        .recoverCatching { OffsetDateTime.parse(this).atZoneSameInstant(koreaZoneId).toLocalDate() }
        .recoverCatching { Instant.parse(this).atZone(koreaZoneId).toLocalDate() }
        .getOrThrow()

private fun String.toInstant(): Instant =
    runCatching { Instant.parse(this) }
        .recoverCatching { OffsetDateTime.parse(this).toInstant() }
        .getOrThrow()

private fun String.toRestrictionMode(): ReminderRestrictionMode =
    when (this) {
        ReminderRestrictModeValue.NONE -> ReminderRestrictionMode.NONE
        ReminderRestrictModeValue.FULL_PHONE -> ReminderRestrictionMode.FULL_PHONE
        ReminderRestrictModeValue.SPECIFIC_APP -> ReminderRestrictionMode.SPECIFIC_APP
        else -> error("Unknown reminder restrict mode: $this")
    }

private fun ReminderRestrictionMode.toApiValue(): String =
    when (this) {
        ReminderRestrictionMode.NONE -> ReminderRestrictModeValue.NONE
        ReminderRestrictionMode.FULL_PHONE -> ReminderRestrictModeValue.FULL_PHONE
        ReminderRestrictionMode.SPECIFIC_APP -> ReminderRestrictModeValue.SPECIFIC_APP
    }

private fun ReminderRestrictionMode.validatedAppIds(appIds: Set<String>): List<String> =
    if (this == ReminderRestrictionMode.SPECIFIC_APP) appIds.sorted() else emptyList()
