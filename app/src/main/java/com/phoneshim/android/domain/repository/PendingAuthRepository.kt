package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.SocialIdentity
import com.phoneshim.android.domain.model.AuthUser

interface PendingAuthRepository {
    suspend fun logout(): Result<Unit>
    suspend fun recoverWithdrawal(identity: SocialIdentity): Result<AuthUser>
    suspend fun linkAccount(identity: SocialIdentity): Result<Unit>
}
