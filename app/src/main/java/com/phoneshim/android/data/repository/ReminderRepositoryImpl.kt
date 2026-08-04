package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.ReminderApi
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.data.api.common.ApiException
import com.phoneshim.android.data.mapper.ReminderMappingException
import com.phoneshim.android.data.mapper.toDomain
import com.phoneshim.android.data.mapper.toRequest
import com.phoneshim.android.domain.model.CreateReminderCommand
import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.model.UpdateReminderCommand
import com.phoneshim.android.domain.repository.ReminderRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class ReminderRepositoryImpl @Inject constructor(
    private val reminderApi: ReminderApi,
    private val apiCallExecutor: ApiCallExecutor,
) : ReminderRepository {
    override suspend fun getReminders(date: LocalDate): Result<List<Reminder>> =
        resultOf {
            apiCallExecutor.execute { reminderApi.getReminders(date.toString()) }
                .map { it.toDomain() }
        }

    override suspend fun getReminder(id: String): Result<Reminder> =
        resultOf {
            apiCallExecutor.execute { reminderApi.getReminder(id) }.toDomain()
        }

    override suspend fun createReminder(command: CreateReminderCommand): Result<Reminder> =
        resultOf {
            apiCallExecutor.execute { reminderApi.createReminder(command.toRequest()) }.toDomain()
        }

    override suspend fun updateReminder(
        id: String,
        command: UpdateReminderCommand,
    ): Result<Reminder> =
        resultOf {
            apiCallExecutor.execute { reminderApi.updateReminder(id, command.toRequest()) }.toDomain()
        }

    override suspend fun deleteReminder(id: String): Result<Unit> =
        resultOf {
            apiCallExecutor.executeNoContent { reminderApi.deleteReminder(id) }
        }

    private suspend fun <T> resultOf(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (error: CancellationException) {
            throw error
        } catch (error: ReminderMappingException) {
            Result.failure(ApiException.Serialization(error))
        } catch (error: Throwable) {
            Result.failure(error)
        }
}
