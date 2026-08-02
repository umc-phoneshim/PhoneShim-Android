package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.repository.AuthSessionRepository
import com.phoneshim.android.domain.repository.CurrentUserRepository
import javax.inject.Inject

class ClearAuthSessionUseCase @Inject constructor(
    private val authSessionRepository: AuthSessionRepository,
    private val currentUserRepository: CurrentUserRepository,
) {
    suspend operator fun invoke() {
        try {
            authSessionRepository.clearSession()
        } finally {
            currentUserRepository.clear()
        }
    }
}
