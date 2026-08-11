package com.phoneshim.android.ui.features.report.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phoneshim.android.domain.model.RestSuggestion
import com.phoneshim.android.domain.model.SuggestionType
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

/**
 * 데일리 리포트 상단 "쉼이의 제안" 카드.
 *
 * 문구는 서버가 목표 대비 사용량을 보고 골라 내려줍니다(GET /api/reports/suggestion).
 * 화면은 [RestSuggestion.suggestionType] 에 따라 색만 다르게 표현합니다.
 * - 목표 초과(TOTAL_EXCEEDED / APP_EXCEEDED) : 주의를 주는 붉은 톤
 * - 목표 달성(ACHIEVED) : 브랜드 초록 톤
 * - 목표 없음(NO_GOAL) : 중립 톤
 */
@Composable
fun RestSuggestionCard(
    suggestion: RestSuggestion,
    modifier: Modifier = Modifier,
) {
    val tone = suggestion.suggestionType.toTone()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(tone.background, RoundedCornerShape(16.dp))
            .padding(PhoneShimDimensCardPadding),
    ) {
        Text(
            text = "쉼이의 제안",
            style = PhoneShimType.KorBodyM,
            fontWeight = FontWeight.Bold,
            color = tone.title,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = suggestion.message,
            style = PhoneShimType.KorCaption,
            color = PhoneShimTheme.colors.textSecondary,
        )
    }
}

private val PhoneShimDimensCardPadding = 20.dp

/** 제안 타입별 색 조합. */
private data class SuggestionTone(val background: Color, val title: Color)

@Composable
private fun SuggestionType.toTone(): SuggestionTone = when (this) {
    // 제안 1, 2 — 목표를 넘긴 상태라 붉은 톤으로 주의를 줍니다.
    SuggestionType.TOTAL_EXCEEDED,
    SuggestionType.APP_EXCEEDED,
    -> SuggestionTone(
        background = ExceededBackground,
        title = PhoneShimTheme.colors.error,
    )

    // 제안 3 — 목표 달성. 브랜드 톤으로 칭찬합니다.
    SuggestionType.ACHIEVED -> SuggestionTone(
        background = PhoneShimTheme.colors.brandSubtle,
        title = PhoneShimTheme.colors.brandStrong,
    )

    // 목표가 없거나 알 수 없는 상태는 중립 톤.
    SuggestionType.NO_GOAL,
    SuggestionType.UNKNOWN,
    -> SuggestionTone(
        background = PhoneShimTheme.colors.surfaceCream,
        title = PhoneShimTheme.colors.textPrimary,
    )
}

// 목표 초과 카드 배경. 팔레트에 아직 없는 톤이라 리포트 화면 로컬로 둡니다.
// TODO: 디자인 시스템에 경고 배경 토큰이 생기면 교체하세요.
private val ExceededBackground = Color(0xFFFBECEA)

@Preview(name = "제안 1 - 전체 목표 초과", showBackground = true)
@Composable
private fun RestSuggestionCardExceededPreview() {
    PhoneShimTheme {
        RestSuggestionCard(
            suggestion = RestSuggestion(
                suggestionType = SuggestionType.TOTAL_EXCEEDED,
                message = "오늘 폰 사용 시간이 목표보다 30분 많았어요.\n" +
                    "그 중에서 특히 인스타그램의 사용이 많이 나타났어요.\n" +
                    "내일은 인스타그램의 사용을 줄여 전체 폰 사용 시간을 줄여보아요.",
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
                message = "아직 전체 사용 목표가 없어요. 목표를 설정하면 매일 맞춤 제안을 받아볼 수 있어요.",
                excessMinutes = 0,
            ),
        )
    }
}
