package com.phoneshim.android.ui.features.auth.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.features.auth.viewmodel.SessionRestoreState
import com.phoneshim.android.ui.features.auth.viewmodel.SplashViewModel

@Composable
fun SplashRoute(
    onAuthenticated: () -> Unit,
    onUnauthenticated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state == SessionRestoreState.LOADING) return@LaunchedEffect

        when (state) {
            SessionRestoreState.LOADING -> Unit
            SessionRestoreState.AUTHENTICATED -> onAuthenticated()
            SessionRestoreState.UNAUTHENTICATED -> onUnauthenticated()
        }
    }

    SplashScreen(modifier = modifier)
}
