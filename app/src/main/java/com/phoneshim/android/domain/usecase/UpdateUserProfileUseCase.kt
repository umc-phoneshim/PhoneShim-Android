package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.User
import com.phoneshim.android.domain.repository.MyPageRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val repository: MyPageRepository,
) {
    suspend operator fun invoke(gender: String, ageGroup: String): Result<User> {
        if (gender !in GENDERS) {
            return Result.failure(IllegalArgumentException("지원하지 않는 성별 값입니다."))
        }
        if (ageGroup !in AGE_GROUPS) {
            return Result.failure(IllegalArgumentException("지원하지 않는 연령대 값입니다."))
        }
        return repository.updateUserProfile(gender, ageGroup)
    }

    companion object {
        val GENDERS = setOf("MALE", "FEMALE")
        val AGE_GROUPS = setOf("TEENS", "TWENTIES", "THIRTIES", "FORTIES", "FIFTIES_PLUS")
    }
}
