package com.phoneshim.android.ui.features.auth.viewmodel

import com.phoneshim.android.data.api.common.ApiError
import com.phoneshim.android.data.api.common.ApiErrorCodes
import com.phoneshim.android.data.api.common.ApiException
import com.phoneshim.android.domain.model.SocialLoginResult
import com.phoneshim.android.domain.model.SocialProvider
import com.phoneshim.android.domain.model.SocialIdentity
import com.phoneshim.android.domain.model.AuthException
import com.phoneshim.android.domain.model.AuthFeatureAvailability
import com.phoneshim.android.domain.model.User
import com.phoneshim.android.domain.model.WithdrawalResult
import com.phoneshim.android.domain.repository.AuthRepository
import com.phoneshim.android.domain.repository.CurrentUserRepository
import com.phoneshim.android.domain.repository.MyPageRepository
import com.phoneshim.android.domain.repository.PendingAuthRepository
import com.phoneshim.android.domain.usecase.SocialLoginUseCase
import com.phoneshim.android.domain.usecase.RecoverWithdrawalUseCase
import com.phoneshim.android.domain.usecase.GetMyInfoUseCase
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
    fun `email permission failure shows actionable Kakao guidance`() = runTest(dispatcher) {
        val failure = ApiException.Http(
            statusCode = 400,
            error = ApiError(
                code = ApiErrorCodes.EMAIL_PERMISSION_REQUIRED,
                message = "카카오 이메일 제공 동의가 필요합니다.",
            ),
            cause = IllegalStateException("HTTP 400"),
        )
        val viewModel = createViewModel(loginFailure = failure)

        viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.KAKAO))
        runCurrent()

        assertTrue(
            viewModel.uiState.value.errorMessage.orEmpty().contains(
                "카카오 로그인에 이메일 제공 동의가 필요합니다.",
            ),
        )
        assertTrue(
            viewModel.uiState.value.errorMessage.orEmpty().contains(
                "HTTP 400 / EMAIL_PERMISSION_REQUIRED",
            ),
        )
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `SDK failure exposes safe debug diagnostic without token`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            authResult = AuthClientResult.Failure(IllegalStateException("SDK failed")),
        )

        viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.KAKAO))
        runCurrent()

        assertTrue(viewModel.uiState.value.errorMessage.orEmpty().contains("SDK / IllegalStateException"))
        assertFalse(viewModel.uiState.value.errorMessage.orEmpty().contains("provider-token"))
    }

    @Test
    fun `missing Google credential shows account recovery guidance`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            authResult = AuthClientResult.Failure(AuthException.GoogleCredentialUnavailable),
        )

        viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.GOOGLE))
        runCurrent()

        assertTrue(viewModel.uiState.value.errorMessage.orEmpty().contains("Google 계정 상태"))
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `legacy invalid token error identifies outdated server for Google login`() =
        runTest(dispatcher) {
            val failure = ApiException.Http(
                statusCode = 401,
                error = ApiError(
                    code = ApiErrorCodes.INVALID_TOKEN,
                    message = "Invalid token.",
                ),
                cause = IllegalStateException("HTTP 401"),
            )
            val viewModel = createViewModel(loginFailure = failure)

            viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.GOOGLE))
            runCurrent()

            val message = viewModel.uiState.value.errorMessage.orEmpty()
            assertTrue(message.contains("서버 업데이트가 필요"))
            assertFalse(message.contains("카카오 로그인 정보"))
            assertTrue(message.contains("HTTP 401 / INVALID_TOKEN"))
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
                providerAccessToken: String,
            ): Result<SocialLoginResult> = loginFailure?.let(Result.Companion::failure)
                ?: Result.success(loginResult)
        }
        val pendingRepository = object : PendingAuthRepository {
            override suspend fun logout(): Result<Unit> = Result.success(Unit)
            override suspend fun recoverWithdrawal(
                identity: SocialIdentity,
            ): Result<SocialLoginResult> = Result.success(SocialLoginResult(isNewUser = false))

            override suspend fun linkAccount(identity: SocialIdentity): Result<Unit> = Result.success(Unit)
        }
        return LoginViewModel(
            googleAuthClient = googleAuthClient,
            kakaoAuthClient = kakaoAuthClient,
            socialLoginUseCase = SocialLoginUseCase(repository),
            recoverWithdrawalUseCase = RecoverWithdrawalUseCase(pendingRepository),
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
                canRecoverWithdrawal = true,
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
