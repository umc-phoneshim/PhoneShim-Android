package com.phoneshim.android.ui.features.auth.viewmodel

import com.phoneshim.android.domain.repository.AuthSessionRepository
import com.phoneshim.android.domain.usecase.ClearAuthSessionUseCase
import com.phoneshim.android.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthSessionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `여러 화면에서 인증 만료가 반복되어도 세션 정리와 이동은 한 번만 수행한다`() = runTest {
        val repository = RecordingAuthSessionRepository()
        val viewModel = AuthSessionViewModel(ClearAuthSessionUseCase(repository))
        val effects = mutableListOf<AuthSessionEffect>()
        val job = collectEffects(viewModel, effects)

        repeat(3) { viewModel.onAuthExpired() }
        advanceUntilIdle()

        assertEquals(1, repository.clearCount)
        assertEquals(listOf(AuthSessionEffect.NavigateToLogin), effects)
        job.cancel()
    }

    @Test
    fun `a new session allows the next expiration to be handled`() = runTest {
        val repository = RecordingAuthSessionRepository()
        val viewModel = AuthSessionViewModel(ClearAuthSessionUseCase(repository))
        val effects = mutableListOf<AuthSessionEffect>()
        val job = collectEffects(viewModel, effects)

        viewModel.onAuthExpired()
        advanceUntilIdle()
        viewModel.onSessionStarted()
        viewModel.onAuthExpired()
        advanceUntilIdle()

        assertEquals(2, repository.clearCount)
        assertEquals(
            listOf(AuthSessionEffect.NavigateToLogin, AuthSessionEffect.NavigateToLogin),
            effects,
        )
        job.cancel()
    }

    private fun TestScope.collectEffects(
        viewModel: AuthSessionViewModel,
        into: MutableList<AuthSessionEffect>,
    ): Job = launch { viewModel.effect.collect { into += it } }

    private class RecordingAuthSessionRepository : AuthSessionRepository {
        var clearCount = 0

        override suspend fun restoreSession() = false

        override suspend fun clearSession() {
            clearCount += 1
        }

        override fun hasSession() = false
    }
}
