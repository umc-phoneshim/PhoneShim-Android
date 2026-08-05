package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.LogoutResult
import com.phoneshim.android.domain.repository.AuthSessionRepository
import com.phoneshim.android.domain.repository.CurrentUserRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class LogoutUseCase @Inject constructor(
    private val authSessionRepository: AuthSessionRepository,
    private val currentUserRepository: CurrentUserRepository,
) {
    suspend operator fun invoke(): Result<LogoutResult> = try {
        authSessionRepository.clearSession()
        currentUserRepository.clear()
        Result.success(LogoutResult.LocalOnly)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Throwable) {
        Result.failure(error)
    }
}
