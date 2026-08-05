package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.WithdrawalResult
import com.phoneshim.android.domain.repository.MyPageRepository
import com.phoneshim.android.domain.repository.AuthSessionRepository
import com.phoneshim.android.domain.repository.CurrentUserRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/**
 * 회원 탈퇴 요청. API 명세 DELETE /api/auth/withdraw (구현완료).
 * 즉시 삭제가 아니라 14일 유예(WITHDRAWAL_PENDING) 상태로 전환됩니다.
 */
class WithdrawUseCase @Inject constructor(
    private val myPageRepository: MyPageRepository,
    private val authSessionRepository: AuthSessionRepository,
    private val currentUserRepository: CurrentUserRepository,
) {
    suspend operator fun invoke(): Result<WithdrawalResult> {
        val result = myPageRepository.withdraw()
        if (result.isFailure) return result

        return try {
            authSessionRepository.clearSession()
            currentUserRepository.clear()
            result
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Throwable) {
            Result.failure(error)
        }
    }
}
