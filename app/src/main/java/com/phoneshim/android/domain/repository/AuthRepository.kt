package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.SocialLoginResult
import com.phoneshim.android.domain.model.SocialProvider

interface AuthRepository {
    suspend fun socialLogin(
        provider: SocialProvider,
        providerAccessToken: String,
    ): Result<SocialLoginResult>
}
