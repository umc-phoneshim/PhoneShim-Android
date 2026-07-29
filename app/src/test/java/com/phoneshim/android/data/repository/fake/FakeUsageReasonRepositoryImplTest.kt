package com.phoneshim.android.data.repository.fake

import com.phoneshim.android.domain.model.UsageReasonSubmission
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeUsageReasonRepositoryImplTest {

    @Test
    fun `패키지별 최신 사용 이유를 메모리에 저장한다`() = runTest {
        val repository = FakeUsageReasonRepositoryImpl()
        val first = UsageReasonSubmission("com.example.video", "여가 시간")
        val latest = UsageReasonSubmission("com.example.video", "정보를 얻기 위해")

        repository.saveUsageReason(first).getOrThrow()
        repository.saveUsageReason(latest).getOrThrow()

        assertEquals(latest, repository.getSavedSubmission("com.example.video"))
    }
}
