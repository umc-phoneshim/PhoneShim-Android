package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.repository.MyPageRepository
import javax.inject.Inject

class WithdrawUseCase @Inject constructor(
    private val myPageRepository: MyPageRepository,
) {
    suspend operator fun invoke(): Result<Unit> =
        myPageRepository.withdraw()
}
