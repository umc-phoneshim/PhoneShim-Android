package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.User
import com.phoneshim.android.domain.model.WithdrawalResult
import com.phoneshim.android.domain.repository.MyPageRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateUserProfileUseCaseTest {
    private val repository = FakeRepository()
    private val useCase = UpdateUserProfileUseCase(repository)

    @Test
    fun `서버 허용 값을 전달한다`() = runTest {
        val result = useCase("FEMALE", "FIFTIES_PLUS")

        assertTrue(result.isSuccess)
        assertEquals("FEMALE" to "FIFTIES_PLUS", repository.lastProfile)
    }

    @Test
    fun `지원하지 않는 값은 API 호출 전에 거부한다`() = runTest {
        val result = useCase("UNKNOWN", "TWENTIES")

        assertTrue(result.isFailure)
        assertEquals(null, repository.lastProfile)
    }

    private class FakeRepository : MyPageRepository {
        var lastProfile: Pair<String, String>? = null
        private val user = User(email = "user@test.com", nickname = "타로")

        override suspend fun getMyInfo() = Result.success(user)
        override suspend fun updateMyInfo(name: String?, motivation: String?) = Result.success(user)
        override suspend fun updateUserProfile(gender: String, ageGroup: String): Result<User> {
            lastProfile = gender to ageGroup
            return Result.success(user.copy(gender = gender, ageGroup = ageGroup))
        }
        override suspend fun withdraw(): Result<WithdrawalResult> = error("unused")
    }
}
