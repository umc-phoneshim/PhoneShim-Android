package com.phoneshim.android.ui.common.base

import com.phoneshim.android.data.api.common.ApiError
import com.phoneshim.android.data.api.common.ApiException
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UiErrorMapperTest {

    @Test
    fun `401은 인증 오류로 매핑한다`() {
        val error = UiErrorMapper.map(
            ApiException.Http(
                statusCode = 401,
                error = ApiError(code = "UNAUTHORIZED", message = "Unauthorized"),
                cause = RuntimeException(),
            ),
        )

        assertEquals(UiError.Kind.AUTH, error.kind)
        assertEquals("UNAUTHORIZED", error.code)
        assertFalse(error.isRetryable)
    }

    @Test
    fun `INVALID_TOKEN도 401이므로 인증 오류로 매핑한다`() {
        val error = UiErrorMapper.map(
            ApiException.Http(
                statusCode = 401,
                error = ApiError(code = "INVALID_TOKEN", message = "Invalid token"),
                cause = RuntimeException(),
            ),
        )

        assertEquals(UiError.Kind.AUTH, error.kind)
        assertEquals("INVALID_TOKEN", error.code)
    }

    @Test
    fun `401이 아닌 HTTP 오류는 서버 오류로 매핑하고 코드와 문구를 보존한다`() {
        val error = UiErrorMapper.map(
            ApiException.Http(
                statusCode = 403,
                error = ApiError(
                    code = "USAGE_REASON_TIME_FORBIDDEN",
                    message = "입력 가능 시간이 아닙니다.",
                ),
                cause = RuntimeException(),
            ),
        )

        assertEquals(UiError.Kind.SERVER, error.kind)
        assertEquals("USAGE_REASON_TIME_FORBIDDEN", error.code)
        assertEquals("입력 가능 시간이 아닙니다.", error.message)
        assertFalse(error.isRetryable)
    }

    @Test
    fun `오류 본문이 없는 HTTP 오류도 서버 오류로 매핑한다`() {
        val error = UiErrorMapper.map(
            ApiException.Http(statusCode = 500, error = null, cause = RuntimeException()),
        )

        assertEquals(UiError.Kind.SERVER, error.kind)
        assertNull(error.code)
        assertTrue(error.message.isNotBlank())
    }

    @Test
    fun `2xx 응답의 비즈니스 오류는 서버 오류로 매핑한다`() {
        val error = UiErrorMapper.map(
            ApiException.Server(
                ApiError(code = "VALIDATION_ERROR", message = "필수값이 누락되었습니다."),
            ),
        )

        assertEquals(UiError.Kind.SERVER, error.kind)
        assertEquals("VALIDATION_ERROR", error.code)
        assertEquals("필수값이 누락되었습니다.", error.message)
    }

    @Test
    fun `연결 실패는 재시도 가능한 네트워크 오류로 매핑한다`() {
        val error = UiErrorMapper.map(ApiException.Network(IOException()))

        assertEquals(UiError.Kind.NETWORK, error.kind)
        assertTrue(error.isRetryable)
        assertNull(error.code)
    }

    @Test
    fun `파싱 실패와 계약 위반 응답은 알 수 없는 오류로 매핑한다`() {
        val serialization = UiErrorMapper.map(ApiException.Serialization(RuntimeException()))
        val invalid = UiErrorMapper.map(ApiException.InvalidResponse("no data"))
        val unexpected = UiErrorMapper.map(ApiException.Unexpected(RuntimeException()))

        assertEquals(UiError.Kind.UNKNOWN, serialization.kind)
        assertEquals(UiError.Kind.UNKNOWN, invalid.kind)
        assertEquals(UiError.Kind.UNKNOWN, unexpected.kind)
        assertFalse(serialization.isRetryable)
    }

    @Test
    fun `ApiException이 아닌 예외도 알 수 없는 오류로 매핑한다`() {
        val error = UiErrorMapper.map(IllegalStateException("boom"))

        assertEquals(UiError.Kind.UNKNOWN, error.kind)
        assertTrue(error.message.isNotBlank())
    }

    @Test
    fun `서버 문구가 비어 있으면 기본 문구로 대체한다`() {
        val error = UiErrorMapper.map(
            ApiException.Server(ApiError(code = "NOT_FOUND", message = "")),
        )

        assertEquals(UiError.Kind.SERVER, error.kind)
        assertTrue(error.message.isNotBlank())
    }
}