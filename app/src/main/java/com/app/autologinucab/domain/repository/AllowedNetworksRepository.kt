package com.app.autologinucab.domain.repository

/**
 * Persistent list of Wi-Fi networks whose connection triggers the automatic login.
 */
interface AllowedNetworksRepository {

    /** Current list, seeded with [com.app.autologinucab.domain.model.SsidRules.DEFAULT_SSIDS] on first use. */
    fun allowedSsids(): List<String>

    /** Adds a normalized SSID. Returns false when it is blank or already present. */
    fun add(rawSsid: String): Boolean

    /** Removes an exact SSID. Removing an absent SSID is a no-op. */
    fun remove(ssid: String)

    /** True when [rawSsid] normalizes to an entry of the current list. */
    fun isAllowed(rawSsid: String?): Boolean
}
