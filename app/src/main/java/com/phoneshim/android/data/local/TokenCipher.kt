package com.phoneshim.android.data.local

data class EncryptedToken(
    val ciphertext: String,
    val initializationVector: String,
)

interface TokenCipher {
    fun encrypt(plainText: String): EncryptedToken
    fun decrypt(encryptedToken: EncryptedToken): String
}
