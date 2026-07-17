package com.phoneshim.android.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimText
import com.phoneshim.android.ui.theme.PhoneShimTextTokens
import com.phoneshim.android.ui.theme.PhoneShimTheme

/**
 * 폰쉼 상단 바. 배리언트마다 title/leftIcon/rightIcon만 바뀌는 슬롯 방식.
 * 배경은 투명이라 화면 배경색이 그대로 비친다.
 *
 * 아이콘 슬롯에는 tint = PhoneShimTheme.colors.textPrimary를 적용한 Icon을 전달할 것
 * (피그마 원본은 좌측 아이콘이 #000이나 디자인 시스템에 없는 색이라 textPrimary로 통일).
 */
@Composable
fun PhoneShimTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    leftIcon: (@Composable () -> Unit)? = null,
    rightIcon: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp) // padding vertical 12 + content 28
            .padding(horizontal = 16.dp)
    ) {
        if (leftIcon != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(24.dp)
            ) {
                leftIcon()
            }
        }

        // 타이틀은 좌/우 아이콘 유무와 무관하게 항상 화면 정중앙
        PhoneShimText(
            text = title,
            style = PhoneShimTextTokens.h3,
            color = PhoneShimTheme.colors.textPrimary,
            modifier = Modifier.align(Alignment.Center)
        )

        if (rightIcon != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(24.dp)
            ) {
                rightIcon()
            }
        }
    }
}
