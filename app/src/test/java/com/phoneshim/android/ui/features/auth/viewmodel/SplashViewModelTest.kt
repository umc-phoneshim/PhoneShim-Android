package com.phoneshim.android.ui.features.auth.viewmodel

import com.phoneshim.android.domain.model.AuthUser
import com.phoneshim.android.domain.model.SocialProvider
import com.phoneshim.android.domain.repository.AuthRepository
import com.phoneshim.android.domain.usecase.RestoreAuthSessionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {
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
    fun `restored token routes to authenticated state`() = runTest(dispatcher) {
        val viewModel = createViewModel(hasSession = true)

        runCurrent()

        assertEquals(SessionRestoreState.AUTHENTICATED, viewModel.state.value)
    }

    @Test
    fun `missing token routes to unauthenticated state`() = runTest(dispatcher) {
        val viewModel = createViewModel(hasSession = false)

        runCurrent()

        assertEquals(SessionRestoreState.UNAUTHENTICATED, viewModel.state.value)
    }

    private fun createViewModel(hasSession: Boolean): SplashViewModel {
        val repository = object : AuthRepository {
            override suspend fun socialLogin(
                provider: SocialProvider,
                providerAccessToken: String,
            ): Result<AuthUser> = Result.success(AuthUser(isNewUser = false))

            override suspend fun restoreSession(): Boolean = hasSession
        }
        return SplashViewModel(RestoreAuthSessionUseCase(repository))
    }
}
