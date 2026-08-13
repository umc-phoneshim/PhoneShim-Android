package com.phoneshim.android.ui.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<S : UiState, E : UiEvent, F : UiEffect>(
    initialState: S,
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _effect = Channel<F>(Channel.BUFFERED)
    val effect: Flow<F> = _effect.receiveAsFlow()

    /**
     * 기능과 무관하게 처리가 같은 효과. [effect] 와 채널을 나눈 이유는 [CommonUiEffect] 참고.
     * 화면에서는 CollectCommonEffect(viewModel) { ... } 한 줄로 받는다.
     */
    private val _commonEffect = Channel<CommonUiEffect>(Channel.BUFFERED)
    val commonEffect: Flow<CommonUiEffect> = _commonEffect.receiveAsFlow()

    protected val currentState: S get() = _uiState.value

    fun onEvent(event: E) = handleEvent(event)

    protected abstract fun handleEvent(event: E)

    protected fun setState(reducer: S.() -> S) = _uiState.update(reducer)

    protected fun sendEffect(effect: F) {
        viewModelScope.launch { _effect.send(effect) }
    }

    /**
     * 실패한 요청의 공통 처리.
     *
     * - 인증 만료(401)면 [CommonUiEffect.AuthExpired] 를 발행한다.
     * - 그 경우에도 [onError] 는 호출한다. 화면이 로딩 상태를 풀어야 하기 때문이다.
     * - 오류 문구를 상태에 둘지 스낵바로 띄울지는 화면이 정한다.
     *   이 함수는 "무슨 오류인가"만 알려주고 표현 방식을 강제하지 않는다.
     *
     * 사용 예:
     * ```
     * .onFailure { throwable ->
     *     handleError(throwable) { error ->
     *         setState { copy(isSubmitting = false, errorMessage = error.message) }
     *     }
     * }
     * ```
     */
    protected fun handleError(throwable: Throwable, onError: (UiError) -> Unit) {
        // 화면 이탈로 코루틴이 취소된 것은 오류가 아니다.
        // 여기서 삼키면 취소가 전파되지 않아 상위 job 이 정상 종료로 오인한다.
        if (throwable is CancellationException) throw throwable

        val error = UiErrorMapper.map(throwable)
        if (error.kind == UiError.Kind.AUTH) {
            viewModelScope.launch { _commonEffect.send(CommonUiEffect.AuthExpired) }
        }
        onError(error)
    }
}