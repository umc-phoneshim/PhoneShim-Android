package com.phoneshim.android.data.demo

import com.phoneshim.android.blocking.demo.DemoBlockTrigger
import com.phoneshim.android.data.database.dao.GoalDao
import com.phoneshim.android.data.database.dao.UserProfileDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersistentDemoScenarioResetter @Inject constructor(
    private val scenarioStore: DemoScenarioStore,
    private val trigger: DemoBlockTrigger,
    private val goalDao: GoalDao,
    private val userProfileDao: UserProfileDao,
) : DemoScenarioResetter {
    override suspend fun reset() {
        scenarioStore.reset()
        trigger.reset()
        goalDao.clearPhoneGoal()
        goalDao.clearAppGoals()
        userProfileDao.clearProfile()
    }
}
