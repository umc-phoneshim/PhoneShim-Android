package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.User
import com.phoneshim.android.domain.repository.MyPageRepository
import javax.inject.Inject

/** 이름/다짐 문구 수정. API 명세 PATCH /api/users/me (예정). */
class UpdateMyInfoUseCase @Inject constructor(
    private val myPageRepository: MyPageRepository,
) {
    suspend operator fun invoke(name: String? = null, motivation: String? = null): Result<User> {
        val trimmedName = name?.trim()
        val trimmedMotivation = motivation?.trim()
        if (trimmedName != null && trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("이름을 입력해 주세요."))
        }
        if (trimmedMotivation != null && trimmedMotivation.length > MAX_MOTIVATION_LENGTH) {
            return Result.failure(
                IllegalArgumentException("다짐 문구는 ${MAX_MOTIVATION_LENGTH}자 이내로 입력해 주세요."),
            )
        }
        return myPageRepository.updateMyInfo(name = trimmedName, motivation = trimmedMotivation)
    }

    companion object {
        const val MAX_MOTIVATION_LENGTH = 100
    }
}
