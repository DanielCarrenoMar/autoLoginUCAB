package com.app.autologinucab.domain

import com.app.autologinucab.data.remote.PortalTransport
import com.app.autologinucab.domain.model.Credentials
import com.app.autologinucab.domain.model.LoginAttempt
import com.app.autologinucab.domain.model.LoginResult
import com.app.autologinucab.domain.model.LoginResultType
import com.app.autologinucab.domain.model.LoginTrigger
import com.app.autologinucab.domain.repository.AllowedNetworksRepository
import com.app.autologinucab.domain.repository.CredentialsRepository
import com.app.autologinucab.domain.repository.InternetValidator
import com.app.autologinucab.domain.repository.LoginStatusRepository
import com.app.autologinucab.domain.repository.WifiStatusProvider
import com.app.autologinucab.domain.usecase.AutomaticLoginGate
import com.app.autologinucab.domain.usecase.LoginCoordinator
import com.app.autologinucab.domain.usecase.LoginExecutor
import com.app.autologinucab.domain.usecase.LoginSingleFlight
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginCoordinatorTest {

    private val credentialsRepository =
        FakeCredentialsRepository(Credentials("student", "secret", ""))
    private val transport = FakePortalTransport()
    private val internetValidator = FakeInternetValidator()
    private val statusRepository = RecordingLoginStatusRepository()
    private val allowedNetworks = FakeAllowedNetworks(listOf("Estudiantes.Ingenierias"))
    private val wifi = FakeWifiStatusProvider()
    private val singleFlight = LoginSingleFlight()
    private var executorCreations = 0

    private val coordinator = LoginCoordinator(
        singleFlight = singleFlight,
        automaticGate = AutomaticLoginGate(allowedNetworks, wifi),
        loginStatusRepository = statusRepository,
        executorFactory = {
            executorCreations++
            LoginExecutor(
                credentialsRepository = credentialsRepository,
                portalTransport = transport,
                internetValidator = internetValidator,
                loginStatusRepository = statusRepository
            )
        }
    )

    @Test
    fun manualTriggerBypassesTheAutomaticGate() {
        wifi.permissionGranted = false

        val result = coordinator.run(LoginTrigger.MANUAL)

        assertEquals(LoginResultType.SUCCESS, result?.type)
        assertTrue(transport.called)
        assertEquals(1, executorCreations)
    }

    @Test
    fun tileTriggerBypassesTheAutomaticGate() {
        allowedNetworks.ssids = emptyList()

        val result = coordinator.run(LoginTrigger.TILE)

        assertEquals(LoginResultType.SUCCESS, result?.type)
        assertTrue(transport.called)
    }

    @Test
    fun automaticTriggerRunsWhenTheConnectionIsAllowed() {
        val result = coordinator.run(LoginTrigger.AUTOMATIC)

        assertEquals(LoginResultType.SUCCESS, result?.type)
        assertTrue(transport.called)
    }

    @Test
    fun automaticTriggerPersistsTheBlockedResultWithoutSending() {
        wifi.permissionGranted = false

        val result = coordinator.run(LoginTrigger.AUTOMATIC)

        assertEquals(LoginResultType.PERMISSION_REQUIRED, result?.type)
        assertFalse(transport.called)
        assertEquals(0, executorCreations)
        assertEquals(LoginResultType.PERMISSION_REQUIRED, statusRepository.current()?.type)
    }

    @Test
    fun automaticTriggerRejectsAnEmptyAllowList() {
        allowedNetworks.ssids = emptyList()

        val result = coordinator.run(LoginTrigger.AUTOMATIC)

        assertEquals(LoginResultType.EMPTY_SSID_LIST, result?.type)
        assertFalse(transport.called)
    }

    @Test
    fun concurrentAttemptReturnsNullAndDoesNotSend() {
        assertTrue(singleFlight.tryAcquire())

        val result = coordinator.run(LoginTrigger.MANUAL)

        assertNull(result)
        assertFalse(transport.called)

        singleFlight.release()
        assertEquals(LoginResultType.SUCCESS, coordinator.run(LoginTrigger.MANUAL)?.type)
    }

    @Test
    fun singleFlightIsReleasedAfterATransportFailure() {
        transport.failure = IOException("portal unreachable")

        assertEquals(LoginResultType.NETWORK_ERROR, coordinator.run(LoginTrigger.MANUAL)?.type)

        transport.failure = null
        assertEquals(LoginResultType.SUCCESS, coordinator.run(LoginTrigger.MANUAL)?.type)
    }

    private class FakeCredentialsRepository(var value: Credentials?) : CredentialsRepository {
        override fun save(credentials: Credentials) {
            value = credentials
        }

        override fun get(): Credentials? = value

        override fun clear() {
            value = null
        }

        override fun hasAny(): Boolean = value != null
    }

    private class FakePortalTransport : PortalTransport {
        var responseCode: Int = 200
        var failure: IOException? = null
        var called: Boolean = false

        override fun post(username: String, password: String): Int {
            called = true
            failure?.let { throw it }
            return responseCode
        }
    }

    private class FakeInternetValidator : InternetValidator {
        override fun awaitValidated(timeoutMillis: Long): Boolean = true
    }

    private class FakeAllowedNetworks(var ssids: List<String>) : AllowedNetworksRepository {
        override fun allowedSsids(): List<String> = ssids

        override fun add(rawSsid: String): Boolean = false

        override fun remove(ssid: String) {
            ssids = ssids.filterNot { it == ssid }
        }

        override fun isAllowed(rawSsid: String?): Boolean =
            ssids.any { it == rawSsid }
    }

    private class FakeWifiStatusProvider : WifiStatusProvider {
        var permissionGranted = true

        override fun hasSsidPermission(): Boolean = permissionGranted

        override fun isLocationEnabled(): Boolean = true

        override fun currentSsid(): String? = "Estudiantes.Ingenierias"
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
