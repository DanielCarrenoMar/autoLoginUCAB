package com.app.autologinucab.data.repository

import android.content.Context
import com.app.autologinucab.data.local.SecurePrefs
import com.app.autologinucab.domain.model.Credentials
import com.app.autologinucab.domain.repository.CredentialsRepository

class CredentialsRepositoryImpl(context: Context) : CredentialsRepository {
    private val securePrefs = SecurePrefs(context)

    override fun save(credentials: Credentials) {
        securePrefs.saveCredentials(credentials.username, credentials.password, credentials.url)
    }

    override fun get(): Credentials? {
        val c = securePrefs.readCredentials() ?: return null
        return Credentials(c.username, c.password, c.url)
    }

    override fun clear() {
        securePrefs.clearCredentials()
    }

    override fun hasAny(): Boolean = securePrefs.hasCredentials()
}
