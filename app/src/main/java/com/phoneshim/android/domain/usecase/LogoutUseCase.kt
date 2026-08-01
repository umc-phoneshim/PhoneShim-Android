package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.repository.PendingAuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: PendingAuthRepository,
) {
    suspend operator fun invoke(): Result<Unit> = repository.logout()
}
