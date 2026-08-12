package com.phoneshim.android.ui.features.pref.viewmodel

import com.phoneshim.android.domain.model.AppGoal
import com.phoneshim.android.domain.model.Goal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PrefGoalMapperTest {

    @Test
    fun `Goal을 PREF 설정으로 변환할 때 앱 저장 계약을 보존한다`() {
        val goal = Goal(
            id = "goal-1",
            gender = "FEMALE",
            ageGroup = "THIRTIES",
            dailyGoalMinutes = 180,
            blockAfterGoal = true,
            apps = listOf(
                AppGoal(
                    packageName = "com.google.android.youtube",
                    appName = "YouTube",
                    goalMinutes = 60,
                    accessLimited = true,
                    targetCount = 3,
                    goalReason = "영상 줄이기",
                ),
            ),
        )

        val settings = goal.toPrefSettings(PrefMockData.initialSettings)
        val app = settings.appGoals.single()

        assertEquals("goal-1", settings.goalId)
        assertEquals(Gender.FEMALE, settings.gender)
        assertEquals(AgeGroup.THIRTIES, settings.ageGroup)
        assertEquals("com.google.android.youtube", app.id)
        assertEquals("com.google.android.youtube", app.packageName)
        assertEquals(3, app.targetCount)
        assertEquals("영상 줄이기", app.goalDescription)
    }

    @Test
    fun `PREF 설정을 Goal로 변환할 때 빈 goalReason은 null로 저장한다`() {
        val settings = PrefMockData.initialSettings.copy(
            appGoals = listOf(
                PrefMockData.initialSettings.appGoals.first().copy(
                    packageName = "com.kakao.talk",
                    goalDescription = "   ",
                    targetCount = 2,
                ),
            ),
        )

        val goal = settings.toGoal()
        val app = goal.apps.single()

        assertEquals("com.kakao.talk", app.packageName)
        assertEquals(2, app.targetCount)
        assertNull(app.goalReason)
    }
}
