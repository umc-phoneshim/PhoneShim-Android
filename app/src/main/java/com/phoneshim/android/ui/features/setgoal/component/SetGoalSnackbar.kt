package com.phoneshim.android.ui.features.setgoal.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

/**
 * 목표 설정 온보딩 전용 하단 안내.
 *
 * Figma 04-x Bottom Popup(328×43)의 크기, 색상, 타이포그래피를 그대로 사용한다.
 * 공용 Snackbar 스타일에는 영향을 주지 않도록 setgoal 기능 내부에 둔다.
 */
@Composable
fun SetGoalSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier.padding(horizontal = 16.dp),
    ) { data ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(43.dp)
                .background(
                    color = PhoneShimTheme.colors.divider,
                    shape = MaterialTheme.shapes.small,
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = data.visuals.message,
                style = PhoneShimType.KorCaption,
                color = PhoneShimTheme.colors.textSecondary,
            )
        }
    }
}
