package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.User
import com.phoneshim.android.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String, nickname: String): Result<User> =
        authRepository.signUp(email, password, nickname)
}
