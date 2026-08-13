package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.SocialCredential
import com.phoneshim.android.domain.repository.PendingAuthRepository
import javax.inject.Inject

class RecoverWithdrawalUseCase @Inject constructor(
    private val repository: PendingAuthRepository,
) {
    suspend operator fun invoke(credential: SocialCredential): Result<Unit> =
        repository.recoverWithdrawal(credential)
}
