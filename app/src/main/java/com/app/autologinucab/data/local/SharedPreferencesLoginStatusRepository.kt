package com.app.autologinucab.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.app.autologinucab.domain.model.LoginAttempt
import com.app.autologinucab.domain.model.LoginResult
import com.app.autologinucab.domain.model.LoginResultType
import com.app.autologinucab.domain.repository.LoginStatusRepository

class SharedPreferencesLoginStatusRepository(
    context: Context,
    private val clock: () -> Long = System::currentTimeMillis
) : LoginStatusRepository {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun recordRunning() {
        store(LoginResult(LoginResultType.RUNNING))
    }

    override fun record(result: LoginResult) {
        store(result)
    }

    override fun current(): LoginAttempt? {
        val rawType = prefs.getString(KEY_TYPE, null) ?: return null
        val type = LoginResultType.entries.firstOrNull { it.name == rawType } ?: return null
        val storedCode = prefs.getInt(KEY_HTTP_CODE, NO_HTTP_CODE)
        return LoginAttempt(
            type = type,
            httpCode = storedCode.takeUnless { it == NO_HTTP_CODE },
            timestamp = prefs.getLong(KEY_TIMESTAMP, 0L)
        )
    }

    private fun store(result: LoginResult) {
        prefs.edit {
            putString(KEY_TYPE, result.type.name)
            if (result.httpCode == null) {
                remove(KEY_HTTP_CODE)
            } else {
                putInt(KEY_HTTP_CODE, result.httpCode)
            }
            putLong(KEY_TIMESTAMP, clock())
        }
    }

    companion object {
        private const val PREFS_NAME = "login_status"
        private const val KEY_TYPE = "type"
        private const val KEY_HTTP_CODE = "http_code"
        private const val KEY_TIMESTAMP = "timestamp"
        private const val NO_HTTP_CODE = -1
    }
}
