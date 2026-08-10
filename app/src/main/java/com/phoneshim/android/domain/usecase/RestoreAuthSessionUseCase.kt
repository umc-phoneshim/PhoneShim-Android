package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.repository.AuthSessionRepository
import javax.inject.Inject

class RestoreAuthSessionUseCase @Inject constructor(
    private val authSessionRepository: AuthSessionRepository,
) {
    suspend operator fun invoke(): Boolean = authSessionRepository.restoreSession()
}
