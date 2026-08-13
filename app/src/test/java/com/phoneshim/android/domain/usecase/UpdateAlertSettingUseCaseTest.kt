package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.AlertSetting
import com.phoneshim.android.domain.model.InvalidAlertTimeException
import com.phoneshim.android.domain.repository.AlertSettingRepository
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateAlertSettingUseCaseTest {
    private val repository = FakeRepository()
    private val useCase = UpdateAlertSettingUseCase(repository)

    @Test
    fun `22시부터 23시 59분까지 서버에 전달한다`() = runTest {
        assertTrue(useCase(1320).isSuccess)
        assertTrue(useCase(1439).isSuccess)
        assertEquals(listOf(1320, 1439), repository.updatedMinutes)
    }

    @Test
    fun `허용 범위 밖의 시간은 서버 호출 전에 거부한다`() = runTest {
        val result = useCase(1319)

        assertTrue(result.exceptionOrNull() is InvalidAlertTimeException)
        assertTrue(repository.updatedMinutes.isEmpty())
    }

    private class FakeRepository : AlertSettingRepository {
        val updatedMinutes = mutableListOf<Int>()

        override suspend fun getAlertSetting(): Result<AlertSetting> = Result.success(setting(1320))

        override suspend fun updateAlertSetting(alertTimeMinutes: Int): Result<AlertSetting> {
            updatedMinutes += alertTimeMinutes
            return Result.success(setting(alertTimeMinutes))
        }

        private fun setting(minutes: Int) = AlertSetting(
            id = "alert-1",
            userId = "user-1",
            enabled = true,
            alertTimeMinutes = minutes,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
        )
    }
}
