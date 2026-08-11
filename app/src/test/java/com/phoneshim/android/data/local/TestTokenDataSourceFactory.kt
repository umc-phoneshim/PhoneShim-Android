package com.phoneshim.android.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.test.TestScope

fun TestScope.createTestTokenDataSource(file: File): TokenDataSource = TokenDataSource(
    PreferenceDataStoreFactory.create(
        scope = backgroundScope,
        produceFile = { file },
    ),
    FakeTokenCipher(),
)

class FakeTokenCipher : TokenCipher {
    override fun encrypt(plainText: String): EncryptedToken = EncryptedToken(
        ciphertext = plainText.reversed(),
        initializationVector = "test-iv",
    )

    override fun decrypt(encryptedToken: EncryptedToken): String =
        encryptedToken.ciphertext.reversed()
}
