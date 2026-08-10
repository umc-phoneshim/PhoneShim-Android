package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.ReminderListResult
import com.phoneshim.android.domain.repository.ReminderRepository
import java.time.LocalDate
import javax.inject.Inject

class GetRemindersUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository,
) {
    suspend operator fun invoke(date: LocalDate): Result<ReminderListResult> =
        reminderRepository.getReminders(date)
}
