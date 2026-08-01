package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.WithdrawalResult
import com.phoneshim.android.domain.repository.MyPageRepository
import javax.inject.Inject

/**
 * 회원 탈퇴 요청. API 명세 DELETE /api/auth/withdraw (구현완료).
 * 즉시 삭제가 아니라 14일 유예(WITHDRAWAL_PENDING) 상태로 전환됩니다.
 */
class WithdrawUseCase @Inject constructor(
    private val myPageRepository: MyPageRepository,
) {
    suspend operator fun invoke(): Result<WithdrawalResult> =
        myPageRepository.withdraw()
}
