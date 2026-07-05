package com.phoneshim.android.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.phoneshim.android.ui.theme.PhoneShimDimens

/**
 * 폰쉼 기본 CTA 버튼.
 * 브랜드 컬러/라운드/높이 토큰을 적용한 풀-와이드 버튼입니다.
 */
@Composable
fun PhoneShimButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(PhoneShimDimens.buttonHeight),
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}
