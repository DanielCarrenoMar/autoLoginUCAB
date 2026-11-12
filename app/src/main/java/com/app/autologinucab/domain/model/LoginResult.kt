package com.app.autologinucab.domain.model

enum class LoginResultType {
    RUNNING,
    SUCCESS,
    NO_INTERNET,
    MISSING_CREDENTIALS,
    CREDENTIALS_UNAVAILABLE,
    HTTP_ERROR,
    NETWORK_ERROR,
    TIMEOUT,
    PERMISSION_REQUIRED,
    LOCATION_DISABLED,
    SSID_NOT_ALLOWED,
    EMPTY_SSID_LIST
}

data class LoginResult(
    val type: LoginResultType,
    val httpCode: Int? = null
)

data class LoginAttempt(
    val type: LoginResultType,
    val httpCode: Int?,
    val timestamp: Long
)
