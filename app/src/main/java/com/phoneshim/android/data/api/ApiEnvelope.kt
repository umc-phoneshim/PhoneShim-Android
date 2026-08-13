package com.phoneshim.android.data.api

import com.google.gson.Gson
import retrofit2.HttpException

/**
 * [임시 복구 파일 / 삭제 예정]
 *
 * 공통 응답·오류 처리는 `data.api.common` (ApiResponse / ApiException / ApiCallExecutor) 으로
 * 이관되었습니다(28eb641). 다만 이관 당시 목표(Goal) 도메인이 함께 옮겨지지 않아
 * develop 브랜치가 컴파일되지 않는 상태였습니다.
 *
 * 아래 파일들이 아직 이 구(舊) 타입에 의존합니다.
 * - data/api/AppGoalApi.kt, TotalGoalApi.kt, MonitoredAppApi.kt (반환 타입 ApiEnvelope)
 * - data/repository/GoalRepositoryImpl.kt (runCatchingApi / unwrap / ApiException)
 * - test/.../GoalRepositoryImplTest.kt
 *
 * TODO(goal 도메인 담당): 위 파일들을 ApiResponse + ApiCallExecutor 로 옮긴 뒤 이 파일을 삭제해주세요.
 * 리포트/마이페이지 쪽은 이미 새 공통 계층만 사용합니다.
 */
data class ApiEnvelope<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiErrorBody? = null,
)

data class ApiErrorBody(
    val code: String,
    val message: String,
)

/** 서버가 내려준 에러 코드를 그대로 들고 다니는 예외. */
class ApiException(
    val code: String,
    val httpStatus: Int,
    override val message: String,
) : Exception(message) {

    /**
     * 422 INSUFFICIENT_*_DATA 계열인지 여부.
     * 이 경우는 "오류"가 아니라 "집계할 데이터가 아직 부족한 상태"로 화면에 표시해야 합니다.
     */
    val isInsufficientData: Boolean
        get() = httpStatus == 422 && code.startsWith("INSUFFICIENT_")

    /** 인증 만료/누락. 로그인 화면으로 보내야 하는 경우. */
    val isUnauthorized: Boolean
        get() = httpStatus == 401
}

object ApiErrorCodes {
    const val UNAUTHORIZED = "UNAUTHORIZED"
    const val INVALID_TOKEN = "INVALID_TOKEN"
    const val VALIDATION_ERROR = "VALIDATION_ERROR"
    const val USER_NOT_FOUND = "USER_NOT_FOUND"
    const val WITHDRAWAL_EXPIRED = "WITHDRAWAL_EXPIRED"
    const val USAGE_REASON_TIME_FORBIDDEN = "USAGE_REASON_TIME_FORBIDDEN"
    const val MONITORED_APP_NOT_FOUND = "MONITORED_APP_NOT_FOUND"
    const val INVALID_REPORT_RANGE = "INVALID_REPORT_RANGE"
    const val INSUFFICIENT_REPORT_DATA = "INSUFFICIENT_REPORT_DATA"
    const val INSUFFICIENT_AI_FEEDBACK_DATA = "INSUFFICIENT_AI_FEEDBACK_DATA"
    const val UNKNOWN = "UNKNOWN"
}

/** envelope 를 벗겨 data 를 꺼냅니다. success 가 false 이거나 data 가 없으면 [ApiException]. */
fun <T> ApiEnvelope<T>.unwrap(): T {
    if (!success || data == null) {
        val body = error
        throw ApiException(
            code = body?.code ?: ApiErrorCodes.UNKNOWN,
            httpStatus = 200,
            message = body?.message ?: "알 수 없는 오류가 발생했습니다.",
        )
    }
    return data
}

/**
 * Repository 에서 API 호출을 감쌀 때 사용합니다.
 * HTTP 에러 응답의 body 를 파싱해 서버 에러 코드를 [ApiException] 으로 살려 보냅니다.
 */
inline fun <T> runCatchingApi(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: HttpException) {
    Result.failure(e.toApiException())
} catch (e: ApiException) {
    Result.failure(e)
} catch (e: Exception) {
    Result.failure(e)
}

fun HttpException.toApiException(): ApiException {
    val raw = runCatching { response()?.errorBody()?.string() }.getOrNull()
    val parsed = raw?.let {
        runCatching { Gson().fromJson(it, ApiEnvelope::class.java).error }.getOrNull()
    }
    return ApiException(
        code = parsed?.code ?: ApiErrorCodes.UNKNOWN,
        httpStatus = code(),
        message = parsed?.message ?: (message() ?: "네트워크 오류가 발생했습니다."),
    )
}
