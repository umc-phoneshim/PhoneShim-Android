package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.CreateReminderCommand
import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.model.UpdateReminderCommand
import java.time.LocalDate

interface ReminderRepository {
    suspend fun getReminders(date: LocalDate): Result<List<Reminder>>
    suspend fun getReminder(id: String): Result<Reminder>
    suspend fun createReminder(command: CreateReminderCommand): Result<Reminder>
    suspend fun updateReminder(id: String, command: UpdateReminderCommand): Result<Reminder>
    suspend fun deleteReminder(id: String): Result<Unit>
}
