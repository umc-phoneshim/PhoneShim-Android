package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.ReminderApi
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.data.api.common.ApiException
import com.phoneshim.android.data.database.dao.ReminderDao
import com.phoneshim.android.data.mapper.ReminderMappingException
import com.phoneshim.android.data.mapper.toCacheEntry
import com.phoneshim.android.data.mapper.toDomain
import com.phoneshim.android.data.mapper.toRequest
import com.phoneshim.android.domain.model.CreateReminderCommand
import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.model.ReminderDataSource
import com.phoneshim.android.domain.model.ReminderListResult
import com.phoneshim.android.domain.model.UpdateReminderCommand
import com.phoneshim.android.domain.repository.ReminderRepository
import java.time.LocalDate
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderRepositoryImpl @Inject constructor(
    private val reminderApi: ReminderApi,
    private val apiCallExecutor: ApiCallExecutor,
    private val reminderDao: ReminderDao,
) : ReminderRepository {
    override suspend fun getReminders(date: LocalDate): Result<ReminderListResult> =
        try {
            val reminders = apiCallExecutor.execute { reminderApi.getReminders(date.toString()) }
                .map { it.toDomain() }
            reminderDao.replaceDate(
                dateEpochDay = date.toEpochDay(),
                entries = reminders.map(Reminder::toCacheEntry),
                syncedAtEpochMillis = Instant.now().toEpochMilli(),
            )
            Result.success(ReminderListResult(reminders, ReminderDataSource.REMOTE))
        } catch (error: CancellationException) {
            throw error
        } catch (error: ApiException.Network) {
            cachedDateOrFailure(date, error)
        } catch (error: ReminderMappingException) {
            Result.failure(ApiException.Serialization(error))
        } catch (error: Throwable) {
            Result.failure(error)
        }

    override suspend fun getReminder(id: String): Result<Reminder> =
        try {
            Result.success(apiCallExecutor.execute { reminderApi.getReminder(id) }.toDomain())
        } catch (error: CancellationException) {
            throw error
        } catch (error: ApiException.Network) {
            cachedReminderOrFailure(id, error)
        } catch (error: ReminderMappingException) {
            Result.failure(ApiException.Serialization(error))
        } catch (error: Throwable) {
            Result.failure(error)
        }

    override suspend fun createReminder(command: CreateReminderCommand): Result<Reminder> =
        resultOf {
            apiCallExecutor.execute { reminderApi.createReminder(command.toRequest()) }
                .toDomain()
                .also { reminderDao.upsert(it.toCacheEntry()) }
        }

    override suspend fun updateReminder(
        id: String,
        command: UpdateReminderCommand,
    ): Result<Reminder> =
        resultOf {
            apiCallExecutor.execute { reminderApi.updateReminder(id, command.toRequest()) }
                .toDomain()
                .also { reminderDao.upsert(it.toCacheEntry()) }
        }

    override suspend fun deleteReminder(id: String): Result<Unit> =
        resultOf {
            apiCallExecutor.executeNoContent { reminderApi.deleteReminder(id) }
            reminderDao.deleteById(id)
        }

    override fun observeReminders(date: LocalDate): Flow<List<Reminder>> =
        reminderDao.observeForDate(date.toEpochDay()).map { list -> list.map { it.toDomain() } }

    private suspend fun cachedDateOrFailure(
        date: LocalDate,
        networkError: ApiException.Network,
    ): Result<ReminderListResult> = resultOf {
        val epochDay = date.toEpochDay()
        if (reminderDao.getSyncState(epochDay) == null) throw networkError
        ReminderListResult(
            reminders = reminderDao.getForDate(epochDay).map { it.toDomain() },
            source = ReminderDataSource.CACHE,
        )
    }

    private suspend fun cachedReminderOrFailure(
        id: String,
        networkError: ApiException.Network,
    ): Result<Reminder> = resultOf {
        reminderDao.getById(id)?.toDomain() ?: throw networkError
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
