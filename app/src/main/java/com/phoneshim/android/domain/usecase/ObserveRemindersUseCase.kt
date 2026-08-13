package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.repository.ReminderRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveRemindersUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository,
) {
    operator fun invoke(date: LocalDate): Flow<List<Reminder>> = reminderRepository.observeReminders(date)
}
