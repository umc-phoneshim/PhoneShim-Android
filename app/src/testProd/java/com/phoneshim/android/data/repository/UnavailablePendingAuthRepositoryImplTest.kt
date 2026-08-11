package com.phoneshim.android.data.repository

import com.phoneshim.android.domain.model.AuthException
import com.phoneshim.android.domain.model.PendingAuthFeature
import com.phoneshim.android.domain.model.SocialIdentity
import com.phoneshim.android.domain.model.SocialProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UnavailablePendingAuthRepositoryImplTest {
    private val repository = UnavailablePendingAuthRepositoryImpl()
    private val identity = SocialIdentity(SocialProvider.GOOGLE, "provider-user", "user@example.com")

    @Test
    fun `prod recovery returns explicit unavailable feature`() = runTest {
        val error = repository.recoverWithdrawal(identity).exceptionOrNull()

        assertEquals(
            PendingAuthFeature.RECOVER_WITHDRAWAL,
            (error as AuthException.FeatureUnavailable).feature,
        )
    }
}
