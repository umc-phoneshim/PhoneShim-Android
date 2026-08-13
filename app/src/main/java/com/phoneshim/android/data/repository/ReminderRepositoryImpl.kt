package com.phoneshim.android.data.repository

import android.util.Log
import com.phoneshim.android.data.api.ReminderApi
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.data.api.common.ApiException
import com.phoneshim.android.data.database.dao.ReminderDao
import com.phoneshim.android.data.database.dao.ReminderRestrictionDao
import com.phoneshim.android.data.database.entity.ReminderRestrictionEntity
import com.phoneshim.android.data.mapper.ReminderMappingException
import com.phoneshim.android.data.mapper.toCacheEntry
import com.phoneshim.android.data.mapper.toDomain
import com.phoneshim.android.data.mapper.toRequest
import com.phoneshim.android.domain.model.CreateReminderCommand
import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.model.ReminderDataSource
import com.phoneshim.android.domain.model.ReminderListResult
import com.phoneshim.android.domain.model.ReminderRestrictionMode
import com.phoneshim.android.domain.model.UpdateReminderCommand
import com.phoneshim.android.domain.repository.ReminderRepository
import com.phoneshim.android.domain.schedule.ReminderScheduleCoordinator
import com.phoneshim.android.domain.usecase.ResolveRestrictedPackageNamesUseCase
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderRepositoryImpl @Inject constructor(
    private val reminderApi: ReminderApi,
    private val apiCallExecutor: ApiCallExecutor,
    private val reminderDao: ReminderDao,
    private val reminderRestrictionDao: ReminderRestrictionDao,
    private val resolveRestrictedPackageNames: ResolveRestrictedPackageNamesUseCase,
    private val scheduleCoordinator: ReminderScheduleCoordinator,
) : ReminderRepository {
    override suspend fun getReminders(date: LocalDate): Result<ReminderListResult> =
        try {
            val reminders = apiCallExecutor.execute { reminderApi.getReminders(date.toString()) }
                .map { it.toDomain() }
            val restrictions = reminders.toRestrictionEntities()
            reminderDao.replaceDate(
                dateEpochDay = date.toEpochDay(),
                entries = reminders.map(Reminder::toCacheEntry),
                syncedAtEpochMillis = Instant.now().toEpochMilli(),
            )
            reminderRestrictionDao.replaceDate(
                epochDay = date.toEpochDay(),
                restrictions = restrictions,
            )
            scheduleCoordinator.refreshToday()
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
            val reminder = apiCallExecutor.execute { reminderApi.createReminder(command.toRequest()) }
                .toDomain()
            val restriction = reminder.toRestrictionEntity()
            reminderDao.upsert(reminder.toCacheEntry())
            reminderRestrictionDao.upsert(restriction)
            scheduleCoordinator.schedule(reminder.id)
            reminder
        }

    override suspend fun updateReminder(
        id: String,
        command: UpdateReminderCommand,
    ): Result<Reminder> =
        resultOf {
            val reminder = apiCallExecutor.execute { reminderApi.updateReminder(id, command.toRequest()) }
                .toDomain()
            val restriction = reminder.toRestrictionEntity()
            reminderDao.upsert(reminder.toCacheEntry())
            reminderRestrictionDao.upsert(restriction)
            scheduleCoordinator.reschedule(reminder.id)
            reminder
        }

    override suspend fun deleteReminder(id: String): Result<Unit> =
        resultOf {
            apiCallExecutor.executeNoContent { reminderApi.deleteReminder(id) }
            reminderDao.deleteById(id)
            reminderRestrictionDao.delete(id)
            scheduleCoordinator.cancel(id)
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

    private suspend fun List<Reminder>.toRestrictionEntities(): List<ReminderRestrictionEntity> {
        val restrictedIds = asSequence()
            .filter { it.restrictionMode == ReminderRestrictionMode.SPECIFIC_APP }
            .flatMap { it.restrictedAppIds.asSequence() }
            .distinct()
            .toList()
        val resolvedById = resolvePackages(restrictedIds)
        return map { it.toRestrictionEntity(resolvedById) }
    }

    private suspend fun Reminder.toRestrictionEntity(): ReminderRestrictionEntity {
        val resolvedById = if (restrictionMode == ReminderRestrictionMode.SPECIFIC_APP) {
            resolvePackages(restrictedAppIds.toList())
        } else {
            emptyMap()
        }
        return toRestrictionEntity(resolvedById)
    }

    private suspend fun resolvePackages(ids: List<String>): Map<String, String> {
        if (ids.isEmpty()) return emptyMap()
        val resolvedById = linkedMapOf<String, String>()
        val unresolvedIds = mutableListOf<String>()
        ids.distinct().forEach { id ->
            val resolved = resolveRestrictedPackageNames(listOf(id)).getOrThrow()
            val packageName = resolved.packageNames.singleOrNull()
            if (packageName == null) unresolvedIds += id else resolvedById[id] = packageName
        }
        if (unresolvedIds.isNotEmpty()) Log.w(TAG, "주의 앱 packageName 변환 실패: ids=$unresolvedIds")
        return resolvedById
    }

    private fun Reminder.toRestrictionEntity(resolvedById: Map<String, String>): ReminderRestrictionEntity {
        val start = startTime.atZone(KOREA_ZONE_ID).toLocalTime()
        val end = endTime.atZone(KOREA_ZONE_ID).toLocalTime()
        return ReminderRestrictionEntity(
            taskId = id,
            date = date.toEpochDay(),
            startMinutes = start.hour * MINUTES_PER_HOUR + start.minute,
            endMinutes = end.hour * MINUTES_PER_HOUR + end.minute,
            restrictionMode = restrictionMode.name,
            restrictedPackages = restrictedAppIds.mapNotNull(resolvedById::get).sorted().joinToString(","),
        )
    }

    private companion object {
        val KOREA_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
        const val MINUTES_PER_HOUR = 60
        const val TAG = "ReminderRepository"
    }
}
