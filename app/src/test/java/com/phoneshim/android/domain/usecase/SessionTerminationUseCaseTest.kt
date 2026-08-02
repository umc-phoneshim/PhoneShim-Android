package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.LogoutResult
import com.phoneshim.android.domain.model.User
import com.phoneshim.android.domain.model.UserStatus
import com.phoneshim.android.domain.model.WithdrawalResult
import com.phoneshim.android.domain.repository.AuthSessionRepository
import com.phoneshim.android.domain.repository.CurrentUserRepository
import com.phoneshim.android.domain.repository.MyPageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class SessionTerminationUseCaseTest {

    @Test
    fun `로그아웃은 서버 API 없이 로컬 토큰과 사용자 캐시를 삭제한다`() = runTest {
        val session = RecordingSessionRepository()
        val currentUser = RecordingCurrentUserRepository()

        val result = LogoutUseCase(session, currentUser)()

        assertEquals(LogoutResult.LocalOnly, result.getOrThrow())
        assertFalse(session.hasSession())
        assertNull(currentUser.user.value)
    }

    @Test
    fun `탈퇴 성공 시 로컬 토큰과 사용자 캐시를 삭제한다`() = runTest {
        val session = RecordingSessionRepository()
        val currentUser = RecordingCurrentUserRepository()
        val withdrawal = WithdrawalResult(UserStatus.WITHDRAWAL_PENDING, "2026-08-16")

        val result = WithdrawUseCase(
            myPageRepository = SuccessfulWithdrawalRepository(withdrawal),
            authSessionRepository = session,
            currentUserRepository = currentUser,
        )()

        assertEquals(withdrawal, result.getOrThrow())
        assertFalse(session.hasSession())
        assertNull(currentUser.user.value)
    }

    private class RecordingSessionRepository : AuthSessionRepository {
        private var active = true
        override suspend fun restoreSession() = active
        override suspend fun clearSession() { active = false }
        override fun hasSession() = active
    }

    private class RecordingCurrentUserRepository : CurrentUserRepository {
        override val user = MutableStateFlow<User?>(
            User("id", "user@example.com", "쉼이"),
        )
        override fun update(user: User) { this.user.value = user }
        override fun clear() { user.value = null }
    }

    private class SuccessfulWithdrawalRepository(
        private val result: WithdrawalResult,
    ) : MyPageRepository {
        override suspend fun getMyInfo(): Result<User> = error("unused")
        override suspend fun updateMyInfo(name: String?, motivation: String?): Result<User> =
            error("unused")
        override suspend fun withdraw() = Result.success(result)
    }
}
