package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.Goal

interface GoalRepository {
    suspend fun getGoal(): Result<Goal?>
    suspend fun saveGoal(goal: Goal): Result<Unit>
}
