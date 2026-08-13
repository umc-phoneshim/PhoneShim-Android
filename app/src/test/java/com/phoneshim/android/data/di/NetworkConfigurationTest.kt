package com.phoneshim.android.data.di

import com.phoneshim.android.BuildConfig
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkConfigurationTest {
    @Test
    fun `flavor uses expected base url`() {
        val expectedUrl = when (BuildConfig.FLAVOR) {
            "dev" -> "https://api.phoneshim.com/"
            "prod" -> "http://52.79.234.34:3000/"
            else -> throw AssertionError("Unexpected flavor: ${BuildConfig.FLAVOR}")
        }

        assertEquals(expectedUrl, BuildConfig.BASE_URL)
    }

    @Test
    fun `body logging is enabled only for safe debug variants`() {
        val expectedLevel = if (BuildConfig.DEBUG && BuildConfig.ENABLE_NETWORK_BODY_LOGGING) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }

        assertEquals(expectedLevel, NetworkModule.provideHttpLoggingInterceptor().level)
    }
}
