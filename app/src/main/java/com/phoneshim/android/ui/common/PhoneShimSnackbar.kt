package com.phoneshim.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

enum class PhoneShimSnackbarType {
    Default,
    Info,
    Error,
}

@Composable
fun PhoneShimSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    type: PhoneShimSnackbarType = PhoneShimSnackbarType.Default,
) {
    val colors = PhoneShimTheme.colors
    val contentColor = when (type) {
        PhoneShimSnackbarType.Default -> colors.textSecondary
        PhoneShimSnackbarType.Info -> colors.snackbarInfo
        PhoneShimSnackbarType.Error -> colors.error
    }

    Box(
        modifier = modifier
            .widthIn(max = SnackbarMaxWidth)
            .fillMaxWidth()
            .height(SnackbarHeight)
            .background(
                color = colors.divider,
                shape = SnackbarShape,
            )
            .padding(SnackbarContentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = contentColor,
            style = PhoneShimType.KorCaption,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun PhoneShimSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier.padding(horizontal = SnackbarHorizontalPadding),
    ) { data ->
        val type = (data.visuals as? PhoneShimSnackbarVisuals)?.type
            ?: PhoneShimSnackbarType.Default
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            PhoneShimSnackbar(
                message = data.visuals.message,
                type = type,
            )
        }
    }
}

@Composable
fun PhoneShimBottomSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    PhoneShimSnackbarHost(
        hostState = hostState,
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = SnackbarBottomPadding),
    )
}

@Composable
fun PhoneShimBottomBarSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    PhoneShimSnackbarHost(
        hostState = hostState,
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = BottomBarDefaults.ContentBottomPadding),
    )
}

suspend fun SnackbarHostState.showPhoneShimSnackbar(
    message: String,
    type: PhoneShimSnackbarType = PhoneShimSnackbarType.Default,
    duration: SnackbarDuration = SnackbarDuration.Short,
): SnackbarResult = showSnackbar(
    PhoneShimSnackbarVisuals(
        message = message,
        type = type,
        duration = duration,
    ),
)

private data class PhoneShimSnackbarVisuals(
    override val message: String,
    val type: PhoneShimSnackbarType,
    override val duration: SnackbarDuration,
) : SnackbarVisuals {
    override val actionLabel: String? = null
    override val withDismissAction: Boolean = false
}

private val SnackbarHeight = 43.dp
private val SnackbarMaxWidth = 328.dp
private val SnackbarHorizontalPadding = 16.dp
private val SnackbarContentPadding = 12.dp
private val SnackbarBottomPadding = 16.dp
private val SnackbarShape = RoundedCornerShape(8.dp)

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PhoneShimSnackbarPreview() {
    PhoneShimTheme {
        androidx.compose.foundation.layout.Column(
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            PhoneShimSnackbar(message = "목표 사용 시간을 10분 이상 입력하세요.")
            PhoneShimSnackbar(
                message = "저장되었습니다.",
                type = PhoneShimSnackbarType.Info,
            )
            PhoneShimSnackbar(
                message = "삭제되었습니다.",
                type = PhoneShimSnackbarType.Error,
            )
        }
    }
}
