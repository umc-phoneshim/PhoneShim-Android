package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.AuthApi
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.data.api.common.ApiException
import com.phoneshim.android.data.api.dto.SocialLoginRequest
import com.phoneshim.android.data.local.TokenDataSource
import com.phoneshim.android.domain.model.AuthException
import com.phoneshim.android.domain.model.AuthToken
import com.phoneshim.android.domain.model.AuthUser
import com.phoneshim.android.domain.model.SocialProvider
import com.phoneshim.android.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val apiCallExecutor: ApiCallExecutor,
    private val tokenDataSource: TokenDataSource,
) : AuthRepository {
    override suspend fun socialLogin(
        provider: SocialProvider,
        providerAccessToken: String,
    ): Result<AuthUser> = try {
        require(providerAccessToken.isNotBlank()) { "소셜 인증 토큰이 비어 있습니다." }
        val request = SocialLoginRequest(providerAccessToken)
        val response = apiCallExecutor.execute {
            when (provider) {
                SocialProvider.GOOGLE -> authApi.loginWithGoogle(request)
                SocialProvider.KAKAO -> authApi.loginWithKakao(request)
            }
        }
        tokenDataSource.save(AuthToken(response.accessToken))
        Result.success(AuthUser(isNewUser = response.isNewUser))
    } catch (error: CancellationException) {
        throw error
    } catch (error: Throwable) {
        Result.failure(error.toAuthError())
    }

    override suspend fun restoreSession(): Boolean = tokenDataSource.restore()

    private fun Throwable.toAuthError(): Throwable =
        if (this is ApiException.Http && error?.code == ACCOUNT_WITHDRAWAL_PENDING) {
            AuthException.WithdrawalPending
        } else {
            this
        }

    private companion object {
        const val ACCOUNT_WITHDRAWAL_PENDING = "ACCOUNT_WITHDRAWAL_PENDING"
    }
}
