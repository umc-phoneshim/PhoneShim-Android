package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.repository.AuthRepository
import javax.inject.Inject

class RestoreAuthSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Boolean = authRepository.restoreSession()
}
