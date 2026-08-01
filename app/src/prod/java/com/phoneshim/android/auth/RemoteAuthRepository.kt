package com.phoneshim.android.auth

import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.domain.model.SocialLoginResult
import com.phoneshim.android.domain.model.SocialProvider
import com.phoneshim.android.domain.repository.AuthRepository
import com.phoneshim.android.domain.repository.AuthSessionStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

@Singleton
class RemoteAuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val apiCallExecutor: ApiCallExecutor,
    private val authSessionStore: AuthSessionStore,
) : AuthRepository {
    override suspend fun socialLogin(
        provider: SocialProvider,
        providerAccessToken: String,
    ): Result<SocialLoginResult> = try {
        require(providerAccessToken.isNotBlank()) { "소셜 인증 토큰이 비어 있습니다." }
        val request = SocialLoginRequest(accessToken = providerAccessToken)
        val response = apiCallExecutor.execute {
            when (provider) {
                SocialProvider.GOOGLE -> authApi.loginWithGoogle(request)
                SocialProvider.KAKAO -> authApi.loginWithKakao(request)
            }
        }
        authSessionStore.saveAccessToken(response.accessToken)
        Result.success(
            if (response.isNewUser) {
                SocialLoginResult.NewUser
            } else {
                SocialLoginResult.ExistingUser
            },
        )
    } catch (error: CancellationException) {
        throw error
    } catch (error: Throwable) {
        Result.failure(error)
    }
}
