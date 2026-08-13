package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.SocialLoginResult
import com.phoneshim.android.domain.model.SocialProvider
import com.phoneshim.android.domain.repository.AuthRepository
import javax.inject.Inject

class SocialLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        provider: SocialProvider,
        providerToken: String,
    ): Result<SocialLoginResult> {
        if (providerToken.isBlank()) {
            return Result.failure(IllegalArgumentException("소셜 인증 토큰이 비어 있습니다."))
        }
        return authRepository.socialLogin(provider, providerToken)
    }
}
