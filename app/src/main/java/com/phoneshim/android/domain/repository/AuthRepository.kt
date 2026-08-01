package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.AuthUser
import com.phoneshim.android.domain.model.SocialProvider

interface AuthRepository {
    suspend fun socialLogin(
        provider: SocialProvider,
        providerAccessToken: String,
    ): Result<AuthUser>

    suspend fun restoreSession(): Boolean
}
