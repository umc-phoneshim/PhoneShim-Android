package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.model.UpdateReminderCommand
import com.phoneshim.android.domain.repository.ReminderRepository
import javax.inject.Inject

class UpdateReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository,
) {
    suspend operator fun invoke(id: String, command: UpdateReminderCommand): Result<Reminder> =
        reminderRepository.updateReminder(id, command)
}
