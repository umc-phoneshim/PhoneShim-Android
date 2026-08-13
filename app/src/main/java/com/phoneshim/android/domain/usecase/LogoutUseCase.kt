package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.LogoutResult
import com.phoneshim.android.domain.repository.AuthSessionRepository
import com.phoneshim.android.domain.repository.CurrentUserRepository
import com.phoneshim.android.domain.repository.PendingAuthRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class LogoutUseCase @Inject constructor(
    private val pendingAuthRepository: PendingAuthRepository,
    private val authSessionRepository: AuthSessionRepository,
    private val currentUserRepository: CurrentUserRepository,
) {
    suspend operator fun invoke(): Result<LogoutResult> = try {
        val serverConfirmed = pendingAuthRepository.logout().isSuccess
        try {
            authSessionRepository.clearSession()
        } finally {
            currentUserRepository.clear()
        }
        Result.success(
            if (serverConfirmed) LogoutResult.ServerConfirmed else LogoutResult.LocalOnly,
        )
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Throwable) {
        Result.failure(error)
    }
}
