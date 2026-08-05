package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.repository.ReminderRepository
import javax.inject.Inject

class GetReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository,
) {
    suspend operator fun invoke(id: String): Result<Reminder> = reminderRepository.getReminder(id)
}
