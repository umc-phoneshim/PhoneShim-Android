package com.phoneshim.android.ui.features.auth.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is idle`() {
        val state = LoginViewModel().uiState.value

        assertNull(state.selectedProvider)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun `google login updates state then emits navigation effect`() = runTest(dispatcher) {
        val viewModel = LoginViewModel()
        val effect = async { viewModel.effect.first() }

        viewModel.onEvent(LoginUiEvent.GoogleLoginClicked)

        assertEquals(LoginProvider.GOOGLE, viewModel.uiState.value.selectedProvider)
        assertTrue(viewModel.uiState.value.isLoading)

        advanceTimeBy(500)
        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(LoginUiEffect.NavigateToGoalSetup, effect.await())
    }

    @Test
    fun `kakao login updates selected provider`() = runTest(dispatcher) {
        val viewModel = LoginViewModel()

        viewModel.onEvent(LoginUiEvent.KakaoLoginClicked)

        assertEquals(LoginProvider.KAKAO, viewModel.uiState.value.selectedProvider)
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `clicks while loading do not start another login`() = runTest(dispatcher) {
        val viewModel = LoginViewModel()

        viewModel.onEvent(LoginUiEvent.GoogleLoginClicked)
        viewModel.onEvent(LoginUiEvent.KakaoLoginClicked)

        assertEquals(LoginProvider.GOOGLE, viewModel.uiState.value.selectedProvider)

        advanceTimeBy(500)
        runCurrent()
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
