package com.phoneshim.android.ui.features.appblocking.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.phoneshim.android.ui.common.PrimaryButton
import com.phoneshim.android.ui.common.PhoneShimButtonSize
import com.phoneshim.android.ui.features.appblocking.component.EmergencyAction
import com.phoneshim.android.ui.theme.PhoneShimPalette
import com.phoneshim.android.ui.theme.PhoneShimType
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.R

@Composable
fun GoalAchievedScreen(
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onOpenPhoneShim: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PhoneShimPalette.Primary100)
            .padding(PhoneShimDimens.spacing16),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing24),
    ) {
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing8)) {
//            Spacer(Modifier.height(PhoneShimDimens.blockingHeroIllustrationSize))
            Text("오늘 사용 목표를 달성했어요!", style = PhoneShimType.KorH1, color = PhoneShimPalette.Primary600)
            Text("잠깐 휴식 시간을 가져볼까요?", style = MaterialTheme.typography.bodyLarge, color = PhoneShimPalette.Gray700)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing16)) {
            EmergencyAction("전화", R.drawable.ic_phone, onCall)
            EmergencyAction("메시지", R.drawable.ic_message, onMessage)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("내일 다시 만나요", style = MaterialTheme.typography.labelMedium)
            Icon(
                painter = painterResource(R.drawable.ic_heart),
                contentDescription = null,
                tint = Color.Unspecified,
            )
        }
        Spacer(Modifier.weight(0.6f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            PrimaryButton(
                text = "폰쉼 어플",
                onClick = onOpenPhoneShim,
                modifier = Modifier.width(86.dp),
                size = PhoneShimButtonSize.Small,
                fullWidth = false,
                containerColor = PhoneShimPalette.Primary400,
                pressedContainerColor = PhoneShimPalette.Primary500,
                contentPadding = PaddingValues(0.dp),
            )
        }
    }
}

@Preview(name = "폰 전체 제한 이후", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun GoalAchievedScreenPreview() {
    PhoneShimTheme {
        GoalAchievedScreen(
            onCall = {},
            onMessage = {},
            onOpenPhoneShim = {},
        )
    }
}
