package com.phoneshim.android.data.repository.fake

import com.phoneshim.android.data.api.ReminderErrorCodes
import com.phoneshim.android.data.api.common.ApiError
import com.phoneshim.android.data.api.common.ApiException
import com.phoneshim.android.domain.model.CreateReminderCommand
import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.model.ReminderRestrictionMode
import com.phoneshim.android.domain.model.UpdateReminderCommand
import com.phoneshim.android.domain.repository.ReminderRepository
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 인증 기반이 합쳐지기 전 devDebug에서 Reminder 화면을 검증하기 위한 메모리 저장소.
 * 앱 프로세스를 종료하면 데이터가 초기화되며 prod 소스셋에는 포함되지 않는다.
 */
@Singleton
class FakeReminderRepositoryImpl @Inject constructor() : ReminderRepository {
    private val mutex = Mutex()
    private val reminders = linkedMapOf<String, Reminder>()

    override suspend fun getReminders(date: LocalDate): Result<List<Reminder>> = fakeResult {
        mutex.withLock {
            reminders.values
                .filter { it.date == date }
                .sortedWith(compareBy(Reminder::startTime, Reminder::endTime, Reminder::createdAt))
        }
    }

    override suspend fun getReminder(id: String): Result<Reminder> = fakeResult {
        mutex.withLock { reminders[id] ?: throw notFound() }
    }

    override suspend fun createReminder(command: CreateReminderCommand): Result<Reminder> = fakeResult {
        mutex.withLock {
            validate(command.startTime, command.endTime, command.restrictionMode, command.restrictedAppIds)
            ensureNoOverlap(command.date, command.startTime, command.endTime)
            val now = Instant.now()
            Reminder(
                id = UUID.randomUUID().toString(),
                userId = DEV_USER_ID,
                date = command.date,
                title = command.title,
                startTime = command.startTime,
                endTime = command.endTime,
                restrictionMode = command.restrictionMode,
                restrictedAppIds = command.restrictedAppIds,
                createdAt = now,
                updatedAt = now,
            ).also { reminders[it.id] = it }
        }
    }

    override suspend fun updateReminder(
        id: String,
        command: UpdateReminderCommand,
    ): Result<Reminder> = fakeResult {
        mutex.withLock {
            val previous = reminders[id] ?: throw notFound()
            val updated = previous.copy(
                date = command.date ?: previous.date,
                title = command.title ?: previous.title,
                startTime = command.startTime ?: previous.startTime,
                endTime = command.endTime ?: previous.endTime,
                restrictionMode = command.restrictionMode ?: previous.restrictionMode,
                restrictedAppIds = command.restrictedAppIds ?: previous.restrictedAppIds,
                updatedAt = Instant.now(),
            )
            validate(
                updated.startTime,
                updated.endTime,
                updated.restrictionMode,
                updated.restrictedAppIds,
            )
            ensureNoOverlap(updated.date, updated.startTime, updated.endTime, excludedId = id)
            updated.also { reminders[id] = it }
        }
    }

    override suspend fun deleteReminder(id: String): Result<Unit> = fakeResult {
        mutex.withLock {
            if (reminders.remove(id) == null) throw notFound()
        }
    }

    private fun validate(
        startTime: Instant,
        endTime: Instant,
        restrictionMode: ReminderRestrictionMode,
        restrictedAppIds: Set<String>,
    ) {
        if (!startTime.isBefore(endTime)) {
            throw serverError(ReminderErrorCodes.INVALID_TIME_RANGE, "종료 시간은 시작 시간보다 이후여야 합니다")
        }
        if (restrictionMode == ReminderRestrictionMode.SPECIFIC_APP && restrictedAppIds.isEmpty()) {
            throw serverError(
                ReminderErrorCodes.INVALID_RESTRICTED_APP_IDS,
                "특정 앱 제한에는 앱을 한 개 이상 선택해야 합니다",
            )
        }
    }

    private fun ensureNoOverlap(
        date: LocalDate,
        startTime: Instant,
        endTime: Instant,
        excludedId: String? = null,
    ) {
        val overlaps = reminders.values.any { existing ->
            existing.id != excludedId &&
                existing.date == date &&
                startTime < existing.endTime && existing.startTime < endTime
        }
        if (overlaps) {
            throw serverError(ReminderErrorCodes.REMINDER_TIME_OVERLAP, "중복된 일정은 등록할 수 없습니다")
        }
    }

    private suspend fun <T> fakeResult(block: suspend () -> T): Result<T> {
        delay(FAKE_DELAY_MILLIS)
        return try {
            Result.success(block())
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            Result.failure(error)
        }
    }

    private fun notFound() = serverError(ReminderErrorCodes.REMINDER_NOT_FOUND, "리마인더를 찾을 수 없습니다")

    private fun serverError(code: String, message: String) =
        ApiException.Server(ApiError(code = code, message = message))

    private companion object {
        const val DEV_USER_ID = "dev-user"
        const val FAKE_DELAY_MILLIS = 350L
    }
}
