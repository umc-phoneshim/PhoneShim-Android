package com.phoneshim.android.ui.common.base

import com.phoneshim.android.data.api.common.ApiError
import com.phoneshim.android.data.api.common.ApiException
import com.phoneshim.android.testutil.MainDispatcherRule
import java.io.IOException
import java.util.concurrent.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `401이면 인증 만료 효과를 발행한다`() = runTest {
        val viewModel = TestViewModel()
        val effects = mutableListOf<CommonUiEffect>()
        val job = collectCommonEffect(viewModel, effects)

        viewModel.fail(unauthorized())
        advanceUntilIdle()

        assertEquals(listOf(CommonUiEffect.AuthExpired), effects)
        job.cancel()
    }

    @Test
    fun `401이어도 화면이 상태를 정리하도록 콜백을 호출한다`() = runTest {
        val viewModel = TestViewModel()

        viewModel.fail(unauthorized())
        advanceUntilIdle()

        assertEquals(UiError.Kind.AUTH, viewModel.lastError?.kind)
    }

    @Test
    fun `네트워크 오류는 인증 만료 효과를 발행하지 않는다`() = runTest {
        val viewModel = TestViewModel()
        val effects = mutableListOf<CommonUiEffect>()
        val job = collectCommonEffect(viewModel, effects)

        viewModel.fail(ApiException.Network(IOException()))
        advanceUntilIdle()

        assertTrue(effects.isEmpty())
        assertEquals(UiError.Kind.NETWORK, viewModel.lastError?.kind)
        assertTrue(viewModel.lastError?.isRetryable == true)
        job.cancel()
    }

    @Test
    fun `서버 오류는 코드를 그대로 전달한다`() = runTest {
        val viewModel = TestViewModel()

        viewModel.fail(
            ApiException.Server(ApiError(code = "VALIDATION_ERROR", message = "잘못된 요청")),
        )
        advanceUntilIdle()

        assertEquals("VALIDATION_ERROR", viewModel.lastError?.code)
        assertEquals(UiError.Kind.SERVER, viewModel.lastError?.kind)
    }

    @Test
    fun `취소 예외는 오류로 처리하지 않고 다시 던진다`() = runTest {
        val viewModel = TestViewModel()

        try {
            viewModel.fail(CancellationException("스코프 취소"))
            fail("CancellationException 이 전파되어야 한다")
        } catch (expected: CancellationException) {
            // 기대한 동작
        }

        assertNull(viewModel.lastError)
    }

    private fun TestScope.collectCommonEffect(
        viewModel: TestViewModel,
        into: MutableList<CommonUiEffect>,
    ): Job = launch { viewModel.commonEffect.collect { into += it } }

    private fun unauthorized() = ApiException.Http(
        statusCode = 401,
        error = ApiError(code = "UNAUTHORIZED", message = "Unauthorized"),
        cause = RuntimeException(),
    )

    private data class TestState(val value: Int = 0) : UiState

    private object TestEvent : UiEvent

    private object TestEffect : UiEffect

    private class TestViewModel : BaseViewModel<TestState, TestEvent, TestEffect>(TestState()) {

        var lastError: UiError? = null
            private set

        override fun handleEvent(event: TestEvent) = Unit

        fun fail(throwable: Throwable) {
            handleError(throwable) { error -> lastError = error }
        }
    }
}