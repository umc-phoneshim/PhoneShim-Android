package com.phoneshim.android.ui.features.appblocking.viewmodel

import androidx.lifecycle.ViewModel
import com.phoneshim.android.domain.model.UsageReasonSubmission
import com.phoneshim.android.domain.repository.UsageReasonRepository
import com.phoneshim.android.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UsageReasonViewModelFactoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = RecordingRepository()
    private val factory = UsageReasonViewModelFactory(repository)

    @Test
    fun `주입받은 Repository로 UsageReasonViewModel을 생성한다`() = runTest {
        val viewModel = factory.create(UsageReasonViewModel::class.java)
        viewModel.startSession("session", "com.example.app", "예시 앱")
        viewModel.selectReason("기타")
        viewModel.submitReason()
        advanceUntilIdle()

        assertEquals(
            listOf(UsageReasonSubmission("com.example.app", "기타")),
            repository.submissions,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `지원하지 않는 ViewModel 생성을 거부한다`() {
        factory.create(UnsupportedViewModel::class.java)
    }

    private class UnsupportedViewModel : ViewModel()

    private class RecordingRepository : UsageReasonRepository {
        val submissions = mutableListOf<UsageReasonSubmission>()

        override suspend fun saveUsageReason(
            submission: UsageReasonSubmission,
        ): Result<Unit> {
            submissions += submission
            return Result.success(Unit)
        }
    }
}
