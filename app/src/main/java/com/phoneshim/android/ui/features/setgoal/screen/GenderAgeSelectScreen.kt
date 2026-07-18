package com.phoneshim.android.ui.features.setgoal.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.common.SelectableChip
import com.phoneshim.android.ui.common.SelectionDropdown
import com.phoneshim.android.ui.common.SelectionField
import com.phoneshim.android.ui.features.setgoal.component.SetGoalBottomButtons
import com.phoneshim.android.ui.features.setgoal.component.SetGoalCard
import com.phoneshim.android.ui.features.setgoal.component.SetGoalCardDivider
import com.phoneshim.android.ui.features.setgoal.component.SetGoalStepIndicator
import com.phoneshim.android.ui.features.setgoal.component.SetGoalTitle
import com.phoneshim.android.ui.features.setgoal.component.SetGoalTopBar
import com.phoneshim.android.ui.features.setgoal.viewmodel.SetGoalViewModel
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

// 나이 선택 드롭다운 항목
private val AgeOptions = listOf("10대", "20대", "30대", "40대", "50대 이상")

// 성별/나이를 선택하는 목표 설정 1단계 화면 (Figma 04-1. 성별/나이선택)
@Composable
fun GenderAgeSelectScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SetGoalViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    GenderAgeSelectContent(
        gender = uiState.gender,
        age = uiState.ageGroup,
        onGenderSelected = viewModel::selectGender,
        onAgeSelected = viewModel::selectAgeGroup,
        onNext = onNext,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun GenderAgeSelectContent(
    gender: String?,
    age: String?,
    onGenderSelected: (String) -> Unit,
    onAgeSelected: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PhoneShimTheme.colors.background),
    ) {
        SetGoalTopBar(onBack = onBack)

        // Figma Maincontainer: p16 + 세로 gap 24
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = PhoneShimDimens.screenHorizontalPadding,
                    end = PhoneShimDimens.screenHorizontalPadding,
                    top = PhoneShimDimens.spacing16,
                ),
            verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing24),
        ) {
            SetGoalStepIndicator(currentStep = 1)
            SetGoalTitle(

                title = "하루 목표 폰 사용 시간을 설정해주세요!",
                subtitle = "하루 동안 사용할 목표 시간을 설정해요",
            )

            SetGoalCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "성별 선택",
                        style = PhoneShimType.KorCaption,
                        color = PhoneShimTheme.colors.textPrimary,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    SelectableChip(
                        text = "남",
                        selected = gender == "남",
                        onClick = { onGenderSelected("남") },
                        modifier = Modifier.width(36.dp),
                    )
                    Spacer(modifier = Modifier.width(PhoneShimDimens.spacing12))
                    SelectableChip(
                        text = "여",
                        selected = gender == "여",
                        onClick = { onGenderSelected("여") },
                        modifier = Modifier.width(36.dp),
                    )
                }

                SetGoalCardDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "나이 선택",
                        style = PhoneShimType.KorCaption,
                        color = PhoneShimTheme.colors.textPrimary,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    AgeDropdown(
                        selectedAge = age,
                        onAgeSelected = onAgeSelected,
                    )
                }
            }
        }

        SetGoalBottomButtons(
            onBack = onBack,
            onNext = {
                when {
                    gender == null -> Toast.makeText(
                        context, "성별을 선택해주세요", Toast.LENGTH_SHORT,
                    ).show()
                    age == null -> Toast.makeText(
                        context, "나이를 선택해주세요", Toast.LENGTH_SHORT,
                    ).show()
                    else -> onNext()
                }
            },
            showBack = false,
            modifier = Modifier.padding(
                horizontal = PhoneShimDimens.screenHorizontalPadding,
                vertical = PhoneShimDimens.spacing16,
            ),
        )
    }
}

// 나이 선택 드롭다운 (브랜드 컬러 알약 버튼)
@Composable
private fun AgeDropdown(
    selectedAge: String?,
    onAgeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier) {
        SelectionField(
            value = selectedAge,
            placeholder = "나이 선택",
            onClick = { expanded = true },
            modifier = Modifier.width(96.dp),
        )
        SelectionDropdown(
            expanded = expanded,
            options = AgeOptions,
            selected = selectedAge,
            optionLabel = { it },
            onSelected = {
                onAgeSelected(it)
                expanded = false
            },
            onDismiss = { expanded = false },
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun GenderAgeSelectScreenPreview() {
    PhoneShimTheme {
        GenderAgeSelectContent(
            gender = "남",
            age = "20대",
            onGenderSelected = {},
            onAgeSelected = {},
            onNext = {},
            onBack = {},
        )
    }
}
