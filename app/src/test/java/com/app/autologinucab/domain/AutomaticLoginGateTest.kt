package com.app.autologinucab.domain

import com.app.autologinucab.domain.model.LoginResultType
import com.app.autologinucab.domain.model.SsidRules
import com.app.autologinucab.domain.repository.AllowedNetworksRepository
import com.app.autologinucab.domain.repository.WifiStatusProvider
import com.app.autologinucab.domain.usecase.AutomaticLoginGate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AutomaticLoginGateTest {

    private val allowedNetworks = FakeAllowedNetworksRepository()
    private val wifi = FakeWifiStatusProvider()
    private val gate = AutomaticLoginGate(allowedNetworks, wifi)

    @Test
    fun missingSsidPermissionBlocksBeforeReadingTheSsid() {
        wifi.permissionGranted = false

        assertEquals(LoginResultType.PERMISSION_REQUIRED, gate.blockedResult()?.type)
        assertEquals(0, wifi.ssidReads)
    }

    @Test
    fun disabledLocationBlocksEvenWithPermission() {
        wifi.locationEnabled = false

        assertEquals(LoginResultType.LOCATION_DISABLED, gate.blockedResult()?.type)
    }

    @Test
    fun emptyListBlocksAutomaticLogin() {
        allowedNetworks.ssids = emptyList()

        assertEquals(LoginResultType.EMPTY_SSID_LIST, gate.blockedResult()?.type)
    }

    @Test
    fun unknownSsidIsNotAllowed() {
        wifi.ssid = null

        assertEquals(LoginResultType.SSID_NOT_ALLOWED, gate.blockedResult()?.type)
    }

    @Test
    fun unlistedSsidIsNotAllowed() {
        wifi.ssid = "Otra.Red"

        assertEquals(LoginResultType.SSID_NOT_ALLOWED, gate.blockedResult()?.type)
    }

    @Test
    fun listedSsidWithAndroidQuotesIsAllowed() {
        wifi.ssid = "\"Estudiantes.Ingenierias\""

        assertNull(gate.blockedResult())
    }

    @Test
    fun matchingIsExactAfterNormalization() {
        wifi.ssid = "estudiantes.ingenierias"

        assertEquals(LoginResultType.SSID_NOT_ALLOWED, gate.blockedResult()?.type)
    }

    private class FakeAllowedNetworksRepository : AllowedNetworksRepository {
        var ssids: List<String> = SsidRules.DEFAULT_SSIDS

        override fun allowedSsids(): List<String> = ssids

        override fun add(rawSsid: String): Boolean {
            val normalized = SsidRules.normalize(rawSsid) ?: return false
            if (ssids.any { it == normalized }) return false
            ssids = ssids + normalized
            return true
        }

        override fun remove(ssid: String) {
            ssids = ssids.filterNot { it == ssid }
        }

        override fun isAllowed(rawSsid: String?): Boolean {
            val normalized = SsidRules.normalize(rawSsid) ?: return false
            return ssids.any { it == normalized }
        }
    }

    private class FakeWifiStatusProvider : WifiStatusProvider {
        var permissionGranted = true
        var locationEnabled = true
        var ssid: String? = "Estudiantes.Ingenierias"
        var ssidReads = 0

        override fun hasSsidPermission(): Boolean = permissionGranted

        override fun isLocationEnabled(): Boolean = locationEnabled

        override fun currentSsid(): String? {
            ssidReads++
            return ssid
        }
    }
}
