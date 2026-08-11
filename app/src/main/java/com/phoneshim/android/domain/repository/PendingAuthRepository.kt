package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.SocialIdentity
import com.phoneshim.android.domain.model.SocialLoginResult

interface PendingAuthRepository {
    suspend fun logout(): Result<Unit>
    suspend fun recoverWithdrawal(identity: SocialIdentity): Result<SocialLoginResult>
    suspend fun linkAccount(identity: SocialIdentity): Result<Unit>
}
