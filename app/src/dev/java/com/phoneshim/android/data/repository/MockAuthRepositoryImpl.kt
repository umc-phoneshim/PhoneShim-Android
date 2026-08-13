package com.phoneshim.android.data.repository

import com.phoneshim.android.data.local.TokenDataSource
import com.phoneshim.android.domain.model.AuthException
import com.phoneshim.android.domain.model.AuthToken
import com.phoneshim.android.domain.model.SocialLoginResult
import com.phoneshim.android.domain.model.MockAuthScenario
import com.phoneshim.android.domain.model.MockAuthScenarioStore
import com.phoneshim.android.domain.model.SocialProvider
import com.phoneshim.android.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockAuthRepositoryImpl @Inject constructor(
    private val tokenDataSource: TokenDataSource,
    private val scenarioStore: MockAuthScenarioStore,
) : AuthRepository {
    override suspend fun socialLogin(
        provider: SocialProvider,
        providerToken: String,
    ): Result<SocialLoginResult> = runCatching {
        require(providerToken.isNotBlank()) { "소셜 인증 토큰이 비어 있습니다." }
        when (scenarioStore.scenario) {
            MockAuthScenario.SERVER_FAILURE -> error("dev mock 서버 로그인 실패")
            MockAuthScenario.WITHDRAWAL_PENDING -> throw AuthException.WithdrawalPending
            else -> Unit
        }
        tokenDataSource.save(AuthToken("dev-mock-phoneshim-jwt"))
        SocialLoginResult(isNewUser = scenarioStore.scenario == MockAuthScenario.NEW_USER)
    }
}
