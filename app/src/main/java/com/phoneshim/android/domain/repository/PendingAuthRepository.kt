package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.SocialCredential

interface PendingAuthRepository {
    suspend fun logout(): Result<Unit>
    suspend fun recoverWithdrawal(credential: SocialCredential): Result<Unit>
    suspend fun linkAccount(credential: SocialCredential): Result<Unit>
}
