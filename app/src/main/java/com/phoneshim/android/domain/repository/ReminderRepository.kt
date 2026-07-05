package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getReminders(): Flow<List<Reminder>>
    suspend fun addReminder(reminder: Reminder): Result<Unit>
}
