package com.app.autologinucab.domain.usecase

import com.app.autologinucab.data.remote.PortalTransport
import com.app.autologinucab.domain.model.LoginResult
import com.app.autologinucab.domain.model.LoginResultType
import com.app.autologinucab.domain.repository.CredentialsRepository
import com.app.autologinucab.domain.repository.InternetValidator
import com.app.autologinucab.domain.repository.LoginStatusRepository
import java.io.IOException
import java.net.SocketTimeoutException
import java.security.GeneralSecurityException

class LoginExecutor(
    private val credentialsRepository: CredentialsRepository,
    private val portalTransport: PortalTransport,
    private val internetValidator: InternetValidator,
    private val loginStatusRepository: LoginStatusRepository
) {
    fun execute(): LoginResult {
        loginStatusRepository.recordRunning()
        val credentials = try {
            credentialsRepository.get()
        } catch (_: GeneralSecurityException) {
            return finish(LoginResult(LoginResultType.CREDENTIALS_UNAVAILABLE))
        } catch (_: IllegalArgumentException) {
            return finish(LoginResult(LoginResultType.CREDENTIALS_UNAVAILABLE))
        }

        if (credentials == null ||
            credentials.username.isBlank() ||
            credentials.password.isBlank()
        ) {
            return finish(LoginResult(LoginResultType.MISSING_CREDENTIALS))
        }

        val httpCode = try {
            portalTransport.post(credentials.username, credentials.password)
        } catch (_: SocketTimeoutException) {
            return finish(LoginResult(LoginResultType.TIMEOUT))
        } catch (_: IOException) {
            return finish(LoginResult(LoginResultType.NETWORK_ERROR))
        }

        if (httpCode !in 200..299) {
            return finish(LoginResult(LoginResultType.HTTP_ERROR, httpCode))
        }

        val validated = internetValidator.awaitValidated(VALIDATION_TIMEOUT_MILLIS)
        return finish(
            LoginResult(
                type = if (validated) LoginResultType.SUCCESS else LoginResultType.NO_INTERNET,
                httpCode = httpCode
            )
        )
    }

    private fun finish(result: LoginResult): LoginResult {
        loginStatusRepository.record(result)
        return result
    }

    companion object {
        const val VALIDATION_TIMEOUT_MILLIS = 15_000L
    }
}
