package com.phoneshim.android.ui.common.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * 공통 효과 수신 배선.
 *
 * 인증 만료는 어느 화면에서 발생하든 귀결이 같으므로
 * 화면마다 when 분기를 반복하지 않도록 한 줄로 붙인다.
 * 기능별 effect 는 기존처럼 각 화면이 직접 collect 한다.
 *
 * 사용 예:
 * ```
 * CollectCommonEffect(viewModel) { onAuthExpired() }
 * ```
 */
@Composable
fun CollectCommonEffect(
    viewModel: BaseViewModel<*, *, *>,
    onAuthExpired: () -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.commonEffect.collect { effect ->
            when (effect) {
                CommonUiEffect.AuthExpired -> onAuthExpired()
            }
        }
    }
}