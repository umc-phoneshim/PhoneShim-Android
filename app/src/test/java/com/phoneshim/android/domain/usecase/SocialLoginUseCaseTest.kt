package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.SocialLoginResult
import com.phoneshim.android.domain.model.SocialProvider
import com.phoneshim.android.domain.repository.AuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SocialLoginUseCaseTest {
    @Test
    fun `blank provider token is rejected before repository call`() = runTest {
        val repository = RecordingAuthRepository()
        val useCase = SocialLoginUseCase(repository)

        val result = useCase(SocialProvider.GOOGLE, "  ")

        assertTrue(result.isFailure)
        assertEquals(0, repository.callCount)
    }

    @Test
    fun `valid provider token is delegated to repository`() = runTest {
        val repository = RecordingAuthRepository()
        val useCase = SocialLoginUseCase(repository)

        val result = useCase(SocialProvider.KAKAO, "provider-token")

        assertEquals(SocialLoginResult.ExistingUser, result.getOrThrow())
        assertEquals(SocialProvider.KAKAO, repository.provider)
        assertEquals("provider-token", repository.token)
    }

    private class RecordingAuthRepository : AuthRepository {
        var callCount = 0
        var provider: SocialProvider? = null
        var token: String? = null

        override suspend fun socialLogin(
            provider: SocialProvider,
            providerAccessToken: String,
        ): Result<SocialLoginResult> {
            callCount += 1
            this.provider = provider
            token = providerAccessToken
            return Result.success(SocialLoginResult.ExistingUser)
        }
    }
}
