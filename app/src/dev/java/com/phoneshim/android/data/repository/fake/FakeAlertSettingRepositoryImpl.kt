package com.phoneshim.android.data.repository.fake

import com.phoneshim.android.domain.model.AlertSetting
import com.phoneshim.android.domain.model.AlertSettingPolicy
import com.phoneshim.android.domain.model.InvalidAlertTimeException
import com.phoneshim.android.domain.repository.AlertSettingRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** 인증 서버 없이 Report AlertSetting UI를 검증하기 위한 dev 전용 메모리 저장소. */
@Singleton
class FakeAlertSettingRepositoryImpl @Inject constructor() : AlertSettingRepository {
    private var setting = createSetting(AlertSettingPolicy.DEFAULT_MINUTES)

    override suspend fun getAlertSetting(): Result<AlertSetting> = Result.success(setting)

    override suspend fun updateAlertSetting(alertTimeMinutes: Int): Result<AlertSetting> {
        if (!AlertSettingPolicy.isValid(alertTimeMinutes)) {
            return Result.failure(InvalidAlertTimeException(alertTimeMinutes))
        }
        setting = setting.copy(
            alertTimeMinutes = alertTimeMinutes,
            updatedAt = Instant.now(),
        )
        return Result.success(setting)
    }

    private fun createSetting(minutes: Int): AlertSetting {
        val now = Instant.now()
        return AlertSetting(
            id = "dev-alert-setting",
            userId = "dev-user",
            enabled = true,
            alertTimeMinutes = minutes,
            createdAt = now,
            updatedAt = now,
        )
    }
}
