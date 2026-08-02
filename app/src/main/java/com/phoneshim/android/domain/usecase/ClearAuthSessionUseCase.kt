package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.repository.AuthSessionRepository
import javax.inject.Inject

class ClearAuthSessionUseCase @Inject constructor(
    private val authSessionRepository: AuthSessionRepository,
) {
    suspend operator fun invoke() = authSessionRepository.clearSession()
}
