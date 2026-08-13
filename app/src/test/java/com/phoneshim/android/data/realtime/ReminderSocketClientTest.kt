package com.phoneshim.android.data.realtime

import org.json.JSONObject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderSocketClientTest {
    @Test
    fun `requiresRefetch가 true인 payload만 재조회 신호로 판단한다`() {
        assertTrue(JSONObject("""{"requiresRefetch":true}""").requiresRefetch())
        assertTrue(mapOf("requiresRefetch" to true).requiresRefetch())
        assertFalse(JSONObject("""{"requiresRefetch":false}""").requiresRefetch())
        assertFalse(null.requiresRefetch())
    }

    @Test
    fun `INVALID_TOKEN을 직접 또는 data 내부 payload에서 찾는다`() {
        assertTrue(isInvalidTokenPayload(JSONObject("""{"code":"INVALID_TOKEN"}""")))
        assertTrue(
            isInvalidTokenPayload(
                JSONObject("""{"data":{"code":"INVALID_TOKEN"}}"""),
            ),
        )
        assertTrue(isInvalidTokenPayload(mapOf("data" to mapOf("code" to "INVALID_TOKEN"))))
        assertFalse(isInvalidTokenPayload(JSONObject("""{"code":"NETWORK_ERROR"}""")))
    }
}
