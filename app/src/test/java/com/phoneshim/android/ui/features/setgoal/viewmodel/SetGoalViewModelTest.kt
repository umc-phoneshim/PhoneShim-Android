package com.phoneshim.android.ui.features.setgoal.viewmodel

import com.phoneshim.android.domain.model.Goal
import com.phoneshim.android.domain.model.InstalledApp
import com.phoneshim.android.domain.repository.GoalRepository
import com.phoneshim.android.domain.repository.InstalledAppsRepository
import com.phoneshim.android.domain.usecase.SetGoalUseCase
import com.phoneshim.android.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SetGoalViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val kakao = InstalledApp("com.kakao.talk", "카카오톡")

    @Test
    fun `프로토타입 기본 상태는 전체 폰 제한이 켜져 있다`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.blockAfterGoal)
    }

    @Test
    fun `단계별 누락 안내 문구가 Figma와 일치한다`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            SetGoalEffect.ShowMessage("성별과 나이 모두 입력해주세요"),
            effectFor(viewModel, SetGoalEvent.SubmitGenderAge),
        )
        assertEquals(
            SetGoalEffect.ShowMessage("목표 시간을 입력해주세요."),
            effectFor(viewModel, SetGoalEvent.SubmitTimeSet),
        )
        assertEquals(
            SetGoalEffect.ShowMessage("어플을 한 개 이상 선택해주세요."),
            effectFor(viewModel, SetGoalEvent.SubmitAppSelection),
        )
    }

    @Test
    fun `앱 목표가 10분 미만이면 다음 단계로 이동하지 않는다`() = runTest {
        val viewModel = createViewModel(installedApps = listOf(kakao))
        advanceUntilIdle()
        viewModel.onEvent(SetGoalEvent.ToggleApp(kakao))
        viewModel.onEvent(
            SetGoalEvent.SetAppTime(
                packageName = kakao.packageName,
                timeInput = AppTimeInput("00", "09"),
            ),
        )

        assertEquals(
            SetGoalEffect.ShowMessage("목표 사용 시간을 10분 이상 입력하세요."),
            effectFor(viewModel, SetGoalEvent.SubmitAppGoals),
        )

        viewModel.onEvent(
            SetGoalEvent.SetAppTime(
                packageName = kakao.packageName,
                timeInput = AppTimeInput("00", "10"),
            ),
        )
        assertEquals(
            SetGoalEffect.NavigateNext,
            effectFor(viewModel, SetGoalEvent.SubmitAppGoals),
        )
    }

    @Test
    fun `접근 제한을 켜면 Figma 안내를 노출한다`() = runTest {
        val viewModel = createViewModel(installedApps = listOf(kakao))
        advanceUntilIdle()
        viewModel.onEvent(SetGoalEvent.ToggleApp(kakao))

        assertEquals(
            SetGoalEffect.ShowMessage("목표 시간 이후 어플 사용이 제한됩니다."),
            effectFor(viewModel, SetGoalEvent.ToggleAccessLimit(kakao.packageName)),
        )
        assertTrue(viewModel.uiState.value.appSettings.getValue(kakao.packageName).accessLimited)
    }

    @Test
    fun `목표 저장 성공 후에만 완료 화면으로 이동한다`() = runTest {
        val successViewModel = createViewModel(saveResult = Result.success(Unit))
        advanceUntilIdle()
        assertEquals(
            SetGoalEffect.NavigateNext,
            effectFor(successViewModel, SetGoalEvent.SubmitGoal),
        )

        val failureViewModel = createViewModel(
            saveResult = Result.failure(IllegalStateException("save failed")),
        )
        advanceUntilIdle()
        assertEquals(
            SetGoalEffect.ShowMessage("목표 저장에 실패했어요"),
            effectFor(failureViewModel, SetGoalEvent.SubmitGoal),
        )
    }

    private fun createViewModel(
        installedApps: List<InstalledApp> = emptyList(),
        saveResult: Result<Unit> = Result.success(Unit),
    ): SetGoalViewModel {
        val goalRepository = FakeGoalRepository(saveResult)
        val installedAppsRepository = object : InstalledAppsRepository {
            override suspend fun getInstalledApps(): List<InstalledApp> = installedApps
        }
        return SetGoalViewModel(
            setGoalUseCase = SetGoalUseCase(goalRepository),
            installedAppsRepository = installedAppsRepository,
        )
    }

    private suspend fun kotlinx.coroutines.test.TestScope.effectFor(
        viewModel: SetGoalViewModel,
        event: SetGoalEvent,
    ): SetGoalEffect {
        val effect = async { viewModel.effect.first() }
        viewModel.onEvent(event)
        advanceUntilIdle()
        return effect.await()
    }

    private class FakeGoalRepository(
        private val saveResult: Result<Unit>,
    ) : GoalRepository {
        override suspend fun getGoal(): Result<Goal?> = Result.success(null)

        override suspend fun saveGoal(goal: Goal): Result<Unit> = saveResult
    }
}
