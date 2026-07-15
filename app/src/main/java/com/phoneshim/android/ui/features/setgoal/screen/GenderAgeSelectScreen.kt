package com.phoneshim.android.ui.features.setgoal.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
                    GenderChip(
                        text = "남",
                        selected = gender == "남",
                        onClick = { onGenderSelected("남") },
                    )
                    Spacer(modifier = Modifier.width(PhoneShimDimens.spacing12))
                    GenderChip(
                        text = "여",
                        selected = gender == "여",
                        onClick = { onGenderSelected("여") },
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

// 남/여 선택 칩. 선택 시 브랜드 컬러로 채워집니다.
@Composable
private fun GenderChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(width = 36.dp, height = 28.dp)
            .clip(MaterialTheme.shapes.small)
            .background(
                if (selected) PhoneShimTheme.colors.brand else PhoneShimTheme.colors.surface,
            )
            .border(
                width = 1.dp,
                color = if (selected) PhoneShimTheme.colors.brand else PhoneShimTheme.colors.border,
                shape = MaterialTheme.shapes.small,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = PhoneShimType.KorLabel,
            color = if (selected) {
                PhoneShimTheme.colors.onBrand
            } else {
                PhoneShimTheme.colors.textPrimary
            },
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
        Row(
            modifier = Modifier
                .size(width = 96.dp, height = 28.dp)
                .clip(CircleShape)
                .background(PhoneShimTheme.colors.brand)
                .clickable { expanded = true }
                .padding(horizontal = PhoneShimDimens.spacing16),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = selectedAge ?: "나이 선택",
                style = PhoneShimType.KorLabel,
                color = PhoneShimTheme.colors.onBrand,
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = PhoneShimTheme.colors.onBrand,
                modifier = Modifier.size(12.dp),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            AgeOptions.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(text = option, style = PhoneShimType.KorCaption)
                    },
                    onClick = {
                        onAgeSelected(option)
                        expanded = false
                    },
                )
            }
        }
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
