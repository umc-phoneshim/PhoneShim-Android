package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.repository.ReminderRepository
import javax.inject.Inject

class AddReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository,
) {
    suspend operator fun invoke(reminder: Reminder): Result<Unit> =
        reminderRepository.addReminder(reminder)
}
