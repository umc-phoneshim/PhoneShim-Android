package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.SocialIdentity
import com.phoneshim.android.domain.model.SocialLoginResult
import com.phoneshim.android.domain.repository.PendingAuthRepository
import javax.inject.Inject

class RecoverWithdrawalUseCase @Inject constructor(
    private val repository: PendingAuthRepository,
) {
    suspend operator fun invoke(identity: SocialIdentity): Result<SocialLoginResult> =
        repository.recoverWithdrawal(identity)
}
