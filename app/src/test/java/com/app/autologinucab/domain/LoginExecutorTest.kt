package com.app.autologinucab.domain

import com.app.autologinucab.data.remote.PortalTransport
import com.app.autologinucab.domain.model.Credentials
import com.app.autologinucab.domain.model.LoginAttempt
import com.app.autologinucab.domain.model.LoginResult
import com.app.autologinucab.domain.model.LoginResultType
import com.app.autologinucab.domain.repository.CredentialsRepository
import com.app.autologinucab.domain.repository.InternetValidator
import com.app.autologinucab.domain.repository.LoginStatusRepository
import com.app.autologinucab.domain.usecase.LoginExecutor
import java.io.IOException
import java.net.SocketTimeoutException
import java.security.GeneralSecurityException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginExecutorTest {

    private val credentials = Credentials(
        username = "student@ucab",
        password = "secret",
        url = "http://legacy-portal.example"
    )
    private val credentialsRepository = FakeCredentialsRepository(credentials)
    private val transport = FakePortalTransport()
    private val internetValidator = FakeInternetValidator()
    private val statusRepository = RecordingLoginStatusRepository()
    private val executor = LoginExecutor(
        credentialsRepository = credentialsRepository,
        portalTransport = transport,
        internetValidator = internetValidator,
        loginStatusRepository = statusRepository
    )

    @Test
    fun successfulPostAndValidatedInternetRecordsSuccess() {
        internetValidator.validated = true

        val result = executor.execute()

        assertEquals(LoginResultType.SUCCESS, result.type)
        assertEquals(200, result.httpCode)
        assertEquals("student@ucab", transport.username)
        assertEquals("secret", transport.password)
        assertEquals(15_000L, internetValidator.timeoutMillis)
        assertEquals(
            listOf(LoginResultType.RUNNING, LoginResultType.SUCCESS),
            statusRepository.attempts.map { it.type }
        )
        assertTrue(statusRepository.attempts.all { it.timestamp > 0 })
    }

    @Test
    fun successfulPostWithoutValidatedInternetIsNotSuccess() {
        internetValidator.validated = false

        val result = executor.execute()

        assertEquals(LoginResultType.NO_INTERNET, result.type)
        assertEquals(200, result.httpCode)
        assertEquals(LoginResultType.NO_INTERNET, statusRepository.current()?.type)
    }

    @Test
    fun httpFailureDoesNotValidateInternetAndPreservesStatusCode() {
        transport.responseCode = 403

        val result = executor.execute()

        assertEquals(LoginResultType.HTTP_ERROR, result.type)
        assertEquals(403, result.httpCode)
        assertFalse(internetValidator.called)
        assertEquals(403, statusRepository.current()?.httpCode)
    }

    @Test
    fun transportFailureIsRecordedWithoutExceptionMessage() {
        transport.failure = IOException("sensitive host details")

        val result = executor.execute()

        assertEquals(LoginResultType.NETWORK_ERROR, result.type)
        assertNull(result.httpCode)
        assertFalse(internetValidator.called)
        assertEquals(LoginResultType.NETWORK_ERROR, statusRepository.current()?.type)
    }

    @Test
    fun timeoutHasItsOwnResult() {
        transport.failure = SocketTimeoutException("timeout")

        val result = executor.execute()

        assertEquals(LoginResultType.TIMEOUT, result.type)
        assertEquals(LoginResultType.TIMEOUT, statusRepository.current()?.type)
    }

    @Test
    fun missingOrBlankCredentialsNeverReachTransport() {
        credentialsRepository.value = Credentials("", "  ", "")

        val result = executor.execute()

        assertEquals(LoginResultType.MISSING_CREDENTIALS, result.type)
        assertFalse(transport.called)
        assertFalse(internetValidator.called)
    }

    @Test
    fun blankLegacyUrlDoesNotBlockFixedPortalLogin() {
        credentialsRepository.value = credentials.copy(url = "")
        internetValidator.validated = true

        val result = executor.execute()

        assertEquals(LoginResultType.SUCCESS, result.type)
        assertTrue(transport.called)
    }

    @Test
    fun unreadableEncryptedCredentialsHaveSafeResult() {
        credentialsRepository.failure = GeneralSecurityException("keystore details")

        val result = executor.execute()

        assertEquals(LoginResultType.CREDENTIALS_UNAVAILABLE, result.type)
        assertFalse(transport.called)
        assertEquals(LoginResultType.CREDENTIALS_UNAVAILABLE, statusRepository.current()?.type)
    }

    private class FakeCredentialsRepository(var value: Credentials?) : CredentialsRepository {
        var failure: Exception? = null

        override fun save(credentials: Credentials) {
            value = credentials
        }

        override fun get(): Credentials? {
            failure?.let { throw it }
            return value
        }

        override fun clear() {
            value = null
        }

        override fun hasAny(): Boolean = value != null
    }

    private class FakePortalTransport : PortalTransport {
        var responseCode: Int = 200
        var failure: IOException? = null
        var called: Boolean = false
        var username: String? = null
        var password: String? = null

        override fun post(username: String, password: String): Int {
            called = true
            this.username = username
            this.password = password
            failure?.let { throw it }
            return responseCode
        }
    }

    private class FakeInternetValidator : InternetValidator {
        var validated: Boolean = true
        var called: Boolean = false
        var timeoutMillis: Long? = null

        override fun awaitValidated(timeoutMillis: Long): Boolean {
            called = true
            this.timeoutMillis = timeoutMillis
            return validated
        }
    }

    private class RecordingLoginStatusRepository : LoginStatusRepository {
        val attempts = mutableListOf<LoginAttempt>()
        private var tick = 0L

        override fun recordRunning() {
            attempts += LoginAttempt(LoginResultType.RUNNING, null, ++tick)
        }

        override fun record(result: LoginResult) {
            attempts += LoginAttempt(result.type, result.httpCode, ++tick)
        }

        override fun current(): LoginAttempt? = attempts.lastOrNull()
    }
}
