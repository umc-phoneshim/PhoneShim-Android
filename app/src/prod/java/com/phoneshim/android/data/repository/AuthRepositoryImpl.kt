package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.AuthApi
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.data.api.common.ApiException
import com.phoneshim.android.data.api.dto.GoogleLoginRequest
import com.phoneshim.android.data.api.dto.KakaoLoginRequest
import com.phoneshim.android.data.local.TokenDataSource
import com.phoneshim.android.domain.model.AuthException
import com.phoneshim.android.domain.model.AuthToken
import com.phoneshim.android.domain.model.SocialLoginResult
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
    ): Result<SocialLoginResult> = try {
        require(providerAccessToken.isNotBlank()) { "소셜 인증 토큰이 비어 있습니다." }
        val response = apiCallExecutor.execute {
            when (provider) {
                SocialProvider.GOOGLE -> authApi.loginWithGoogle(
                    GoogleLoginRequest(idToken = providerAccessToken),
                )
                SocialProvider.KAKAO -> authApi.loginWithKakao(
                    KakaoLoginRequest(accessToken = providerAccessToken),
                )
            }
        }
        // 소셜 provider token 대신 폰쉼 서버가 발급한 JWT만 장기 세션으로 저장한다.
        tokenDataSource.save(AuthToken(response.accessToken))
        Result.success(SocialLoginResult(isNewUser = response.isNewUser))
    } catch (error: CancellationException) {
        throw error
    } catch (error: Throwable) {
        Result.failure(error.toAuthError())
    }

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
