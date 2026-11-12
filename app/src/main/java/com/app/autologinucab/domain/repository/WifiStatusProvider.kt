package com.app.autologinucab.domain.repository

/**
 * Read-only view of the Wi-Fi environment needed to decide whether an automatic
 * login is allowed. Android-specific permission and location handling lives in the
 * data layer so this port stays unit-testable.
 */
interface WifiStatusProvider {

    /** True when the app can read the current SSID (location or nearby-Wi-Fi permission). */
    fun hasSsidPermission(): Boolean

    /** True when Android location services are enabled (required to read the SSID). */
    fun isLocationEnabled(): Boolean

    /** Current SSID, normalized, or null when unavailable or not connected to Wi-Fi. */
    fun currentSsid(): String?
}
