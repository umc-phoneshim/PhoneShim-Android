package com.phoneshim.android.ui.features.setgoal.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.phoneshim.android.ui.theme.PhoneShimTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// 패키지명으로 기기에 설치된 앱의 런처 아이콘을 그립니다.
// 주의 앱 목록은 실제 설치 앱이므로 아이콘도 번들 에셋이 아니라 PackageManager에서 읽습니다.
// 설치 앱이 수십~수백 개일 수 있어 IO 디스패처에서 비동기로 읽고,
// 준비 전·해석 실패(삭제된 앱)·프리뷰에서는 회색 자리 표시로 대체합니다.
@Composable
fun AppIcon(
    packageName: String,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val sizePx = with(LocalDensity.current) { size.roundToPx() }

    val icon by produceState<ImageBitmap?>(null, packageName, sizePx, isPreview) {
        if (isPreview) return@produceState
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.packageManager
                    .getApplicationIcon(packageName)
                    .toBitmap(sizePx, sizePx)
                    .asImageBitmap()
            }.getOrNull()
        }
    }

    val bitmap = icon
    if (bitmap == null) {
        AppIconPlaceholder(modifier = modifier.size(size))
    } else {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = modifier.size(size),
        )
    }
}

// 아이콘을 아직 못 읽었거나 해석에 실패했을 때의 회색 사각형.
// ui.common.AppInfoRow가 iconContent 없이 그리는 자리 표시와 같은 모양입니다.
@Composable
private fun AppIconPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(PhoneShimTheme.colors.divider),
    )
}
