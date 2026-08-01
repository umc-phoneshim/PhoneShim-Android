package com.phoneshim.android.ui.features.auth.viewmodel

import com.phoneshim.android.domain.model.AuthUser
import com.phoneshim.android.domain.model.SocialProvider
import com.phoneshim.android.domain.model.SocialIdentity
import com.phoneshim.android.domain.model.AuthException
import com.phoneshim.android.domain.repository.AuthRepository
import com.phoneshim.android.domain.repository.PendingAuthRepository
import com.phoneshim.android.domain.usecase.SocialLoginUseCase
import com.phoneshim.android.domain.usecase.RecoverWithdrawalUseCase
import com.phoneshim.android.ui.features.auth.client.AuthClientResult
import com.phoneshim.android.ui.features.auth.client.GoogleAuthClient
import com.phoneshim.android.ui.features.auth.client.KakaoAuthClient
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
        val viewModel = createViewModel(loginResult = AuthUser(isNewUser = false))
        val effect = async { viewModel.effect.first() }

        viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.GOOGLE))
        runCurrent()

        assertEquals(LoginUiEffect.NavigateToMain, effect.await())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `new user login emits goal setup navigation`() = runTest(dispatcher) {
        val viewModel = createViewModel(loginResult = AuthUser(isNewUser = true))
        val effect = async { viewModel.effect.first() }

        viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.KAKAO))
        runCurrent()

        assertEquals(LoginUiEffect.NavigateToGoalSetup, effect.await())
    }

    @Test
    fun `cancelled social login returns to idle without error`() = runTest(dispatcher) {
        val viewModel = createViewModel(authResult = AuthClientResult.Cancelled)

        viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.GOOGLE))
        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `withdrawal pending login exposes recovery state`() = runTest(dispatcher) {
        val authResult = AuthClientResult.Success(
            providerAccessToken = "provider-token",
            providerUserId = "provider-user",
            email = "user@example.com",
        )
        val viewModel = createViewModel(
            authResult = authResult,
            loginFailure = AuthException.WithdrawalPending,
        )

        viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.GOOGLE))
        runCurrent()

        assertEquals(
            SocialIdentity(SocialProvider.GOOGLE, "provider-user", "user@example.com"),
            viewModel.uiState.value.withdrawalRecovery,
        )
    }

    @Test
    fun `clicks while loading do not replace selected provider`() = runTest(dispatcher) {
        val deferred = CompletableDeferred<AuthClientResult>()
        val viewModel = createViewModel(
            googleAuthClient = object : GoogleAuthClient {
                override suspend fun authenticate(): AuthClientResult = deferred.await()
            },
        )

        viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.GOOGLE))
        runCurrent()
        viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.KAKAO))

        assertEquals(SocialProvider.GOOGLE, viewModel.uiState.value.selectedProvider)
        assertTrue(viewModel.uiState.value.isLoading)

        deferred.complete(AuthClientResult.Cancelled)
        runCurrent()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    private fun createViewModel(
        authResult: AuthClientResult = AuthClientResult.Success("provider-token"),
        loginResult: AuthUser = AuthUser(isNewUser = false),
        loginFailure: Throwable? = null,
        googleAuthClient: GoogleAuthClient = object : GoogleAuthClient {
            override suspend fun authenticate(): AuthClientResult = authResult
        },
        kakaoAuthClient: KakaoAuthClient = object : KakaoAuthClient {
            override suspend fun authenticate(): AuthClientResult = authResult
        },
    ): LoginViewModel {
        val repository = object : AuthRepository {
            override suspend fun socialLogin(
                provider: SocialProvider,
                providerAccessToken: String,
            ): Result<AuthUser> = loginFailure?.let(Result.Companion::failure)
                ?: Result.success(loginResult)

            override suspend fun restoreSession(): Boolean = false
        }
        val pendingRepository = object : PendingAuthRepository {
            override suspend fun logout(): Result<Unit> = Result.success(Unit)
            override suspend fun recoverWithdrawal(
                identity: SocialIdentity,
            ): Result<AuthUser> = Result.success(AuthUser(isNewUser = false))

            override suspend fun linkAccount(identity: SocialIdentity): Result<Unit> = Result.success(Unit)
        }
        return LoginViewModel(
            googleAuthClient = googleAuthClient,
            kakaoAuthClient = kakaoAuthClient,
            socialLoginUseCase = SocialLoginUseCase(repository),
            recoverWithdrawalUseCase = RecoverWithdrawalUseCase(pendingRepository),
        )
    }
}
