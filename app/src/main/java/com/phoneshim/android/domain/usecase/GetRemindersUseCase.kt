package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.repository.ReminderRepository
import java.time.LocalDate
import javax.inject.Inject

class GetRemindersUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository,
) {
    suspend operator fun invoke(date: LocalDate): Result<List<Reminder>> =
        reminderRepository.getReminders(date)
}
