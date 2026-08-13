package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.Goal
import com.phoneshim.android.domain.repository.GoalRepository
import javax.inject.Inject

// 저장된 목표 조회 (온라인이면 서버, 오프라인이면 로컬 캐시 폴백).
class GetGoalUseCase @Inject constructor(
    private val goalRepository: GoalRepository,
) {
    suspend operator fun invoke(): Result<Goal?> = goalRepository.getGoal()
}
