package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.User
import com.phoneshim.android.domain.repository.MyPageRepository
import javax.inject.Inject

/** 내 프로필 조회. API 명세 기준 GET /api/users/me (구현완료). */
class GetMyInfoUseCase @Inject constructor(
    private val myPageRepository: MyPageRepository,
) {
    suspend operator fun invoke(): Result<User> =
        myPageRepository.getMyInfo()
}
