package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.Goal
import com.phoneshim.android.domain.repository.GoalRepository
import javax.inject.Inject

class SetGoalUseCase @Inject constructor(
    private val goalRepository: GoalRepository,
) {
    suspend operator fun invoke(goal: Goal): Result<Unit> =
        goalRepository.saveGoal(goal)
}
