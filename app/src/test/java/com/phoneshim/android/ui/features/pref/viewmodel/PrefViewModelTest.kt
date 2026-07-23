package com.phoneshim.android.ui.features.pref.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
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

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PrefViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selectGender updates draft and closes selection popup`() {
        viewModel.showGenderSelection()

        viewModel.selectGender(Gender.FEMALE)

        val state = viewModel.uiState.value
        assertEquals(Gender.FEMALE, state.draftSettings.gender)
        assertNull(state.selectionPopup)
        assertEquals(Gender.MALE, state.savedSettings.gender)
    }

    @Test
    fun `selectAgeGroup updates draft and closes selection popup`() {
        viewModel.showAgeGroupSelection()

        viewModel.selectAgeGroup(AgeGroup.FORTIES)

        val state = viewModel.uiState.value
        assertEquals(AgeGroup.FORTIES, state.draftSettings.ageGroup)
        assertNull(state.selectionPopup)
        assertEquals(AgeGroup.TWENTIES, state.savedSettings.ageGroup)
    }

    @Test
    fun `showTotalTimeEditor initializes inputs from draft total`() {
        viewModel.showTotalTimeEditor()

        assertEquals(
            TimeEditorState(
                target = TimeEditTarget.TotalGoal,
                hoursInput = "3",
                minutesInput = "30",
            ),
            viewModel.uiState.value.timeEditor,
        )
    }

    @Test
    fun `time input keeps digits only and clears previous error`() {
        viewModel.showTotalTimeEditor()
        viewModel.updateMinutesInput("99")
        viewModel.confirmGoalTime()

        viewModel.updateHoursInput("1h2")
        viewModel.updateMinutesInput("3m0")

        val editor = viewModel.uiState.value.timeEditor
        assertEquals("12", editor?.hoursInput)
        assertEquals("30", editor?.minutesInput)
        assertNull(editor?.error)
    }

    @Test
    fun `confirmGoalTime rejects minute outside valid range`() {
        viewModel.showTotalTimeEditor()
        viewModel.updateMinutesInput("60")

        assertFalse(viewModel.confirmGoalTime())

        val state = viewModel.uiState.value
        assertEquals(TimeInputError.INVALID_MINUTE_RANGE, state.timeEditor?.error)
        assertEquals(210, state.draftSettings.totalGoalMinutes)
    }

    @Test
    fun `confirmGoalTime rejects goal below minimum`() {
        viewModel.showTotalTimeEditor()
        viewModel.updateHoursInput("0")
        viewModel.updateMinutesInput("9")

        assertFalse(viewModel.confirmGoalTime())

        val state = viewModel.uiState.value
        assertEquals(TimeInputError.BELOW_MINIMUM, state.timeEditor?.error)
        assertEquals(210, state.draftSettings.totalGoalMinutes)
    }

    @Test
    fun `confirmGoalTime updates total goal and closes editor`() {
        viewModel.showTotalTimeEditor()
        viewModel.updateHoursInput("4")
        viewModel.updateMinutesInput("15")

        assertTrue(viewModel.confirmGoalTime())

        val state = viewModel.uiState.value
        assertEquals(255, state.draftSettings.totalGoalMinutes)
        assertNull(state.timeEditor)
        assertTrue(state.validation.isValid)
    }

    @Test
    fun `confirmGoalTime updates only selected app goal`() {
        val goalsBeforeEdit = viewModel.uiState.value.draftSettings.appGoals
        viewModel.showAppTimeEditor("facebook")
        viewModel.updateHoursInput("2")
        viewModel.updateMinutesInput("0")

        assertTrue(viewModel.confirmGoalTime())

        val goals = viewModel.uiState.value.draftSettings.appGoals
        assertEquals(120, goals.single { it.id == "facebook" }.goalMinutes)
        assertEquals(
            goalsBeforeEdit.filterNot { it.id == "facebook" },
            goals.filterNot { it.id == "facebook" },
        )
    }

    @Test
    fun `unknown app id does not open editors or change state`() {
        val initialState = viewModel.uiState.value

        viewModel.showAppTimeEditor("unknown")
        viewModel.showAppGoalEditor("unknown")

        assertEquals(initialState, viewModel.uiState.value)
    }

    @Test
    fun `disabling app limit removes it from validation targets`() {
        viewModel.showAppTimeEditor("kakao")
        viewModel.updateHoursInput("0")
        viewModel.updateMinutesInput("9")
        assertFalse(viewModel.confirmGoalTime())

        // A rejected time input does not change the draft, and a disabled limit is not validated.
        viewModel.dismissTimeEditor()
        viewModel.toggleAppLimit("kakao")

        val state = viewModel.uiState.value
        assertFalse(state.draftSettings.appGoals.single { it.id == "kakao" }.isLimitEnabled)
        assertFalse("kakao" in state.validation.invalidAppGoalIds)
        assertTrue(state.validation.isValid)
    }

    @Test
    fun `saveAppDescription updates selected app and resets editor`() {
        viewModel.showAppGoalEditor("tiktok")
        viewModel.updateAppDescription("저녁에는 사용하지 않기")

        viewModel.saveAppDescription()

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
        viewModel.showAppGoalEditor("kakao")
        viewModel.updateAppDescription("저장하지 않을 내용")

        viewModel.dismissAppGoalEditor()

        val state = viewModel.uiState.value
        assertEquals(
            originalDescription,
            state.draftSettings.appGoals.single { it.id == "kakao" }.goalDescription,
        )
        assertNull(state.editingAppId)
        assertEquals("", state.appDescriptionInput)
    }

    @Test
    fun `saveChanges persists valid draft settings`() {
        viewModel.selectGender(Gender.FEMALE)
        viewModel.selectAgeGroup(AgeGroup.THIRTIES)

        assertTrue(viewModel.saveChanges())

        val state = viewModel.uiState.value
        assertEquals(state.draftSettings, state.savedSettings)
        assertEquals(Gender.FEMALE, state.savedSettings.gender)
        assertEquals(AgeGroup.THIRTIES, state.savedSettings.ageGroup)
    }

    @Test
    fun `discardChanges restores saved settings and closes every editor`() {
        viewModel.selectGender(Gender.FEMALE)
        viewModel.showAgeGroupSelection()
        viewModel.showTotalTimeEditor()
        viewModel.showAppGoalEditor("kakao")
        viewModel.updateAppDescription("임시 입력")

        viewModel.discardChanges()

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
    fun `rejected editor input does not pollute the draft saved by saveChanges`() {
        viewModel.showTotalTimeEditor()
        viewModel.updateHoursInput("0")
        viewModel.updateMinutesInput("9")
        assertFalse(viewModel.confirmGoalTime())
        val savedBeforeAttempt = viewModel.uiState.value.savedSettings

        assertTrue(viewModel.saveChanges())

        // Invalid editor input never enters the draft, so the valid draft remains saveable.
        assertEquals(savedBeforeAttempt, viewModel.uiState.value.savedSettings)
    }
}
