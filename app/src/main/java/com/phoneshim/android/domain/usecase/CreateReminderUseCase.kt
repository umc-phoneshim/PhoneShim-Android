package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.CreateReminderCommand
import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.repository.ReminderRepository
import javax.inject.Inject

class CreateReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository,
) {
    suspend operator fun invoke(command: CreateReminderCommand): Result<Reminder> =
        reminderRepository.createReminder(command)
}
