package com.phoneshim.android.ui.features.auth.screen

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.features.auth.viewmodel.SessionRestoreState
import com.phoneshim.android.ui.features.auth.viewmodel.SplashViewModel
import kotlinx.coroutines.delay

private const val MIN_SPLASH_VISIBLE_DURATION_MILLIS = 1_500L

@Composable
fun SplashRoute(
    onAuthenticated: () -> Unit,
    onUnauthenticated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val splashStartedAt = remember { SystemClock.elapsedRealtime() }

    LaunchedEffect(state) {
        if (state == SessionRestoreState.LOADING) return@LaunchedEffect

        val elapsedTime = SystemClock.elapsedRealtime() - splashStartedAt
        val remainingTime = MIN_SPLASH_VISIBLE_DURATION_MILLIS - elapsedTime
        if (remainingTime > 0L) delay(remainingTime)

        when (state) {
            SessionRestoreState.LOADING -> Unit
            SessionRestoreState.AUTHENTICATED -> onAuthenticated()
            SessionRestoreState.UNAUTHENTICATED -> onUnauthenticated()
        }
    }

    SplashScreen(modifier = modifier)
}
