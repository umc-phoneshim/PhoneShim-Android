package com.phoneshim.android.auth

import com.phoneshim.android.domain.model.SocialIdentity
import com.phoneshim.android.domain.model.SocialLoginResult
import com.phoneshim.android.domain.repository.AuthSessionStore
import com.phoneshim.android.domain.repository.PendingAuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DevPendingAuthRepository @Inject constructor(
    private val authSessionStore: AuthSessionStore,
    private val scenarioStore: FakeAuthScenarioStore,
) : PendingAuthRepository {
    override suspend fun logout(): Result<Unit> = runCatching {
        authSessionStore.clear()
    }

    override suspend fun recoverWithdrawal(
        identity: SocialIdentity,
    ): Result<SocialLoginResult> = runCatching {
        if (scenarioStore.scenario == FakeAuthScenario.RECOVERY_FAILURE) {
            error("dev 탈퇴 복구 실패")
        }
        authSessionStore.saveAccessToken("dev-recovered-phoneshim-jwt")
        SocialLoginResult.ExistingUser
    }

    override suspend fun linkAccount(identity: SocialIdentity): Result<Unit> = Result.success(Unit)
}
