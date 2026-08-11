package com.phoneshim.android.data.repository

import com.phoneshim.android.domain.model.AuthException
import com.phoneshim.android.domain.model.SocialLoginResult
import com.phoneshim.android.domain.model.PendingAuthFeature
import com.phoneshim.android.domain.model.SocialIdentity
import com.phoneshim.android.domain.repository.PendingAuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnavailablePendingAuthRepositoryImpl @Inject constructor() : PendingAuthRepository {
    override suspend fun logout(): Result<Unit> =
        Result.failure(AuthException.FeatureUnavailable(PendingAuthFeature.LOGOUT))

    override suspend fun recoverWithdrawal(identity: SocialIdentity): Result<SocialLoginResult> =
        Result.failure(AuthException.FeatureUnavailable(PendingAuthFeature.RECOVER_WITHDRAWAL))

    override suspend fun linkAccount(identity: SocialIdentity): Result<Unit> =
        Result.failure(AuthException.FeatureUnavailable(PendingAuthFeature.LINK_ACCOUNT))
}
