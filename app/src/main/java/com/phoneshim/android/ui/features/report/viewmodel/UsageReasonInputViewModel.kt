package com.phoneshim.android.ui.features.report.viewmodel

import androidx.lifecycle.viewModelScope
import com.phoneshim.android.data.api.ApiErrorCodes
import com.phoneshim.android.data.api.ApiException
import com.phoneshim.android.domain.model.UsageReasonEntry
import com.phoneshim.android.domain.usecase.SubmitUsageReasonUseCase
import com.phoneshim.android.ui.common.base.BaseViewModel
import com.phoneshim.android.ui.common.base.UiEffect
import com.phoneshim.android.ui.common.base.UiEvent
import com.phoneshim.android.ui.common.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UsageReasonInputUiState(
    /** 타임테이블에서 선택한 사용 구간 식별자. 현재는 monitoredAppId 로 사용합니다. */
    val entryId: String = "",
    val date: String = "",
    val timeRangeStart: String = "",
    val timeRangeEnd: String = "",
    val reason: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    /** 입력 가능 시간대(당일 22:00~익일 10:00)를 벗어난 경우. */
    val isOutsideInputWindow: Boolean = false,
) : UiState {
    val remainingChars: Int get() = UsageReasonEntry.MAX_REASON_LENGTH - reason.length
    val canSubmit: Boolean
        get() = !isSubmitting && !isOutsideInputWindow &&
            reason.isNotBlank() && reason.length <= UsageReasonEntry.MAX_REASON_LENGTH
}

sealed interface UsageReasonInputUiEvent : UiEvent {
    data class Started(
        val entryId: String,
        val date: String,
        val timeRangeStart: String,
        val timeRangeEnd: String,
    ) : UsageReasonInputUiEvent

    data class ReasonChanged(val value: String) : UsageReasonInputUiEvent
    data object SubmitClicked : UsageReasonInputUiEvent
}

sealed interface UsageReasonInputUiEffect : UiEffect {
    /** 저장 완료. TODO: 포인트 리워드 팝업 정책이 정해지면 여기서 함께 처리하세요. */
    data object Submitted : UsageReasonInputUiEffect
    data class ShowMessage(val message: String) : UsageReasonInputUiEffect
}

@HiltViewModel
class UsageReasonInputViewModel @Inject constructor(
    private val submitUsageReasonUseCase: SubmitUsageReasonUseCase,
) : BaseViewModel<UsageReasonInputUiState, UsageReasonInputUiEvent, UsageReasonInputUiEffect>(
    UsageReasonInputUiState(),
) {

    override fun handleEvent(event: UsageReasonInputUiEvent) {
        when (event) {
            is UsageReasonInputUiEvent.Started -> start(event)
            is UsageReasonInputUiEvent.ReasonChanged -> changeReason(event)
            UsageReasonInputUiEvent.SubmitClicked -> submit()
        }
    }

    private fun start(event: UsageReasonInputUiEvent.Started) = setState {
        copy(
            entryId = event.entryId,
            date = event.date,
            timeRangeStart = event.timeRangeStart,
            timeRangeEnd = event.timeRangeEnd,
        )
    }

    private fun changeReason(event: UsageReasonInputUiEvent.ReasonChanged) = setState {
        copy(
            reason = event.value.take(UsageReasonEntry.MAX_REASON_LENGTH),
            errorMessage = null,
        )
    }

    private fun submit() {
        val state = currentState
        if (!state.canSubmit) return
        val entry = runCatching {
            UsageReasonEntry(
                monitoredAppId = state.entryId,
                date = state.date,
                timeRangeStart = state.timeRangeStart,
                timeRangeEnd = state.timeRangeEnd,
                reason = state.reason.trim(),
            )
        }.getOrElse { throwable ->
            setState { copy(errorMessage = throwable.message) }
            return
        }

        setState { copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            submitUsageReasonUseCase(entry)
                .onSuccess {
                    setState { copy(isSubmitting = false) }
                    sendEffect(UsageReasonInputUiEffect.Submitted)
                }
                .onFailure { throwable ->
                    val api = throwable as? ApiException
                    val outsideWindow = api?.code == ApiErrorCodes.USAGE_REASON_TIME_FORBIDDEN
                    setState {
                        copy(
                            isSubmitting = false,
                            isOutsideInputWindow = outsideWindow,
                            errorMessage = when {
                                outsideWindow ->
                                    "사용 이유는 당일 ${UsageReasonEntry.INPUT_WINDOW_START_HOUR}시부터 " +
                                        "다음날 ${UsageReasonEntry.INPUT_WINDOW_END_HOUR}시까지만 입력할 수 있어요."
                                api != null && api.isUnauthorized -> "다시 로그인해 주세요."
                                api != null -> api.message
                                else -> "저장하지 못했습니다. 다시 시도해 주세요."
                            },
                        )
                    }
                }
        }
    }
}
