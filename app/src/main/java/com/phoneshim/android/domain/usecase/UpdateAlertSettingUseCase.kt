package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.AlertSetting
import com.phoneshim.android.domain.model.AlertSettingPolicy
import com.phoneshim.android.domain.model.InvalidAlertTimeException
import com.phoneshim.android.domain.repository.AlertSettingRepository
import javax.inject.Inject

class UpdateAlertSettingUseCase @Inject constructor(
    private val repository: AlertSettingRepository,
) {
    suspend operator fun invoke(alertTimeMinutes: Int): Result<AlertSetting> =
        if (AlertSettingPolicy.isValid(alertTimeMinutes)) {
            repository.updateAlertSetting(alertTimeMinutes)
        } else {
            Result.failure(InvalidAlertTimeException(alertTimeMinutes))
        }
}
