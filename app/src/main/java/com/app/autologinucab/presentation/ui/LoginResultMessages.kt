package com.app.autologinucab.presentation.ui

import android.content.Context
import androidx.annotation.StringRes
import com.app.autologinucab.R
import com.app.autologinucab.domain.model.LoginAttempt
import com.app.autologinucab.domain.model.LoginResultType

/**
 * Maps login outcomes to non-sensitive, user-facing text. HTTP failures include only
 * the status code, never response bodies, URLs or credentials.
 */
object LoginResultMessages {

    @StringRes
    fun messageRes(type: LoginResultType): Int = when (type) {
        LoginResultType.RUNNING -> R.string.login_result_running
        LoginResultType.SUCCESS -> R.string.login_result_success
        LoginResultType.NO_INTERNET -> R.string.login_result_no_internet
        LoginResultType.MISSING_CREDENTIALS -> R.string.login_result_missing_credentials
        LoginResultType.CREDENTIALS_UNAVAILABLE -> R.string.login_result_credentials_unavailable
        LoginResultType.HTTP_ERROR -> R.string.login_result_http_error
        LoginResultType.NETWORK_ERROR -> R.string.login_result_network_error
        LoginResultType.TIMEOUT -> R.string.login_result_timeout
        LoginResultType.PERMISSION_REQUIRED -> R.string.login_result_permission_required
        LoginResultType.LOCATION_DISABLED -> R.string.login_result_location_disabled
        LoginResultType.SSID_NOT_ALLOWED -> R.string.login_result_ssid_not_allowed
        LoginResultType.EMPTY_SSID_LIST -> R.string.login_result_empty_ssid_list
        LoginResultType.INTERRUPTED -> R.string.login_result_interrupted
    }
}

object LoginResultFormatter {

    fun text(context: Context, attempt: LoginAttempt): String =
        text(context, attempt.type, attempt.httpCode)

    fun text(context: Context, type: LoginResultType, httpCode: Int?): String = when {
        type != LoginResultType.HTTP_ERROR -> context.getString(LoginResultMessages.messageRes(type))
        httpCode == null -> context.getString(R.string.login_result_http_error_unknown)
        else -> context.getString(R.string.login_result_http_error, httpCode)
    }
}
