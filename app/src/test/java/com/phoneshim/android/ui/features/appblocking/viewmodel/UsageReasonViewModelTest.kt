package com.phoneshim.android.ui.features.appblocking.viewmodel

import com.phoneshim.android.domain.model.UsageReasonSubmission
import com.phoneshim.android.domain.repository.UsageReasonRepository
import com.phoneshim.android.testutil.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UsageReasonViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `이유를 선택하지 않으면 저장하지 않는다`() = runTest {
        val repository = QueueRepository()
        val viewModel = startedViewModel(repository)

        viewModel.submitReason()
        advanceUntilIdle()

        assertTrue(repository.submissions.isEmpty())
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `저장 성공 후 effect를 한 번 발행한다`() = runTest {
        val repository = QueueRepository()
        val viewModel = startedViewModel(repository)
        viewModel.selectReason("여가 시간")
        val effect = async { viewModel.effect.first() }

        viewModel.submitReason()
        viewModel.submitReason()
        advanceUntilIdle()

        assertEquals(1, repository.submissions.size)
        assertEquals(
            UsageReasonUiEffect.ReasonSubmitted(
                sessionId = "session-a",
                submission = UsageReasonSubmission("com.example.video", "여가 시간"),
            ),
            effect.await(),
        )
        assertTrue(viewModel.uiState.value.isCompleted)
    }

    @Test
    fun `저장 실패 시 오류를 표시하고 재시도할 수 있다`() = runTest {
        val repository = QueueRepository(
            results = ArrayDeque(
                listOf(
                    Result.failure(IllegalStateException("임시 저장 실패")),
                    Result.success(Unit),
                ),
            ),
        )
        val viewModel = startedViewModel(repository)
        viewModel.selectReason("기타")

        viewModel.submitReason()
        advanceUntilIdle()

        assertEquals("임시 저장 실패", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isSaving)
        assertFalse(viewModel.uiState.value.isCompleted)

        val effect = async { viewModel.effect.first() }
        viewModel.submitReason()
        advanceUntilIdle()

        assertEquals(2, repository.submissions.size)
        assertTrue(effect.await() is UsageReasonUiEffect.ReasonSubmitted)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `이전 세션의 늦은 저장 결과를 무시한다`() = runTest {
        val repository = DeferredRepository()
        val viewModel = startedViewModel(repository)
        viewModel.selectReason("습관적으로")
        val pendingEffect = backgroundScope.async { viewModel.effect.first() }

        viewModel.submitReason()
        runCurrent()
        viewModel.startSession("session-b", "com.example.social", "소셜 앱")
        repository.result.complete(Result.success(Unit))
        advanceUntilIdle()

        assertEquals("session-b", viewModel.uiState.value.sessionId)
        assertFalse(viewModel.uiState.value.isCompleted)
        assertFalse(pendingEffect.isCompleted)
        pendingEffect.cancel()
    }

    private fun startedViewModel(repository: UsageReasonRepository) =
        UsageReasonViewModel(repository).apply {
            startSession("session-a", "com.example.video", "비디오 앱")
        }

    private class QueueRepository(
        private val results: ArrayDeque<Result<Unit>> =
            ArrayDeque(listOf(Result.success(Unit))),
    ) : UsageReasonRepository {
        val submissions = mutableListOf<UsageReasonSubmission>()

        override suspend fun saveUsageReason(
            submission: UsageReasonSubmission,
        ): Result<Unit> {
            submissions += submission
            return results.removeFirst()
        }
    }

    private class DeferredRepository : UsageReasonRepository {
        val result = CompletableDeferred<Result<Unit>>()

        override suspend fun saveUsageReason(
            submission: UsageReasonSubmission,
        ): Result<Unit> = result.await()
    }
}
