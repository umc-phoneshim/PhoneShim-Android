package com.phoneshim.android.ui.features.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoneshim.android.domain.usecase.RestoreAuthSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SessionRestoreState {
    LOADING,
    AUTHENTICATED,
    UNAUTHENTICATED,
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val restoreAuthSessionUseCase: RestoreAuthSessionUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(SessionRestoreState.LOADING)
    val state: StateFlow<SessionRestoreState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // 시작 시에는 로컬 토큰 복원만 판단한다. 서버 유효성은 이후 API의 401 흐름에서 처리한다.
            _state.value = try {
                if (restoreAuthSessionUseCase()) {
                    SessionRestoreState.AUTHENTICATED
                } else {
                    SessionRestoreState.UNAUTHENTICATED
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                SessionRestoreState.UNAUTHENTICATED
            }
        }
    }
}
