package com.app.autologinucab.domain.repository

import com.app.autologinucab.domain.model.Credentials

interface CredentialsRepository {
    fun save(credentials: Credentials)
    fun get(): Credentials?
    fun clear()
    fun hasAny(): Boolean
}
