package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.CreateReminderCommand
import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.model.ReminderListResult
import com.phoneshim.android.domain.model.UpdateReminderCommand
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    suspend fun getReminders(date: LocalDate): Result<ReminderListResult>
    suspend fun getReminder(id: String): Result<Reminder>
    suspend fun createReminder(command: CreateReminderCommand): Result<Reminder>
    suspend fun updateReminder(id: String, command: UpdateReminderCommand): Result<Reminder>
    suspend fun deleteReminder(id: String): Result<Unit>

    /** 로컬 캐시(Room)를 관찰. CRUD로 캐시가 갱신되면 즉시 재emit된다. */
    fun observeReminders(date: LocalDate): Flow<List<Reminder>>
}
