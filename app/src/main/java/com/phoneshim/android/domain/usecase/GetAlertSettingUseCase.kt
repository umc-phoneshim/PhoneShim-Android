package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.AlertSetting
import com.phoneshim.android.domain.repository.AlertSettingRepository
import javax.inject.Inject

class GetAlertSettingUseCase @Inject constructor(
    private val repository: AlertSettingRepository,
) {
    suspend operator fun invoke(): Result<AlertSetting> = repository.getAlertSetting()
}
