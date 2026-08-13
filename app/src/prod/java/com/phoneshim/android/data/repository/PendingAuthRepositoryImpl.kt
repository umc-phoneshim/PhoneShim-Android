package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.AuthApi
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.data.api.dto.ProviderTokenRequest
import com.phoneshim.android.data.local.TokenDataSource
import com.phoneshim.android.domain.model.AuthToken
import com.phoneshim.android.domain.model.SocialCredential
import com.phoneshim.android.domain.model.SocialProvider
import com.phoneshim.android.domain.repository.PendingAuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

@Singleton
class PendingAuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val apiCallExecutor: ApiCallExecutor,
    private val tokenDataSource: TokenDataSource,
) : PendingAuthRepository {
    override suspend fun logout(): Result<Unit> = asResult {
        apiCallExecutor.executeNoContent { authApi.logout() }
    }

    override suspend fun recoverWithdrawal(credential: SocialCredential): Result<Unit> = asResult {
        val response = apiCallExecutor.execute {
            authApi.recoverWithdrawal(credential.toRequest())
        }
        tokenDataSource.save(AuthToken(response.accessToken))
    }

    override suspend fun linkAccount(credential: SocialCredential): Result<Unit> = asResult {
        apiCallExecutor.execute { authApi.linkAccount(credential.toRequest()) }
        Unit
    }

    private suspend fun <T> asResult(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (error: CancellationException) {
        throw error
    } catch (error: Throwable) {
        Result.failure(error)
    }

    private fun SocialCredential.toRequest(): ProviderTokenRequest {
        require(providerToken.isNotBlank()) { "Social provider token must not be blank." }
        return when (provider) {
            SocialProvider.GOOGLE -> ProviderTokenRequest(
                provider = provider.name,
                idToken = providerToken,
            )
            SocialProvider.KAKAO -> ProviderTokenRequest(
                provider = provider.name,
                accessToken = providerToken,
            )
        }
    }
}
