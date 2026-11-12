package com.app.autologinucab.domain.usecase

import com.app.autologinucab.domain.model.LoginResult
import com.app.autologinucab.domain.model.LoginResultType
import com.app.autologinucab.domain.model.SsidRules
import com.app.autologinucab.domain.repository.AllowedNetworksRepository
import com.app.autologinucab.domain.repository.WifiStatusProvider

/**
 * Decides whether an automatic login may run right now. Returns the non-sensitive
 * result to persist when it may not, or null when the Wi-Fi gate is satisfied.
 */
class AutomaticLoginGate(
    private val allowedNetworksRepository: AllowedNetworksRepository,
    private val wifiStatusProvider: WifiStatusProvider
) {

    fun blockedResult(): LoginResult? {
        if (!wifiStatusProvider.hasSsidPermission()) {
            return LoginResult(LoginResultType.PERMISSION_REQUIRED)
        }
        if (!wifiStatusProvider.isLocationEnabled()) {
            return LoginResult(LoginResultType.LOCATION_DISABLED)
        }

        val allowed = allowedNetworksRepository.allowedSsids()
        if (allowed.isEmpty()) {
            return LoginResult(LoginResultType.EMPTY_SSID_LIST)
        }

        val ssid = SsidRules.normalize(wifiStatusProvider.currentSsid())
            ?: return LoginResult(LoginResultType.SSID_NOT_ALLOWED)
        if (allowed.none { it == ssid }) {
            return LoginResult(LoginResultType.SSID_NOT_ALLOWED)
        }

        return null
    }
}
