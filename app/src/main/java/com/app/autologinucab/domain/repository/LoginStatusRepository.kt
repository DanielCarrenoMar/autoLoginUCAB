package com.app.autologinucab.domain.repository

import com.app.autologinucab.domain.model.LoginAttempt
import com.app.autologinucab.domain.model.LoginResult

interface LoginStatusRepository {
    fun recordRunning()
    fun record(result: LoginResult)
    fun current(): LoginAttempt?
}
