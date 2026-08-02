package com.phoneshim.android.data.repository

import com.phoneshim.android.data.local.TokenDataSource
import com.phoneshim.android.domain.model.AuthToken
import com.phoneshim.android.domain.model.SocialLoginResult
import com.phoneshim.android.domain.model.MockAuthScenario
import com.phoneshim.android.domain.model.MockAuthScenarioStore
import com.phoneshim.android.domain.model.SocialIdentity
import com.phoneshim.android.domain.repository.PendingAuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockPendingAuthRepositoryImpl @Inject constructor(
    private val tokenDataSource: TokenDataSource,
    private val scenarioStore: MockAuthScenarioStore,
) : PendingAuthRepository {
    override suspend fun logout(): Result<Unit> = runCatching { tokenDataSource.clearSession() }

    override suspend fun recoverWithdrawal(identity: SocialIdentity): Result<SocialLoginResult> = runCatching {
        if (scenarioStore.scenario == MockAuthScenario.RECOVERY_FAILURE) {
            error("dev mock 탈퇴 복구 실패")
        }
        tokenDataSource.save(AuthToken("dev-mock-recovered-phoneshim-jwt"))
        SocialLoginResult(isNewUser = false)
    }

    override suspend fun linkAccount(identity: SocialIdentity): Result<Unit> = Result.success(Unit)
}
