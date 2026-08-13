package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.AlertSetting

interface AlertSettingRepository {
    suspend fun getAlertSetting(): Result<AlertSetting>
    suspend fun updateAlertSetting(alertTimeMinutes: Int): Result<AlertSetting>
}
