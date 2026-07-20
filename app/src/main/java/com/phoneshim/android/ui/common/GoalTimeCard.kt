package com.phoneshim.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

@Composable
fun GoalTimeCard(
    label: String,
    totalMinutes: Int,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val shape = MaterialTheme.shapes.medium
    val borderColor = if (isError) {
        PhoneShimTheme.colors.error
    } else {
        PhoneShimTheme.colors.brand
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(PhoneShimTheme.colors.brandSubtle)
            .border(1.dp, borderColor, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                },
            )
            .padding(PhoneShimDimens.spacing16),
        verticalArrangement = Arrangement.spacedBy(
            space = PhoneShimDimens.spacing8,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        Text(
            text = label,
            style = PhoneShimType.KorCaption,
            color = PhoneShimTheme.colors.brandStrong,
        )
        DurationDisplay(totalMinutes = totalMinutes)
    }
}

@Preview(name = "기본", showBackground = true, widthDp = 360)
@Composable
private fun GoalTimeCardPreview() {
    PhoneShimTheme {
        GoalTimeCard(
            label = "총 목표 시간",
            totalMinutes = 210,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "클릭 가능", showBackground = true, widthDp = 360)
@Composable
private fun GoalTimeCardClickablePreview() {
    PhoneShimTheme {
        GoalTimeCard(
            label = "전체 폰 목표 시간",
            totalMinutes = 210,
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "오류", showBackground = true, widthDp = 360)
@Composable
private fun GoalTimeCardErrorPreview() {
    PhoneShimTheme {
        GoalTimeCard(
            label = "전체 폰 목표 시간",
            totalMinutes = 0,
            isError = true,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "긴 라벨", showBackground = true, widthDp = 360)
@Composable
private fun GoalTimeCardLongLabelPreview() {
    PhoneShimTheme {
        GoalTimeCard(
            label = "하루 동안 사용할 수 있는 전체 스마트폰 목표 시간",
            totalMinutes = 210,
            modifier = Modifier.padding(16.dp),
        )
    }
}
