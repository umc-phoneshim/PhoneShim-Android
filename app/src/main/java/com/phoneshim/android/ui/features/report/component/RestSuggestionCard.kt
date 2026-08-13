package com.phoneshim.android.ui.features.report.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phoneshim.android.R
import com.phoneshim.android.domain.model.RestSuggestion
import com.phoneshim.android.domain.model.SuggestionType
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

/** 카드 오른쪽 마스코트 크기. */
private val MascotSize = 88.dp

/**
 * 데일리 리포트 상단 "쉼이의 제안" 카드.
 *
 * 왼쪽에 제목과 문구, 오른쪽에 쉼이 마스코트를 두는 가로 구성입니다.
 *
 * 문구는 서버가 목표 대비 사용량을 보고 골라 내려줍니다(GET /api/reports/suggestion).
 * 제안 1/2/3 베리언트는 카드 모양이 아니라 내려오는 [RestSuggestion.message] 로만 갈립니다.
 * TODO: 베리언트별 색이 따로 정해지면 [RestSuggestion.suggestionType] 으로 분기하세요.
 */
@Composable
fun RestSuggestionCard(
    suggestion: RestSuggestion,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(PhoneShimTheme.colors.brandSubtle, RoundedCornerShape(16.dp))
            .border(1.dp, PhoneShimTheme.colors.brand, RoundedCornerShape(16.dp))
            .padding(PhoneShimDimens.spacing16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "쉼이의 제안",
                style = PhoneShimType.KorH1,
                color = PhoneShimTheme.colors.textPrimary,
            )
            Spacer(modifier = Modifier.height(PhoneShimDimens.spacing12))
            Text(
                text = suggestion.message,
                style = PhoneShimType.KorCaption,
                color = PhoneShimTheme.colors.textSecondary,
            )
        }

        Spacer(modifier = Modifier.width(PhoneShimDimens.spacing8))

        Image(
            painter = painterResource(R.drawable.phoneshim_mascot),
            contentDescription = null,
            modifier = Modifier.size(MascotSize),
        )
    }
}

@Preview(name = "제안 1 - 전체 목표 초과", showBackground = true)
@Composable
private fun RestSuggestionCardExceededPreview() {
    PhoneShimTheme {
        RestSuggestionCard(
            suggestion = RestSuggestion(
                suggestionType = SuggestionType.TOTAL_EXCEEDED,
                message = "오늘 폰 사용 시간이 목표보다 30분 많았어요.\n" +
                    "그 중에서 특히 인스타그램의 사용이 많이 나타났어요.\n" +
                    "내일은 인스타그램의 사용을 줄여보아요.",
                excessMinutes = 30,
                appName = "인스타그램",
            ),
        )
    }
}

@Preview(name = "제안 2 - 앱 목표 초과", showBackground = true)
@Composable
private fun RestSuggestionCardAppExceededPreview() {
    PhoneShimTheme {
        RestSuggestionCard(
            suggestion = RestSuggestion(
                suggestionType = SuggestionType.APP_EXCEEDED,
                message = "오늘 인스타그램 사용 시간이 목표보다 42분 많았어요.\n" +
                    "내일은 10분만 줄여보는 건 어떨까요?",
                excessMinutes = 42,
                appName = "인스타그램",
            ),
        )
    }
}

@Preview(name = "제안 3 - 목표 달성", showBackground = true)
@Composable
private fun RestSuggestionCardAchievedPreview() {
    PhoneShimTheme {
        RestSuggestionCard(
            suggestion = RestSuggestion(
                suggestionType = SuggestionType.ACHIEVED,
                message = "오늘 목표를 달성했어요! 지금처럼 꾸준히 이어가 보세요.",
                excessMinutes = 0,
            ),
        )
    }
}

@Preview(name = "목표 미설정", showBackground = true)
@Composable
private fun RestSuggestionCardNoGoalPreview() {
    PhoneShimTheme {
        RestSuggestionCard(
            suggestion = RestSuggestion(
                suggestionType = SuggestionType.NO_GOAL,
                message = "아직 전체 사용 목표가 없어요.\n목표를 설정하면 매일 맞춤 제안을 받아볼 수 있어요.",
                excessMinutes = 0,
            ),
        )
    }
}
