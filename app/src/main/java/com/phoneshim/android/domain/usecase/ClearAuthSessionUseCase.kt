package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.repository.AuthSessionRepository
import com.phoneshim.android.domain.repository.CurrentUserRepository
import javax.inject.Inject

class ClearAuthSessionUseCase @Inject constructor(
    private val authSessionRepository: AuthSessionRepository,
    private val currentUserRepository: CurrentUserRepository,
) {
    suspend operator fun invoke() {
        try {
            authSessionRepository.clearSession()
        } finally {
            // 토큰 저장소 정리가 실패해도 이전 사용자의 화면 데이터가 새 로그인에 노출되면 안 된다.
            currentUserRepository.clear()
        }
    }
}
