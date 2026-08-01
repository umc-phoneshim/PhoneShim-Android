package com.phoneshim.android.auth

import com.phoneshim.android.domain.model.SocialLoginResult
import com.phoneshim.android.domain.model.SocialProvider
import com.phoneshim.android.domain.repository.AuthRepository
import com.phoneshim.android.domain.repository.AuthSessionStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAuthRepository @Inject constructor(
    private val authSessionStore: AuthSessionStore,
    private val scenarioStore: FakeAuthScenarioStore,
) : AuthRepository {
    override suspend fun socialLogin(
        provider: SocialProvider,
        providerAccessToken: String,
    ): Result<SocialLoginResult> = runCatching {
        require(providerAccessToken.isNotBlank()) { "소셜 인증 토큰이 비어 있습니다." }
        if (scenarioStore.scenario == FakeAuthScenario.SERVER_FAILURE) {
            error("dev 서버 로그인 실패")
        }

        authSessionStore.saveAccessToken("dev-phoneshim-jwt")
        when (scenarioStore.scenario) {
            FakeAuthScenario.NEW_USER -> SocialLoginResult.NewUser
            else -> SocialLoginResult.ExistingUser
        }
    }
}
