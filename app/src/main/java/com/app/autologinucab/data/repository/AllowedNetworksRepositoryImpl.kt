package com.app.autologinucab.data.repository

import com.app.autologinucab.domain.model.SsidRules
import com.app.autologinucab.domain.repository.AllowedNetworksRepository
import com.app.autologinucab.domain.repository.SsidSettingsStore

class AllowedNetworksRepositoryImpl(
    private val store: SsidSettingsStore
) : AllowedNetworksRepository {

    override fun allowedSsids(): List<String> {
        val stored = store.load()
        if (stored != null) return stored

        store.save(SsidRules.DEFAULT_SSIDS)
        return SsidRules.DEFAULT_SSIDS
    }

    override fun add(rawSsid: String): Boolean {
        val ssid = SsidRules.normalize(rawSsid) ?: return false
        val current = allowedSsids()
        if (current.any { it == ssid }) return false

        store.save(current + ssid)
        return true
    }

    override fun remove(ssid: String) {
        val current = allowedSsids()
        val updated = current.filterNot { it == ssid }
        if (updated.size != current.size) {
            store.save(updated)
        }
    }

    override fun isAllowed(rawSsid: String?): Boolean {
        val ssid = SsidRules.normalize(rawSsid) ?: return false
        return allowedSsids().any { it == ssid }
    }
}
