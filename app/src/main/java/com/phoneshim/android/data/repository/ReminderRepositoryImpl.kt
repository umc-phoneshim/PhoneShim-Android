package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.ReminderApi
import com.phoneshim.android.data.api.ReminderResponse
import com.phoneshim.android.data.database.dao.ReminderDao
import com.phoneshim.android.data.database.entity.ReminderEntity
import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.repository.ReminderRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderRepositoryImpl @Inject constructor(
    private val reminderApi: ReminderApi,
    private val reminderDao: ReminderDao,
) : ReminderRepository {
    override fun getReminders(): Flow<List<Reminder>> =
        reminderDao.getReminders().map { entities ->
            entities.map { Reminder(id = it.id, title = it.title, scheduledAt = it.scheduledAt) }
        }

    override suspend fun addReminder(reminder: Reminder): Result<Unit> = runCatching {
        reminderApi.addReminder(
            ReminderResponse(id = reminder.id, title = reminder.title, scheduledAt = reminder.scheduledAt),
        )
        reminderDao.insert(
            ReminderEntity(id = reminder.id, title = reminder.title, scheduledAt = reminder.scheduledAt),
        )
    }
}
