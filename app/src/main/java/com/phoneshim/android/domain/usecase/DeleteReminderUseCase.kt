package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.repository.ReminderRepository
import javax.inject.Inject

class DeleteReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository,
) {
    suspend operator fun invoke(id: String): Result<Unit> = reminderRepository.deleteReminder(id)
}
