package com.app.autologinucab.domain.repository

/**
 * Storage port for the allowed-SSID list. Kept separate from Android so the
 * seeding and editing rules stay unit-testable on the JVM.
 */
interface SsidSettingsStore {

    /** Null means the user has never configured the list, so defaults must be seeded. */
    fun load(): List<String>?

    fun save(ssids: List<String>)
}
