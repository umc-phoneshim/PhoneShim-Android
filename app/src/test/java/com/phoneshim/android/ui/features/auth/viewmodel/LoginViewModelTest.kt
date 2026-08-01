package com.phoneshim.android.ui.features.auth.viewmodel

import com.phoneshim.android.domain.model.SocialLoginResult
import com.phoneshim.android.domain.model.SocialProvider
import com.phoneshim.android.domain.repository.AuthRepository
import com.phoneshim.android.domain.usecase.SocialLoginUseCase
import com.phoneshim.android.ui.features.auth.social.SocialAuthClient
import com.phoneshim.android.ui.features.auth.social.SocialAuthResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
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
        val state = createViewModel().uiState.value

        assertNull(state.selectedProvider)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun `existing user login emits main navigation`() = runTest(dispatcher) {
        val viewModel = createViewModel(loginResult = SocialLoginResult.ExistingUser)
        val effect = async { viewModel.effect.first() }

        viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.GOOGLE))
        runCurrent()

        assertEquals(LoginUiEffect.NavigateToMain, effect.await())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `new user login emits goal setup navigation`() = runTest(dispatcher) {
        val viewModel = createViewModel(loginResult = SocialLoginResult.NewUser)
        val effect = async { viewModel.effect.first() }

        viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.KAKAO))
        runCurrent()

        assertEquals(LoginUiEffect.NavigateToGoalSetup, effect.await())
    }

    @Test
    fun `cancelled social login returns to idle without error`() = runTest(dispatcher) {
        val viewModel = createViewModel(authResult = SocialAuthResult.Cancelled)

        viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.GOOGLE))
        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `clicks while loading do not replace selected provider`() = runTest(dispatcher) {
        val deferred = CompletableDeferred<SocialAuthResult>()
        val viewModel = createViewModel(
            socialAuthClient = object : SocialAuthClient {
                override suspend fun authenticate(provider: SocialProvider): SocialAuthResult = deferred.await()
            },
        )

        viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.GOOGLE))
        runCurrent()
        viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.KAKAO))

        assertEquals(SocialProvider.GOOGLE, viewModel.uiState.value.selectedProvider)
        assertTrue(viewModel.uiState.value.isLoading)

        deferred.complete(SocialAuthResult.Cancelled)
        runCurrent()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    private fun createViewModel(
        authResult: SocialAuthResult = SocialAuthResult.Success("provider-token"),
        loginResult: SocialLoginResult = SocialLoginResult.ExistingUser,
        socialAuthClient: SocialAuthClient = object : SocialAuthClient {
            override suspend fun authenticate(provider: SocialProvider): SocialAuthResult = authResult
        },
    ): LoginViewModel {
        val repository = object : AuthRepository {
            override suspend fun socialLogin(
                provider: SocialProvider,
                providerAccessToken: String,
            ): Result<SocialLoginResult> = Result.success(loginResult)
        }
        return LoginViewModel(
            socialAuthClient = socialAuthClient,
            socialLoginUseCase = SocialLoginUseCase(repository),
        )
    }
}
