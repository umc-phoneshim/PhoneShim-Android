package com.phoneshim.android.data.realtime

import com.phoneshim.android.data.local.TokenProvider
import com.phoneshim.android.domain.model.AuthSessionState
import com.phoneshim.android.domain.model.CreateReminderCommand
import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.model.ReminderDataSource
import com.phoneshim.android.domain.model.ReminderListResult
import com.phoneshim.android.domain.model.UpdateReminderCommand
import com.phoneshim.android.domain.repository.AuthSessionRepository
import com.phoneshim.android.domain.repository.ReminderRepository
import com.phoneshim.android.domain.usecase.GetRemindersUseCase
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderSocketSessionCoordinatorTest {
    @Test
    fun `인증 상태에서만 최신 토큰으로 연결하고 로그아웃하면 해제한다`() = runTest {
        val fixture = fixture(backgroundScope)
        fixture.coordinator.start()
        runCurrent()
        assertTrue(fixture.socket.connectedTokens.isEmpty())

        fixture.token.value = "first-token"
        fixture.session.state.value = AuthSessionState.AUTHENTICATED
        runCurrent()
        assertEquals(listOf("first-token"), fixture.socket.connectedTokens)

        fixture.session.state.value = AuthSessionState.UNAUTHENTICATED
        runCurrent()
        assertTrue(fixture.socket.disconnectCount >= 1)
    }

    @Test
    fun `재연결 직전에 TokenProvider의 최신 토큰을 다시 읽는다`() = runTest {
        val fixture = fixture(backgroundScope, initialState = AuthSessionState.AUTHENTICATED, token = "old-token")
        fixture.coordinator.start()
        runCurrent()

        fixture.token.value = "new-token"
        fixture.socket.emit(ReminderSocketEvent.ConnectionLost)
        runCurrent()
        advanceTimeBy(1_000L)
        runCurrent()

        assertEquals(listOf("old-token", "new-token"), fixture.socket.connectedTokens)
    }

    @Test
    fun `연속 이벤트와 foreground 복귀는 하나의 오늘 재조회로 합친다`() = runTest {
        val fixture = fixture(backgroundScope, initialState = AuthSessionState.AUTHENTICATED, token = "token")
        fixture.coordinator.start()
        runCurrent()

        fixture.socket.emit(ReminderSocketEvent.Connected)
        fixture.socket.emit(ReminderSocketEvent.RefetchRequired)
        fixture.coordinator.onAppForegrounded()
        runCurrent()
        advanceTimeBy(301L)
        runCurrent()

        assertEquals(1, fixture.repository.getDates.size)
    }

    @Test
    fun `INVALID_TOKEN은 한 연결에서 인증 만료를 한 번만 전달하고 재연결하지 않는다`() = runTest {
        val fixture = fixture(backgroundScope, initialState = AuthSessionState.AUTHENTICATED, token = "expired")
        fixture.coordinator.start()
        runCurrent()
        val authExpired = async { fixture.coordinator.authExpired.first() }

        fixture.socket.emit(ReminderSocketEvent.InvalidToken)
        fixture.socket.emit(ReminderSocketEvent.InvalidToken)
        runCurrent()
        authExpired.await()
        advanceTimeBy(30_000L)
        runCurrent()

        assertEquals(1, fixture.socket.connectedTokens.size)
    }

    private fun fixture(
        scope: kotlinx.coroutines.CoroutineScope,
        initialState: AuthSessionState = AuthSessionState.RESTORING,
        token: String? = null,
    ): Fixture {
        val session = FakeAuthSessionRepository(initialState)
        val tokenProvider = FakeTokenProvider(token)
        val socket = FakeReminderSocketClient()
        val repository = CountingReminderRepository()
        return Fixture(
            session = session,
            token = tokenProvider,
            socket = socket,
            repository = repository,
            coordinator = ReminderSocketSessionCoordinator(
                session,
                tokenProvider,
                socket,
                GetRemindersUseCase(repository),
                scope,
            ),
        )
    }
}

private data class Fixture(
    val session: FakeAuthSessionRepository,
    val token: FakeTokenProvider,
    val socket: FakeReminderSocketClient,
    val repository: CountingReminderRepository,
    val coordinator: ReminderSocketSessionCoordinator,
)

private class FakeAuthSessionRepository(initialState: AuthSessionState) : AuthSessionRepository {
    val state = MutableStateFlow(initialState)
    override val sessionState = state
    override suspend fun restoreSession() = state.value == AuthSessionState.AUTHENTICATED
    override suspend fun clearSession() { state.value = AuthSessionState.UNAUTHENTICATED }
    override fun hasSession() = state.value == AuthSessionState.AUTHENTICATED
}

private class FakeTokenProvider(var value: String?) : TokenProvider {
    override fun getAccessToken() = value
}

private class CountingReminderRepository : ReminderRepository {
    val getDates = mutableListOf<LocalDate>()
    override suspend fun getReminders(date: LocalDate): Result<ReminderListResult> {
        getDates += date
        return Result.success(ReminderListResult(emptyList(), ReminderDataSource.REMOTE))
    }
    override suspend fun getReminder(id: String): Result<Reminder> = error("unused")
    override suspend fun createReminder(command: CreateReminderCommand): Result<Reminder> = error("unused")
    override suspend fun updateReminder(id: String, command: UpdateReminderCommand): Result<Reminder> = error("unused")
    override suspend fun deleteReminder(id: String): Result<Unit> = error("unused")
    override fun observeReminders(date: LocalDate): Flow<List<Reminder>> = emptyFlow()
}
