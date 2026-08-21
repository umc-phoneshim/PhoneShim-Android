package com.phoneshim.android.blocking

import com.phoneshim.android.domain.model.AuthSessionState
import com.phoneshim.android.domain.repository.AuthSessionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BlockingSessionCoordinatorTest {

    @Test
    fun `세션 복원 중에는 차단 상태를 변경하지 않는다`() = runTest {
        val session = FakeAuthSessionRepository(AuthSessionState.RESTORING)
        val actions = RecordingBlockingSessionActions()
        val coordinator = BlockingSessionCoordinator(session, actions)

        coordinator.observe(backgroundScope)
        runCurrent()

        assertTrue(actions.events.isEmpty())
    }

    @Test
    fun `로그인 로그아웃 재로그인 순서대로 차단 엔진을 제어한다`() = runTest {
        val session = FakeAuthSessionRepository(AuthSessionState.RESTORING)
        val actions = RecordingBlockingSessionActions()
        val coordinator = BlockingSessionCoordinator(session, actions)
        coordinator.observe(backgroundScope)
        runCurrent()

        session.state.value = AuthSessionState.AUTHENTICATED
        runCurrent()
        session.state.value = AuthSessionState.UNAUTHENTICATED
        runCurrent()
        session.state.value = AuthSessionState.AUTHENTICATED
        runCurrent()

        assertEquals(
            listOf(
                "gate:true",
                "start",
                "gate:false",
                "stop",
                "gate:true",
                "start",
            ),
            actions.events,
        )
    }

    @Test
    fun `초기 로그아웃 상태에서도 게이트를 먼저 닫고 서비스를 중지한다`() = runTest {
        val session = FakeAuthSessionRepository(AuthSessionState.UNAUTHENTICATED)
        val actions = RecordingBlockingSessionActions()
        val coordinator = BlockingSessionCoordinator(session, actions)

        coordinator.observe(backgroundScope)
        runCurrent()

        assertEquals(listOf("gate:false", "stop"), actions.events)
    }
}

private class FakeAuthSessionRepository(initialState: AuthSessionState) : AuthSessionRepository {
    val state = MutableStateFlow(initialState)
    override val sessionState = state

    override suspend fun restoreSession(): Boolean =
        state.value == AuthSessionState.AUTHENTICATED

    override suspend fun clearSession() {
        state.value = AuthSessionState.UNAUTHENTICATED
    }

    override fun hasSession(): Boolean = state.value == AuthSessionState.AUTHENTICATED
}

private class RecordingBlockingSessionActions : BlockingSessionActions {
    val events = mutableListOf<String>()

    override fun setEnabled(enabled: Boolean) {
        events += "gate:$enabled"
    }

    override fun startIfPermitted() {
        events += "start"
    }

    override fun stop() {
        events += "stop"
    }
}
