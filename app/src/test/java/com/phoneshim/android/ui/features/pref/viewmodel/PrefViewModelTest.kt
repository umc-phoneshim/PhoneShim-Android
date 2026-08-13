package com.phoneshim.android.ui.features.pref.viewmodel

import com.phoneshim.android.domain.model.Goal
import com.phoneshim.android.domain.model.User
import com.phoneshim.android.domain.repository.GoalRepository
import com.phoneshim.android.domain.repository.MyPageRepository
import com.phoneshim.android.domain.usecase.GetGoalUseCase
import com.phoneshim.android.domain.usecase.GetMyInfoUseCase
import com.phoneshim.android.domain.usecase.SetGoalUseCase
import com.phoneshim.android.domain.usecase.UpdateUserProfileUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrefViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: PrefViewModel
    private lateinit var repository: FakeGoalRepository
    private lateinit var userRepository: FakeMyPageRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeGoalRepository()
        userRepository = FakeMyPageRepository()
        viewModel = PrefViewModel(
            GetGoalUseCase(repository),
            SetGoalUseCase(repository),
            GetMyInfoUseCase(userRepository),
            UpdateUserProfileUseCase(userRepository),
        )
        testDispatcher.scheduler.runCurrent()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selectGender updates draft and closes selection popup`() {
        viewModel.onEvent(PrefUiEvent.GenderSelectionOpened)

        viewModel.onEvent(PrefUiEvent.GenderSelected(Gender.FEMALE))

        val state = viewModel.uiState.value
        assertEquals(Gender.FEMALE, state.draftSettings.gender)
        assertNull(state.selectionPopup)
        assertEquals(Gender.MALE, state.savedSettings.gender)
    }

    @Test
    fun `selectAgeGroup updates draft and closes selection popup`() {
        viewModel.onEvent(PrefUiEvent.AgeGroupSelectionOpened)

        viewModel.onEvent(PrefUiEvent.AgeGroupSelected(AgeGroup.FORTIES))

        val state = viewModel.uiState.value
        assertEquals(AgeGroup.FORTIES, state.draftSettings.ageGroup)
        assertNull(state.selectionPopup)
        assertEquals(AgeGroup.TWENTIES, state.savedSettings.ageGroup)
    }

    @Test
    fun `showTotalTimeEditor initializes inputs from draft total`() {
        viewModel.onEvent(PrefUiEvent.TotalTimeEditorOpened)

        assertEquals(
            TimeEditorState(
                target = TimeEditTarget.TotalGoal,
                hoursInput = "03",
                minutesInput = "30",
                isLimitEnabled = true,
            ),
            viewModel.uiState.value.timeEditor,
        )
    }

    @Test
    fun `total limit toggle is applied when goal time is confirmed`() {
        viewModel.onEvent(PrefUiEvent.TotalTimeEditorOpened)
        viewModel.onEvent(PrefUiEvent.TimeEditorLimitToggled)

        assertFalse(viewModel.uiState.value.timeEditor?.isLimitEnabled ?: true)
        assertTrue(viewModel.uiState.value.draftSettings.isTotalLimitEnabled)

        viewModel.onEvent(PrefUiEvent.GoalTimeConfirmed)

        assertFalse(viewModel.uiState.value.draftSettings.isTotalLimitEnabled)
        assertNull(viewModel.uiState.value.timeEditor)
    }

    @Test
    fun `time input keeps digits only and clears previous error`() {
        viewModel.onEvent(PrefUiEvent.TotalTimeEditorOpened)
        viewModel.onEvent(PrefUiEvent.MinutesInputChanged("99"))
        viewModel.onEvent(PrefUiEvent.GoalTimeConfirmed)

        viewModel.onEvent(PrefUiEvent.HoursInputChanged("1h2"))
        viewModel.onEvent(PrefUiEvent.MinutesInputChanged("3m0"))

        val editor = viewModel.uiState.value.timeEditor
        assertEquals("12", editor?.hoursInput)
        assertEquals("30", editor?.minutesInput)
        assertNull(editor?.error)
    }

    @Test
    fun `confirmGoalTime rejects minute outside valid range`() {
        viewModel.onEvent(PrefUiEvent.TotalTimeEditorOpened)
        viewModel.onEvent(PrefUiEvent.MinutesInputChanged("60"))

        viewModel.onEvent(PrefUiEvent.GoalTimeConfirmed)

        val state = viewModel.uiState.value
        assertEquals(TimeInputError.INVALID_MINUTE_RANGE, state.timeEditor?.error)
        assertEquals(210, state.draftSettings.totalGoalMinutes)
    }

    @Test
    fun `confirmGoalTime rejects goal below minimum`() {
        viewModel.onEvent(PrefUiEvent.TotalTimeEditorOpened)
        viewModel.onEvent(PrefUiEvent.HoursInputChanged("0"))
        viewModel.onEvent(PrefUiEvent.MinutesInputChanged("9"))

        viewModel.onEvent(PrefUiEvent.GoalTimeConfirmed)

        val state = viewModel.uiState.value
        assertEquals(TimeInputError.BELOW_MINIMUM, state.timeEditor?.error)
        assertEquals(210, state.draftSettings.totalGoalMinutes)
    }

    @Test
    fun `confirmGoalTime updates total goal and closes editor`() {
        viewModel.onEvent(PrefUiEvent.TotalTimeEditorOpened)
        viewModel.onEvent(PrefUiEvent.HoursInputChanged("4"))
        viewModel.onEvent(PrefUiEvent.MinutesInputChanged("15"))

        viewModel.onEvent(PrefUiEvent.GoalTimeConfirmed)

        val state = viewModel.uiState.value
        assertEquals(255, state.draftSettings.totalGoalMinutes)
        assertNull(state.timeEditor)
        assertTrue(state.validation.isValid)
    }

    @Test
    fun `confirmGoalTime updates only selected app goal`() {
        val goalsBeforeEdit = viewModel.uiState.value.draftSettings.appGoals
        viewModel.onEvent(PrefUiEvent.AppTimeEditorOpened("facebook"))
        viewModel.onEvent(PrefUiEvent.HoursInputChanged("2"))
        viewModel.onEvent(PrefUiEvent.MinutesInputChanged("0"))

        viewModel.onEvent(PrefUiEvent.GoalTimeConfirmed)

        val goals = viewModel.uiState.value.draftSettings.appGoals
        assertEquals(120, goals.single { it.id == "facebook" }.goalMinutes)
        assertEquals(
            goalsBeforeEdit.filterNot { it.id == "facebook" },
            goals.filterNot { it.id == "facebook" },
        )
    }

    @Test
    fun `app time popup applies limit toggle only after confirmation`() {
        viewModel.onEvent(PrefUiEvent.AppTimeEditorOpened("facebook"))
        viewModel.onEvent(PrefUiEvent.TimeEditorLimitToggled)

        assertFalse(viewModel.uiState.value.timeEditor?.isLimitEnabled ?: true)
        assertTrue(
            viewModel.uiState.value.draftSettings.appGoals
                .single { it.id == "facebook" }
                .isLimitEnabled,
        )

        viewModel.onEvent(PrefUiEvent.GoalTimeConfirmed)

        assertFalse(
            viewModel.uiState.value.draftSettings.appGoals
                .single { it.id == "facebook" }
                .isLimitEnabled,
        )
    }

    @Test
    fun `dismissing app time popup discards limit toggle`() {
        viewModel.onEvent(PrefUiEvent.AppTimeEditorOpened("facebook"))
        viewModel.onEvent(PrefUiEvent.TimeEditorLimitToggled)

        viewModel.onEvent(PrefUiEvent.TimeEditorDismissed)

        assertTrue(
            viewModel.uiState.value.draftSettings.appGoals
                .single { it.id == "facebook" }
                .isLimitEnabled,
        )
        assertNull(viewModel.uiState.value.timeEditor)
    }

    @Test
    fun `unknown app id does not open editors or change state`() {
        val initialState = viewModel.uiState.value

        viewModel.onEvent(PrefUiEvent.AppTimeEditorOpened("unknown"))
        viewModel.onEvent(PrefUiEvent.AppGoalEditorOpened("unknown"))

        assertEquals(initialState, viewModel.uiState.value)
    }

    @Test
    fun `disabling app limit removes it from validation targets`() {
        viewModel.onEvent(PrefUiEvent.AppTimeEditorOpened("kakao"))
        viewModel.onEvent(PrefUiEvent.HoursInputChanged("0"))
        viewModel.onEvent(PrefUiEvent.MinutesInputChanged("9"))
        viewModel.onEvent(PrefUiEvent.GoalTimeConfirmed)
        assertEquals(TimeInputError.BELOW_MINIMUM, viewModel.uiState.value.timeEditor?.error)

        // A rejected time input does not change the draft, and a disabled limit is not validated.
        viewModel.onEvent(PrefUiEvent.TimeEditorDismissed)
        viewModel.onEvent(PrefUiEvent.AppLimitToggled("kakao"))

        val state = viewModel.uiState.value
        assertFalse(state.draftSettings.appGoals.single { it.id == "kakao" }.isLimitEnabled)
        assertFalse("kakao" in state.validation.invalidAppGoalIds)
        assertTrue(state.validation.isValid)
    }

    @Test
    fun `saveAppDescription updates selected app and resets editor`() {
        viewModel.onEvent(PrefUiEvent.AppGoalEditorOpened("tiktok"))
        viewModel.onEvent(PrefUiEvent.AppDescriptionChanged("저녁에는 사용하지 않기"))

        viewModel.onEvent(PrefUiEvent.AppDescriptionSaved)

        val state = viewModel.uiState.value
        assertEquals(
            "저녁에는 사용하지 않기",
            state.draftSettings.appGoals.single { it.id == "tiktok" }.goalDescription,
        )
        assertNull(state.editingAppId)
        assertEquals("", state.appDescriptionInput)
    }

    @Test
    fun `dismissAppGoalEditor discards description input`() {
        val originalDescription = viewModel.uiState.value.draftSettings.appGoals
            .single { it.id == "kakao" }
            .goalDescription
        viewModel.onEvent(PrefUiEvent.AppGoalEditorOpened("kakao"))
        viewModel.onEvent(PrefUiEvent.AppDescriptionChanged("저장하지 않을 내용"))

        viewModel.onEvent(PrefUiEvent.AppGoalEditorDismissed)

        val state = viewModel.uiState.value
        assertEquals(
            originalDescription,
            state.draftSettings.appGoals.single { it.id == "kakao" }.goalDescription,
        )
        assertNull(state.editingAppId)
        assertEquals("", state.appDescriptionInput)
    }

    @Test
    fun `saveChanges persists valid draft settings`() = runTest(testDispatcher) {
        viewModel.onEvent(PrefUiEvent.GenderSelected(Gender.FEMALE))
        viewModel.onEvent(PrefUiEvent.AgeGroupSelected(AgeGroup.THIRTIES))

        viewModel.onEvent(PrefUiEvent.SaveChanges)
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals(state.draftSettings, state.savedSettings)
        assertEquals(Gender.FEMALE, state.savedSettings.gender)
        assertEquals(AgeGroup.THIRTIES, state.savedSettings.ageGroup)
    }

    @Test
    fun `discardChanges restores saved settings and closes every editor`() {
        viewModel.onEvent(PrefUiEvent.GenderSelected(Gender.FEMALE))
        viewModel.onEvent(PrefUiEvent.AgeGroupSelectionOpened)
        viewModel.onEvent(PrefUiEvent.TotalTimeEditorOpened)
        viewModel.onEvent(PrefUiEvent.AppGoalEditorOpened("kakao"))
        viewModel.onEvent(PrefUiEvent.AppDescriptionChanged("임시 입력"))

        viewModel.onEvent(PrefUiEvent.DiscardChanges)

        val state = viewModel.uiState.value
        assertEquals(state.savedSettings, state.draftSettings)
        assertEquals(Gender.MALE, state.draftSettings.gender)
        assertNull(state.selectionPopup)
        assertNull(state.timeEditor)
        assertNull(state.editingAppId)
        assertEquals("", state.appDescriptionInput)
        assertEquals(PrefValidationResult(), state.validation)
    }

    @Test
    fun `rejected editor input does not pollute the draft saved by saveChanges`() = runTest(testDispatcher) {
        viewModel.onEvent(PrefUiEvent.TotalTimeEditorOpened)
        viewModel.onEvent(PrefUiEvent.HoursInputChanged("0"))
        viewModel.onEvent(PrefUiEvent.MinutesInputChanged("9"))
        viewModel.onEvent(PrefUiEvent.GoalTimeConfirmed)
        assertEquals(TimeInputError.BELOW_MINIMUM, viewModel.uiState.value.timeEditor?.error)
        val savedBeforeAttempt = viewModel.uiState.value.savedSettings

        viewModel.onEvent(PrefUiEvent.SaveChanges)
        runCurrent()

        // Invalid editor input never enters the draft, so the valid draft remains saveable.
        assertEquals(savedBeforeAttempt, viewModel.uiState.value.savedSettings)
    }

    @Test
    fun `saveChanges emits a one-off settings saved effect`() = runTest(testDispatcher) {
        viewModel.onEvent(PrefUiEvent.GenderSelected(Gender.FEMALE))
        val effect = async { viewModel.effect.first() }

        viewModel.onEvent(PrefUiEvent.SaveChanges)
        runCurrent()

        assertEquals(PrefUiEffect.SettingsSaved, effect.await())
    }

    @Test
    fun `화면 진입 시 Goal을 saved와 draft에 반영한다`() {
        val settings = viewModel.uiState.value.savedSettings

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(210, settings.totalGoalMinutes)
        assertEquals("kakao", settings.appGoals.first().packageName)
        assertEquals("메신저 줄이기", settings.appGoals.first().goalDescription)
        assertFalse(viewModel.uiState.value.hasUnsavedChanges)
    }

    @Test
    fun `User 서버의 성별과 연령대를 Goal 로컬 값보다 우선 표시한다`() {
        assertEquals(Gender.MALE, viewModel.uiState.value.savedSettings.gender)
        assertEquals(AgeGroup.TWENTIES, viewModel.uiState.value.savedSettings.ageGroup)
    }

    @Test
    fun `성별 연령대 변경 시 User API 형식으로 저장한다`() = runTest(testDispatcher) {
        viewModel.onEvent(PrefUiEvent.GenderSelected(Gender.FEMALE))
        viewModel.onEvent(PrefUiEvent.AgeGroupSelected(AgeGroup.FIFTIES_OR_MORE))

        viewModel.onEvent(PrefUiEvent.SaveChanges)
        runCurrent()

        assertEquals("FEMALE", userRepository.lastGender)
        assertEquals("FIFTIES_PLUS", userRepository.lastAgeGroup)
        assertTrue(viewModel.uiState.value.hasServerUserProfile)
    }

    @Test
    fun `저장 실패 시 draft를 유지하고 saved는 변경하지 않는다`() = runTest(testDispatcher) {
        val savedBefore = viewModel.uiState.value.savedSettings
        repository.saveError = IllegalStateException("save failed")
        viewModel.onEvent(PrefUiEvent.TotalTimeEditorOpened)
        viewModel.onEvent(PrefUiEvent.HoursInputChanged("4"))
        viewModel.onEvent(PrefUiEvent.MinutesInputChanged("0"))
        viewModel.onEvent(PrefUiEvent.GoalTimeConfirmed)

        viewModel.onEvent(PrefUiEvent.SaveChanges)
        runCurrent()

        assertEquals(savedBefore, viewModel.uiState.value.savedSettings)
        assertEquals(240, viewModel.uiState.value.draftSettings.totalGoalMinutes)
        assertTrue(viewModel.uiState.value.hasUnsavedChanges)
        assertFalse(viewModel.uiState.value.isSaving)
        assertTrue(viewModel.uiState.value.errorMessage != null)
    }
}

private class FakeMyPageRepository : MyPageRepository {
    private var user = User(
        email = "user@phoneshim.com",
        nickname = "타로",
        gender = "MALE",
        ageGroup = "TWENTIES",
    )
    var lastGender: String? = null
    var lastAgeGroup: String? = null

    override suspend fun getMyInfo(): Result<User> = Result.success(user)

    override suspend fun updateMyInfo(name: String?, motivation: String?): Result<User> =
        Result.success(user)

    override suspend fun updateUserProfile(gender: String, ageGroup: String): Result<User> {
        lastGender = gender
        lastAgeGroup = ageGroup
        user = user.copy(gender = gender, ageGroup = ageGroup)
        return Result.success(user)
    }

    override suspend fun withdraw() = error("unused")
}

private class FakeGoalRepository : GoalRepository {
    var goal: Goal? = Goal(
        id = "goal-1",
        // User API 값이 이 로컬 Goal 프로필보다 우선되는지 검증하기 위해 다르게 둔다.
        gender = "FEMALE",
        ageGroup = "FORTIES",
        dailyGoalMinutes = 210,
        blockAfterGoal = true,
        apps = listOf(
            com.phoneshim.android.domain.model.AppGoal(
                packageName = "kakao",
                appName = "카카오톡",
                goalMinutes = 60,
                accessLimited = true,
                goalReason = "메신저 줄이기",
            ),
            com.phoneshim.android.domain.model.AppGoal(
                packageName = "facebook",
                appName = "페이스북",
                goalMinutes = 90,
                accessLimited = true,
            ),
            com.phoneshim.android.domain.model.AppGoal(
                packageName = "tiktok",
                appName = "틱톡",
                goalMinutes = 60,
                accessLimited = true,
            ),
        ),
    )
    var loadError: Throwable? = null
    var saveError: Throwable? = null

    override suspend fun getGoal(): Result<Goal?> =
        loadError?.let(Result<Goal?>::failure) ?: Result.success(goal)

    override suspend fun saveGoal(goal: Goal): Result<Unit> {
        saveError?.let { return Result.failure(it) }
        this.goal = goal
        return Result.success(Unit)
    }
}
