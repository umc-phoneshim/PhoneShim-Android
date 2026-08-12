package com.phoneshim.android.ui.features.report.viewmodel

import androidx.lifecycle.viewModelScope
import com.phoneshim.android.data.api.common.ApiErrorCodes
import com.phoneshim.android.domain.model.UsageReasonCode
import com.phoneshim.android.domain.model.UsageReasonEntry
import com.phoneshim.android.domain.usecase.SubmitUsageReasonUseCase
import com.phoneshim.android.ui.common.base.BaseViewModel
import com.phoneshim.android.ui.common.base.UiEffect
import com.phoneshim.android.ui.common.base.UiEvent
import com.phoneshim.android.ui.common.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 사용 이유 입력. 자유 입력이 아니라 5개 고정 선택지에서 복수 선택합니다.
 * (백엔드 prisma enum UsageReasonCode 기준)
 */
data class UsageReasonInputUiState(
    /** 타임테이블에서 고른 구간이 속한 주의 앱 ID. */
    val monitoredAppId: String = "",
    val appName: String = "",
    val date: String = "",
    val timeRangeStart: String = "",
    val timeRangeEnd: String = "",
    /** 화면에 보여줄 시간 라벨. 예) "22:00 ~ 22:35" */
    val timeRangeLabel: String = "",
    val selectedReasons: Set<UsageReasonCode> = emptySet(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    /** 입력 가능 시간대(당일 22시 ~ 익일 10시)를 벗어난 경우. */
    val isOutsideInputWindow: Boolean = false,
) : UiState {
    val options: List<UsageReasonCode> get() = UsageReasonCode.entries

    val canSubmit: Boolean
        get() = !isSubmitting && !isOutsideInputWindow && selectedReasons.isNotEmpty() &&
            monitoredAppId.isNotBlank() && timeRangeStart.isNotBlank() && timeRangeEnd.isNotBlank()
}

sealed interface UsageReasonInputUiEvent : UiEvent {
    data class Started(
        val monitoredAppId: String,
        val appName: String,
        val date: String,
        val timeRangeStart: String,
        val timeRangeEnd: String,
        val timeRangeLabel: String,
    ) : UsageReasonInputUiEvent

    data class ReasonToggled(val reason: UsageReasonCode) : UsageReasonInputUiEvent
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
            is UsageReasonInputUiEvent.ReasonToggled -> toggleReason(event)
            UsageReasonInputUiEvent.SubmitClicked -> submit()
        }
    }

    private fun start(event: UsageReasonInputUiEvent.Started) = setState {
        copy(
            monitoredAppId = event.monitoredAppId,
            appName = event.appName,
            date = event.date,
            timeRangeStart = event.timeRangeStart,
            timeRangeEnd = event.timeRangeEnd,
            timeRangeLabel = event.timeRangeLabel,
        )
    }

    private fun toggleReason(event: UsageReasonInputUiEvent.ReasonToggled) = setState {
        copy(
            selectedReasons = if (event.reason in selectedReasons) {
                selectedReasons - event.reason
            } else {
                selectedReasons + event.reason
            },
            errorMessage = null,
        )
    }

    private fun submit() {
        val state = currentState
        if (!state.canSubmit) return

        val entry = runCatching {
            UsageReasonEntry(
                monitoredAppId = state.monitoredAppId,
                date = state.date,
                timeRangeStart = state.timeRangeStart,
                timeRangeEnd = state.timeRangeEnd,
                reasonCodes = state.selectedReasons.toList(),
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
                    handleError(throwable) { error ->
                        val outsideWindow = error.code == ApiErrorCodes.USAGE_REASON_TIME_FORBIDDEN
                        setState {
                            copy(
                                isSubmitting = false,
                                isOutsideInputWindow = outsideWindow,
                                errorMessage = if (outsideWindow) {
                                    "사용 이유는 당일 ${UsageReasonEntry.INPUT_WINDOW_START_HOUR}시부터 " +
                                        "다음날 ${UsageReasonEntry.INPUT_WINDOW_END_HOUR}시까지만 입력할 수 있어요."
                                } else {
                                    error.message
                                },
                            )
                        }
                    }
                }
        }
    }
}
