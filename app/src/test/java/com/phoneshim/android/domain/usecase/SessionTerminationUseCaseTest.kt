package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.LogoutResult
import com.phoneshim.android.domain.model.SocialCredential
import com.phoneshim.android.domain.model.User
import com.phoneshim.android.domain.model.UserStatus
import com.phoneshim.android.domain.model.WithdrawalResult
import com.phoneshim.android.domain.repository.AuthSessionRepository
import com.phoneshim.android.domain.repository.CurrentUserRepository
import com.phoneshim.android.domain.repository.MyPageRepository
import com.phoneshim.android.domain.repository.PendingAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionTerminationUseCaseTest {

    @Test
    fun `clearing an expired session removes the token and cached user`() = runTest {
        val session = RecordingSessionRepository()
        val currentUser = RecordingCurrentUserRepository()

        ClearAuthSessionUseCase(session, currentUser)()

        assertFalse(session.hasSession())
        assertNull(currentUser.user.value)
    }

    @Test
    fun `cached user is removed even when token deletion fails`() = runTest {
        val currentUser = RecordingCurrentUserRepository()

        val result = runCatching {
            ClearAuthSessionUseCase(FailingSessionRepository(), currentUser)()
        }

        assertTrue(result.isFailure)
        assertNull(currentUser.user.value)
    }

    @Test
    fun `server confirmed logout clears local token and cached user`() = runTest {
        val session = RecordingSessionRepository()
        val currentUser = RecordingCurrentUserRepository()

        val result = LogoutUseCase(
            SuccessfulPendingAuthRepository(),
            session,
            currentUser,
        )()

        assertEquals(LogoutResult.ServerConfirmed, result.getOrThrow())
        assertFalse(session.hasSession())
        assertNull(currentUser.user.value)
    }

    @Test
    fun `server logout failure still clears local session`() = runTest {
        val session = RecordingSessionRepository()
        val currentUser = RecordingCurrentUserRepository()

        val result = LogoutUseCase(
            FailingPendingAuthRepository(),
            session,
            currentUser,
        )()

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

    private class FailingSessionRepository : AuthSessionRepository {
        override suspend fun restoreSession() = true
        override suspend fun clearSession() = error("token deletion failed")
        override fun hasSession() = true
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

    private open class SuccessfulPendingAuthRepository : PendingAuthRepository {
        override suspend fun logout() = Result.success(Unit)
        override suspend fun recoverWithdrawal(credential: SocialCredential) = error("unused")
        override suspend fun linkAccount(credential: SocialCredential) = error("unused")
    }

    private class FailingPendingAuthRepository : SuccessfulPendingAuthRepository() {
        override suspend fun logout() = Result.failure<Unit>(IllegalStateException("offline"))
    }
}
