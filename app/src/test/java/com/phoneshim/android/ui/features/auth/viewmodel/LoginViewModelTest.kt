package com.phoneshim.android.ui.features.auth.viewmodel

import com.phoneshim.android.domain.model.AuthException
import com.phoneshim.android.domain.model.AuthFeatureAvailability
import com.phoneshim.android.domain.model.SocialLoginResult
import com.phoneshim.android.domain.model.SocialProvider
import com.phoneshim.android.domain.model.User
import com.phoneshim.android.domain.model.WithdrawalResult
import com.phoneshim.android.domain.repository.AuthRepository
import com.phoneshim.android.domain.repository.CurrentUserRepository
import com.phoneshim.android.domain.repository.MyPageRepository
import com.phoneshim.android.domain.usecase.GetMyInfoUseCase
import com.phoneshim.android.domain.usecase.SocialLoginUseCase
import com.phoneshim.android.ui.features.auth.client.AuthClientResult
import com.phoneshim.android.ui.features.auth.client.GoogleAuthClient
import com.phoneshim.android.ui.features.auth.client.KakaoAuthClient
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
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
        assertFalse(state.isWithdrawalPending)
    }

    @Test
    fun `unavailable Google login ignores clicks`() = runTest(dispatcher) {
        val viewModel = createViewModel(canGoogleLogin = false)

        viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.GOOGLE))
        runCurrent()

        assertNull(viewModel.uiState.value.selectedProvider)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `existing user login emits main navigation`() = runTest(dispatcher) {
        val viewModel = createViewModel(loginResult = SocialLoginResult(isNewUser = false))
        val effect = async { viewModel.effect.first() }

        viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.GOOGLE))
        runCurrent()

        assertEquals(LoginUiEffect.NavigateToMain, effect.await())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `new user login emits goal setup navigation`() = runTest(dispatcher) {
        val viewModel = createViewModel(loginResult = SocialLoginResult(isNewUser = true))
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
    fun `withdrawal pending login exposes informational state without identity`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            loginFailure = AuthException.WithdrawalPending,
        )

        viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.GOOGLE))
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.isWithdrawalPending)
        assertFalse(state.isLoading)
        assertNull(state.selectedProvider)
        assertNull(state.errorMessage)
    }

    @Test
    fun `acknowledging withdrawal pending clears informational state`() = runTest(dispatcher) {
        val viewModel = createViewModel(loginFailure = AuthException.WithdrawalPending)

        viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.GOOGLE))
        runCurrent()
        viewModel.onEvent(LoginUiEvent.WithdrawalPendingAcknowledged)

        assertFalse(viewModel.uiState.value.isWithdrawalPending)
    }

    @Test
    fun `dismissing withdrawal pending clears informational state`() = runTest(dispatcher) {
        val viewModel = createViewModel(loginFailure = AuthException.WithdrawalPending)

        viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.KAKAO))
        runCurrent()
        viewModel.onEvent(LoginUiEvent.WithdrawalPendingDismissed)

        assertFalse(viewModel.uiState.value.isWithdrawalPending)
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
        loginResult: SocialLoginResult = SocialLoginResult(isNewUser = false),
        loginFailure: Throwable? = null,
        googleAuthClient: GoogleAuthClient = object : GoogleAuthClient {
            override suspend fun authenticate(): AuthClientResult = authResult
        },
        kakaoAuthClient: KakaoAuthClient = object : KakaoAuthClient {
            override suspend fun authenticate(): AuthClientResult = authResult
        },
        canGoogleLogin: Boolean = true,
    ): LoginViewModel {
        val repository = object : AuthRepository {
            override suspend fun socialLogin(
                provider: SocialProvider,
                providerToken: String,
            ): Result<SocialLoginResult> = loginFailure?.let(Result.Companion::failure)
                ?: Result.success(loginResult)
        }
        return LoginViewModel(
            googleAuthClient = googleAuthClient,
            kakaoAuthClient = kakaoAuthClient,
            socialLoginUseCase = SocialLoginUseCase(repository),
            getMyInfoUseCase = GetMyInfoUseCase(
                object : MyPageRepository {
                    override suspend fun getMyInfo() = Result.success(TEST_USER)
                    override suspend fun updateMyInfo(name: String?, motivation: String?) =
                        Result.success(TEST_USER)
                    override suspend fun withdraw(): Result<WithdrawalResult> = error("unused")
                },
            ),
            currentUserRepository = object : CurrentUserRepository {
                override val user = MutableStateFlow<User?>(null)
                override fun update(user: User) { this.user.value = user }
                override fun clear() { user.value = null }
            },
            authFeatureAvailability = AuthFeatureAvailability(
                canGoogleLogin = canGoogleLogin,
                shouldLoadRemoteProfile = true,
            ),
        )
    }

    private companion object {
        val TEST_USER = User(
            id = "user-id",
            email = "user@example.com",
            nickname = "쉼이",
        )
    }
}
